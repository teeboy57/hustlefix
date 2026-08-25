package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.content.Context
import com.example.hustlefix.SessionHelper

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccessful: Boolean = false,
    val isRegisterSuccessful: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun login(email: String, password: String, context: Context) {
        if (email.isEmpty() || password.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Please fill in all fields")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    SessionHelper.setLoggedIn(context, true)
                    _uiState.value = _uiState.value.copy(isLoading = false, isLoginSuccessful = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, 
                        error = task.exception?.message ?: "Login failed"
                    )
                }
            }
    }

    fun register(fullName: String, email: String, phone: String, pass: String, context: Context) {
        if (email.isEmpty() || pass.isEmpty() || fullName.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Please fill in all fields")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    SessionHelper.setLoggedIn(context, true)
                    // In a real app, you'd save the extra user data to Realtime Database here
                    _uiState.value = _uiState.value.copy(isLoading = false, isRegisterSuccessful = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, 
                        error = task.exception?.message ?: "Registration failed"
                    )
                }
            }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
