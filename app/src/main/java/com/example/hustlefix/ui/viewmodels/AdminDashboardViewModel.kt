package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

data class AdminDashboardUiState(
    val profitBalance: String = "R0.00",
    val totalUsers: Int = 0,
    val totalJobs: Int = 0,
    val pendingVerifications: Int = 0,
    val activeEmergencies: Int = 0,
    val isLoading: Boolean = false
)

class AdminDashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    private val database = FirebaseDatabase.getInstance()

    init {
        loadAdminData()
    }

    private fun loadAdminData() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        // 1. Load Profit Balance
        database.getReference("admin_wallet").child("balance").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val balance = snapshot.getValue(Double::class.java) ?: 0.0
                _uiState.value = _uiState.value.copy(
                    profitBalance = String.format(Locale.getDefault(), "R%.2f", balance)
                )
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 2. Count Users
        database.getReference("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var pending = 0
                for (user in snapshot.children) {
                    if (user.child("verificationStatus").getValue(String::class.java) == "pending") {
                        pending++
                    }
                }
                _uiState.value = _uiState.value.copy(
                    totalUsers = snapshot.childrenCount.toInt(),
                    pendingVerifications = pending
                )
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 3. Count Jobs
        database.getReference("jobs").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _uiState.value = _uiState.value.copy(totalJobs = snapshot.childrenCount.toInt())
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 4. Active Emergencies
        database.getReference("emergency_requests").orderByChild("status").equalTo("pending").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _uiState.value = _uiState.value.copy(
                    activeEmergencies = snapshot.childrenCount.toInt(),
                    isLoading = false
                )
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
