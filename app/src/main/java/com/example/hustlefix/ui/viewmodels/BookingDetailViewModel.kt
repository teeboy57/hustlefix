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
                val platformFeeRate = 0.10 // 10% Commission
                val platformFee = amount * platformFeeRate
                val workerEarnings = amount - platformFee

                // 1. Deduct full amount from client wallet
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

                // 3. Move Commission to Admin Revenue Node
                val revenueRef = database.getReference("admin_revenue").push()
                revenueRef.setValue(mapOf(
                    "bookingId" to booking.bookingId,
                    "totalAmount" to amount,
                    "commission" to platformFee,
                    "timestamp" to System.currentTimeMillis()
                )).await()

                // 4. Update Admin Global Wallet Balance
                database.getReference("admin_wallet").child("balance").runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val currentBalance = currentData.getValue(Double::class.java) ?: 0.0
                        currentData.value = currentBalance + platformFee
                        return Transaction.success(currentData)
                    }
                    override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {}
                })

                // 5. Update Booking with payout details
                database.getReference("bookings").child(booking.bookingId).updateChildren(mapOf(
                    "paymentStatus" to "PAID",
                    "paidAt" to System.currentTimeMillis(),
                    "paymentMethod" to "wallet",
                    "platformFee" to platformFee,
                    "workerEarnings" to workerEarnings
                )).await()

                // Log Activity
                com.example.hustlefix.util.ActivityLogger.log(uid, "System", "PAYMENT_PROCESSED", "R$amount paid for booking ${booking.bookingId}")

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

    fun updateStatus(status: String, inputCode: String? = null) {
        val booking = _uiState.value.booking ?: return
        
        // Security Check: Only verify code if completing the job
        if (status == "completed" && inputCode != null) {
            if (booking.completionCode != inputCode) {
                _uiState.value = _uiState.value.copy(error = "Invalid Completion Code. Please ask the Client for the 4-digit code.")
                return
            }
        }

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

    fun submitDispute(reason: String) {
        val booking = _uiState.value.booking ?: return
        val uid = auth.currentUser?.uid ?: return
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val disputeRef = database.getReference("disputes").push()
                val id = disputeRef.key ?: return@launch
                
                val dispute = mapOf(
                    "id" to id,
                    "bookingId" to booking.bookingId,
                    "reporterId" to uid,
                    "reason" to reason,
                    "status" to "pending",
                    "timestamp" to System.currentTimeMillis()
                )
                disputeRef.setValue(dispute).await()
                
                // Mark booking as disputed to freeze actions
                database.getReference("bookings").child(booking.bookingId)
                    .child("status").setValue("disputed").await()
                
                // Log for Admin Website
                com.example.hustlefix.util.ActivityLogger.log(uid, "User", "DISPUTE_OPENED", "Dispute raised for booking ${booking.bookingId}")

                _uiState.value = _uiState.value.copy(isLoading = false, isUpdateSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
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
