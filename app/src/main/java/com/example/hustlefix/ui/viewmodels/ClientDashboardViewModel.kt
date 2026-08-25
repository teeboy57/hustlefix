package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustlefix.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ClientDashboardUiState(
    val clientName: String = "",
    val totalBookings: Int = 0,
    val activeBookings: Int = 0,
    val completedBookings: Int = 0,
    val recentBookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false
)

class ClientDashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ClientDashboardUiState())
    val uiState: StateFlow<ClientDashboardUiState> = _uiState.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val currentUserId: String? = auth.currentUser?.uid

    init {
        loadData()
    }

    private fun loadData() {
        val user = auth.currentUser ?: return
        val name = user.displayName ?: user.email?.split("@")?.get(0) ?: "Client"
        _uiState.value = _uiState.value.copy(clientName = name)

        loadBookings()
    }

    private fun loadBookings() {
        val userId = currentUserId ?: return
        val bookingsRef = database.getReference("bookings")
        
        bookingsRef.orderByChild("clientId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var total = 0
                    var active = 0
                    var completed = 0
                    val list = mutableListOf<Booking>()

                    for (bookingSnapshot in snapshot.children) {
                        val booking = bookingSnapshot.getValue(Booking::class.java)
                        if (booking != null) {
                            total++
                            when (booking.status) {
                                "completed" -> completed++
                                "cancelled" -> {}
                                else -> active++
                            }
                            list.add(booking)
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        totalBookings = total,
                        activeBookings = active,
                        completedBookings = completed,
                        recentBookings = list.sortedByDescending { it.timestamp }.take(5)
                    )
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
