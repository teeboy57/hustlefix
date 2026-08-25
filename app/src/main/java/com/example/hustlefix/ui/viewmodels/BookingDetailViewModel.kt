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

data class BookingDetailUiState(
    val booking: Booking? = null,
    val service: Service? = null,
    val isLoading: Boolean = false,
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

        // Clear existing listener
        bookingRef?.let { ref -> bookingListener?.let { ref.removeEventListener(it) } }

        bookingRef = database.getReference("bookings").child(bookingId)
        bookingListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val booking = snapshot.getValue(Booking::class.java)?.apply {
                    setBookingId(snapshot.key ?: "")
                }
                if (booking != null) {
                    _uiState.value = _uiState.value.copy(booking = booking)
                    val sid = booking.getJobId()
                    if (!sid.isNullOrEmpty() && booking.jobId.isNullOrEmpty()) {
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

    private fun fetchServiceDetails(serviceId: String) {
        database.getReference("services").child(serviceId).get().addOnSuccessListener { snapshot ->
            val service = snapshot.getValue(Service::class.java)
            _uiState.value = _uiState.value.copy(service = service, isLoading = false)
        }.addOnFailureListener {
            _uiState.value = _uiState.value.copy(isLoading = false)
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
