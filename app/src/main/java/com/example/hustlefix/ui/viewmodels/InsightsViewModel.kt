package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class InsightsUiState(
    val stats: Map<String, String> = mapOf(
        "responseTime" to "12 mins",
        "successRate" to "99%",
        "profileViews" to "1.2k",
        "satisfaction" to "5.0/5"
    ),
    val isLoading: Boolean = false
)

class InsightsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val userId = auth.currentUser?.uid

    init {
        // Mock data for now, in production fetch from Firebase
    }
}
