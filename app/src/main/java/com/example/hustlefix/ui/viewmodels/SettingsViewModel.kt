package com.example.hustlefix.ui.viewmodels

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustlefix.SessionHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class SettingsUiState(
    val userName: String = "",
    val userEmail: String = "",
    val isNotificationsEnabled: Boolean = true,
    val isDarkModeEnabled: Boolean = false,
    val isLoggedOut: Boolean = false,
    val isAccountDeleted: Boolean = false,
    val error: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val prefs = SessionHelper.prefs(application)

    init {
        loadUser()
        loadPrefs()
    }

    private fun loadUser() {
        val user = auth.currentUser
        _uiState.value = _uiState.value.copy(
            userName = user?.displayName ?: "User",
            userEmail = user?.email ?: ""
        )
    }

    private fun loadPrefs() {
        val darkMode = prefs.getBoolean("dark_mode_enabled", false)
        val notifications = prefs.getBoolean("notifications_enabled", true)
        _uiState.value = _uiState.value.copy(
            isDarkModeEnabled = darkMode,
            isNotificationsEnabled = notifications
        )
    }

    fun toggleDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode_enabled", enabled).apply()
        _uiState.value = _uiState.value.copy(isDarkModeEnabled = enabled)
        
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
        _uiState.value = _uiState.value.copy(isNotificationsEnabled = enabled)
    }

    fun logout() {
        auth.signOut()
        _uiState.value = _uiState.value.copy(isLoggedOut = true)
    }

    fun deleteAccount() {
        val user = auth.currentUser ?: return
        val uid = user.uid

        viewModelScope.launch {
            try {
                // 1. Delete user data from database
                database.getReference("users").child(uid).removeValue().await()
                
                // 2. Delete user's services if provider
                val servicesSnapshot = database.getReference("services")
                    .orderByChild("serviceProviderId").equalTo(uid).get().await()
                for (ds in servicesSnapshot.children) {
                    ds.ref.removeValue().await()
                }

                // 3. Delete from Auth
                user.delete().await()
                
                _uiState.value = _uiState.value.copy(isAccountDeleted = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Delete failed: ${e.message}. Please re-authenticate and try again.")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
