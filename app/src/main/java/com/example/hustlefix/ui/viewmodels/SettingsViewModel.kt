package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsUiState(
    val userName: String = "",
    val userEmail: String = "",
    val isLoggedOut: Boolean = false
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    init {
        loadUser()
    }

    private fun loadUser() {
        val user = auth.currentUser
        _uiState.value = _uiState.value.copy(
            userName = user?.displayName ?: "User",
            userEmail = user?.email ?: ""
        )
    }

    fun logout() {
        auth.signOut()
        _uiState.value = _uiState.value.copy(isLoggedOut = true)
    }
}
