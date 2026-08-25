package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.content.Context
import com.example.hustlefix.SessionHelper
import com.example.hustlefix.data.UserRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccessful: Boolean = false,
    val isRegisterSuccessful: Boolean = false
)

class AuthViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String, context: Context) {
        if (!com.example.hustlefix.util.NetworkUtils.isNetworkAvailable(context)) {
            _uiState.value = _uiState.value.copy(error = "No internet connection")
            return
        }
        
        if (email.isEmpty() || password.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Please fill in all fields")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        viewModelScope.launch {
                            try {
                                val userProfileResult = userRepository.getUserProfile(user.uid)
                                val profile = userProfileResult.getOrNull()
                                
                                if (profile == null) {
                                    auth.signOut()
                                    _uiState.value = _uiState.value.copy(isLoading = false, error = "User profile not found. Please register.")
                                    return@launch
                                }
                                
                                if (profile.isSuspended) {
                                    auth.signOut()
                                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Your account has been suspended.")
                                } else {
                                    val appRole = if (profile.role == "worker") "service_provider" else "client"
                                    SessionHelper.saveRole(context, appRole)
                                    SessionHelper.setLoggedIn(context, true)
                                    
                                    // Update FCM Token
                                    try {
                                        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                                            viewModelScope.launch {
                                                userRepository.updateFcmToken(user.uid, token)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // FCM not available, ignore for now
                                    }
                                    
                                    _uiState.value = _uiState.value.copy(isLoading = false, isLoginSuccessful = true)
                                }
                            } catch (e: Exception) {
                                auth.signOut()
                                _uiState.value = _uiState.value.copy(isLoading = false, error = "Login failed: ${e.message}")
                            }
                        }
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, 
                        error = task.exception?.message ?: "Login failed"
                    )
                }
            }
    }

    fun register(fullName: String, email: String, phone: String, pass: String, role: String, context: Context) {
        if (email.isEmpty() || pass.isEmpty() || fullName.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Please fill in all fields")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val firebaseRole = if (role == "service_provider") "worker" else "client"
                        val userMap = mutableMapOf<String, Any>(
                            "name" to fullName,
                            "email" to email,
                            "phone" to phone,
                            "role" to firebaseRole,
                            "isVerified" to false,
                            "isSuspended" to false,
                            "createdAt" to System.currentTimeMillis()
                        )
                        
                        viewModelScope.launch {
                            val result = userRepository.saveUserProfile(user.uid, userMap)
                            if (result.isSuccess) {
                                SessionHelper.saveRole(context, role)
                                SessionHelper.setLoggedIn(context, true)
                                _uiState.value = _uiState.value.copy(isLoading = false, isRegisterSuccessful = true)
                            } else {
                                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to save profile")
                            }
                        }
                    }
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
