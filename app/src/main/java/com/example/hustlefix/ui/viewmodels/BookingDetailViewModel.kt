package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.hustlefix.Booking
import com.example.hustlefix.Service
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BookingDetailUiState(
    val booking: Booking? = null,
    val service: Service? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class BookingDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BookingDetailUiState())
    val uiState: StateFlow<BookingDetailUiState> = _uiState.asStateFlow()

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    fun loadBooking(bookingId: String) {
        if (bookingId.isEmpty()) return
        _uiState.value = _uiState.value.copy(isLoading = true)

        database.getReference("bookings").child(bookingId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val booking = snapshot.getValue(Booking::class.java)
                    if (booking != null) {
                        _uiState.value = _uiState.value.copy(booking = booking)
                        fetchServiceDetails(booking.serviceId ?: "")
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Booking not found")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
                }
            })
    }

    private fun fetchServiceDetails(serviceId: String) {
        if (serviceId.isEmpty()) {
            _uiState.value = _uiState.value.copy(isLoading = false)
            return
        }
        database.getReference("services").child(serviceId).get().addOnSuccessListener { snapshot ->
            val service = snapshot.getValue(Service::class.java)
            _uiState.value = _uiState.value.copy(service = service, isLoading = false)
        }.addOnFailureListener {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun updateStatus(status: String) {
        val bookingId = _uiState.value.booking?.bookingId ?: return
        database.getReference("bookings").child(bookingId).child("status").setValue(status)
    }
}
