package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.hustlefix.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

data class AnalyticsUiState(
    val stats: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false
)

class AnalyticsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    init {
        loadData()
    }

    private fun loadData() {
        val uid = userId ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)

        database.getReference("bookings").orderByChild("workerId").equalTo(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bookings = snapshot.children.mapNotNull { it.getValue(Booking::class.java) }
                    
                    val totalBookings = bookings.size
                    val completed = bookings.count { it.status?.lowercase() == "completed" }
                    val pending = bookings.count { it.status?.lowercase() == "pending" }
                    val cancelled = bookings.count { it.status?.lowercase() == "cancelled" }
                    val totalRevenue = bookings.filter { it.status?.lowercase() == "completed" }.sumOf { it.getAmount() }
                    
                    // In a real app, you'd calculate these accurately from timestamps
                    val monthlyRevenue = totalRevenue 
                    val weeklyBookings = totalBookings
                    
                    val stats = mapOf(
                        "totalBookings" to totalBookings.toString(),
                        "completed" to completed.toString(),
                        "pending" to pending.toString(),
                        "cancelled" to cancelled.toString(),
                        "totalRevenue" to String.format(Locale.getDefault(), "R%.2f", totalRevenue),
                        "avgRating" to String.format(Locale.getDefault(), "%.1f", 0.0), // Need Rating data model update
                        "totalClients" to bookings.mapNotNull { it.getClientId() }.distinct().size.toString(),
                        "monthlyRevenue" to String.format(Locale.getDefault(), "R%.2f", monthlyRevenue),
                        "weeklyBookings" to weeklyBookings.toString()
                    )
                    
                    _uiState.value = _uiState.value.copy(stats = stats, isLoading = false)
                }
                override fun onCancelled(error: DatabaseError) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            })
    }
}
