package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PaymentMethod(
    val id: String,
    val type: String,
    val number: String,
    val expiry: String
)

data class PaymentMethodsUiState(
    val methods: List<PaymentMethod> = listOf(
        PaymentMethod("1", "Visa", "**** 4242", "12/26"),
        PaymentMethod("2", "Mastercard", "**** 8888", "05/25")
    ),
    val isLoading: Boolean = false
)

class PaymentMethodsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PaymentMethodsUiState())
    val uiState: StateFlow<PaymentMethodsUiState> = _uiState.asStateFlow()

    fun addMethod(type: String, number: String, expiry: String) {
        val newMethod = PaymentMethod(
            id = System.currentTimeMillis().toString(),
            type = type,
            number = "**** " + number.takeLast(4),
            expiry = expiry
        )
        _uiState.value = _uiState.value.copy(
            methods = _uiState.value.methods + newMethod
        )
    }

    fun removeMethod(id: String) {
        _uiState.value = _uiState.value.copy(
            methods = _uiState.value.methods.filter { it.id != id }
        )
    }
}
