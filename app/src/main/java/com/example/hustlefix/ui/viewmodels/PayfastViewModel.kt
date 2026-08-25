package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustlefix.Booking
import com.example.hustlefix.User
import com.example.hustlefix.data.PayfastRequest
import com.example.hustlefix.data.PayfastRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class PayfastUiState(
    val checkoutUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPaymentSuccess: Boolean = false
)

class PayfastViewModel(private val repository: PayfastRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PayfastUiState())
    val uiState: StateFlow<PayfastUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    fun initiatePayment(booking: Booking) {
        val userId = auth.currentUser?.uid ?: return
        
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val userSnapshot = database.getReference("users").child(userId).get().await()
                val user = userSnapshot.getValue(User::class.java) ?: throw Exception("User profile not found")
                
                val fullName = user.name ?: "User"
                val firstName = fullName.split(" ").firstOrNull() ?: "User"
                val lastName = if (fullName.contains(" ")) fullName.split(" ").last() else ""
                
                val request = PayfastRequest(
                    merchantId = "", // Handled by backend
                    merchantKey = "", // Handled by backend
                    returnUrl = "hustlefix://payment-success",
                    cancelUrl = "hustlefix://payment-cancel",
                    notifyUrl = "https://hustlefix.onrender.com/api/payments/payfast-itn",
                    firstName = firstName,
                    lastName = lastName,
                    email = user.email ?: "",
                    mPaymentId = booking.bookingId,
                    amount = String.format(java.util.Locale.getDefault(), "%.2f", booking.amount),
                    itemName = "Payment for Job: ${booking.jobId}"
                )
                
                val result = repository.getCheckoutUrl(request)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(checkoutUrl = result.getOrNull(), isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.message, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun onPaymentSuccess() {
        _uiState.value = _uiState.value.copy(isPaymentSuccess = true, checkoutUrl = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
