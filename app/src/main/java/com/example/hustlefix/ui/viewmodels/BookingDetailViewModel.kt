package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustlefix.Booking
import com.example.hustlefix.Rating
import com.example.hustlefix.Service
import com.example.hustlefix.data.JobRepository
import com.example.hustlefix.data.RatingRepository
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class BookingDetailUiState(
    val booking: Booking? = null,
    val service: Service? = null,
    val walletBalance: Double = 0.0,
    val isLoading: Boolean = false,
    val isVerifyingPayment: Boolean = false,
    val error: String? = null,
    val isUpdateSuccess: Boolean = false
)

class BookingDetailViewModel(
    private val repository: JobRepository = JobRepository(),
    private val ratingRepository: RatingRepository = RatingRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(BookingDetailUiState())
    val uiState: StateFlow<BookingDetailUiState> = _uiState.asStateFlow()

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val auth: com.google.firebase.auth.FirebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
    private var bookingRef: DatabaseReference? = null
    private var bookingListener: ValueEventListener? = null

    fun loadBooking(bookingId: String) {
        if (bookingId.isEmpty()) return
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        // Load Wallet Balance
        val uid = auth.currentUser?.uid
        if (uid != null) {
            database.getReference("users").child(uid).child("walletBalance").get().addOnSuccessListener {
                _uiState.value = _uiState.value.copy(walletBalance = it.getValue(Double::class.java) ?: 0.0)
            }
        }

        // Clear existing listener
        bookingRef?.let { ref -> bookingListener?.let { ref.removeEventListener(it) } }

        bookingRef = database.getReference("bookings").child(bookingId)
        bookingListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val booking = snapshot.getValue(Booking::class.java)?.apply {
                    setBookingId(snapshot.key ?: "")
                }
                if (booking != null) {
                    val isNowPaid = booking.paymentStatus == "PAID"
                    
                    _uiState.value = _uiState.value.copy(
                        booking = booking,
                        isVerifyingPayment = _uiState.value.isVerifyingPayment && !isNowPaid
                    )
                    
                    val sid = booking.getJobId()
                    if (!sid.isNullOrEmpty() && (booking.jobId.isNullOrEmpty() || _uiState.value.service == null)) {
                        fetchServiceDetails(sid)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Booking not found")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
            }
        }
        bookingListener?.let { bookingRef?.addValueEventListener(it) }
    }

    fun fetchServiceDetails(serviceId: String) {
        database.getReference("services").child(serviceId).get().addOnSuccessListener { snapshot ->
            val service = snapshot.getValue(Service::class.java)
            _uiState.value = _uiState.value.copy(service = service, isLoading = false)
        }.addOnFailureListener {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun startPaymentVerification() {
        _uiState.value = _uiState.value.copy(isVerifyingPayment = true)
    }

    fun payWithWallet() {
        val booking = _uiState.value.booking ?: return
        val uid = auth.currentUser?.uid ?: return
        val amount = booking.amount ?: 0.0
        
        if (_uiState.value.walletBalance < amount) {
            _uiState.value = _uiState.value.copy(error = "Insufficient wallet balance")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                // 1. Deduct from client wallet
                val userRef = database.getReference("users").child(uid)
                val newBalance = _uiState.value.walletBalance - amount
                userRef.child("walletBalance").setValue(newBalance).await()

                // 2. Log Transaction for client
                val tRef = database.getReference("transactions").child(uid).push()
                tRef.setValue(mapOf(
                    "id" to tRef.key,
                    "type" to "Booking Payment",
                    "amount" to -amount,
                    "timestamp" to System.currentTimeMillis()
                )).await()

                // 3. Update Booking
                database.getReference("bookings").child(booking.bookingId).updateChildren(mapOf(
                    "paymentStatus" to "PAID",
                    "paidAt" to System.currentTimeMillis(),
                    "paymentMethod" to "wallet"
                )).await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    walletBalance = newBalance,
                    isUpdateSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun updateStatus(status: String) {
        val booking = _uiState.value.booking ?: return
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = repository.updateBookingStatus(booking, status)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isUpdateSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun submitRating(score: Float, comment: String, isAnonymous: Boolean) {
        val booking = _uiState.value.booking ?: return
        val currentUserId = auth.currentUser?.uid ?: return
        val currentUserName = auth.currentUser?.displayName ?: "User"
        
        val rating = Rating(
            booking.jobId,
            booking.getServiceTitle(),
            currentUserId,
            currentUserName,
            booking.workerId,
            booking.workerName,
            score,
            comment,
            isAnonymous
        )
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = ratingRepository.submitRating(rating)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isUpdateSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun clearStatus() {
        _uiState.value = _uiState.value.copy(isUpdateSuccess = false, error = null)
    }

    override fun onCleared() {
        super.onCleared()
        bookingRef?.let { ref -> bookingListener?.let { ref.removeEventListener(it) } }
    }
}
