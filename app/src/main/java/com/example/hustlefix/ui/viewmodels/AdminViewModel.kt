package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.hustlefix.Booking
import com.example.hustlefix.User
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

data class AdminUiState(
    val totalUsers: Int = 0,
    val totalWorkers: Int = 0,
    val totalEscrow: String = "R0.00",
    val recentBookings: List<Booking> = emptyList(),
    val pendingVerifications: List<User> = emptyList(),
    val isLoading: Boolean = false
)

class AdminViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        // 1. Load All Users & Workers
        database.getReference("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allUsers = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                val workers = allUsers.filter { it.role == "worker" }
                val unverified = workers.filter { !it.isAvailable } // Assuming 'available' or a new 'isVerified' field
                
                _uiState.value = _uiState.value.copy(
                    totalUsers = allUsers.size,
                    totalWorkers = workers.size,
                    pendingVerifications = unverified
                )
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 2. Load All Bookings & Calculate Escrow
        database.getReference("bookings").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookings = snapshot.children.mapNotNull { it.getValue(Booking::class.java) }
                val escrowSum = bookings.filter { it.paymentStatus == "HELD_BY_ADMIN" || it.paymentStatus == "ESCROW" }
                    .sumOf { it.price }
                
                _uiState.value = _uiState.value.copy(
                    totalEscrow = String.format(Locale.getDefault(), "R%.2f", escrowSum),
                    recentBookings = bookings.sortedByDescending { it.timestamp }.take(10),
                    isLoading = false
                )
            }
            override fun onCancelled(error: DatabaseError) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        })
    }

    fun verifyWorker(userId: String) {
        // Logic to verify a service provider (give them the badge)
        database.getReference("users").child(userId).child("available").setValue(true)
    }

    fun releaseEscrow(bookingId: String) {
        // Force release funds in case of dispute resolution
        database.getReference("bookings").child(bookingId).child("paymentStatus").setValue("RELEASED")
    }
}
