package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.hustlefix.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ServiceProviderDashboardUiState(
    val businessName: String = "",
    val totalEarnings: String = "R0.00",
    val totalSkills: Int = 0,
    val totalJobs: Int = 0,
    val averageRating: String = "0.0",
    val recentOrders: List<Booking> = emptyList(),
    val isLoading: Boolean = false
)

class ServiceProviderDashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ServiceProviderDashboardUiState())
    val uiState: StateFlow<ServiceProviderDashboardUiState> = _uiState.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val currentUserId: String? = auth.currentUser?.uid

    init {
        loadData()
    }

    private fun loadData() {
        val user = auth.currentUser ?: return
        val name = user.displayName ?: user.email?.split("@")?.get(0) ?: "Provider"
        _uiState.value = _uiState.value.copy(businessName = name)

        loadStats()
        loadBookings()
    }

    private fun loadStats() {
        val userId = currentUserId ?: return
        
        // Load Skills (Services) count
        database.getReference("services").orderByChild("serviceProviderId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _uiState.value = _uiState.value.copy(totalSkills = snapshot.childrenCount.toInt())
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadBookings() {
        val userId = currentUserId ?: return
        val bookingsRef = database.getReference("bookings")
        
        bookingsRef.orderByChild("serviceProviderId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalJobs = 0
                    var earnings = 0.0
                    var totalRating = 0.0
                    var ratingCount = 0
                    val list = mutableListOf<Booking>()

                    for (bookingSnapshot in snapshot.children) {
                        val booking = bookingSnapshot.getValue(Booking::class.java)
                        if (booking != null) {
                            totalJobs++
                            if (booking.status == "completed") {
                                earnings += booking.price
                            }
                            if (booking.rating > 0) {
                                totalRating += booking.rating
                                ratingCount++
                            }
                            list.add(booking)
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        totalJobs = totalJobs,
                        totalEarnings = "R${String.format("%.2f", earnings)}",
                        averageRating = if (ratingCount > 0) String.format("%.1f", totalRating / ratingCount) else "0.0",
                        recentOrders = list.sortedByDescending { it.timestamp }.take(5)
                    )
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
