package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustlefix.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ServiceProviderDashboardUiState(
    val businessName: String = "",
    val totalEarnings: String = "R0.00",
    val totalSkills: Int = 0,
    val totalJobs: Int = 0,
    val averageRating: String = "0.0",
    val recentOrders: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false
)

class ServiceProviderDashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ServiceProviderDashboardUiState())
    val uiState: StateFlow<ServiceProviderDashboardUiState> = _uiState.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val currentUserId: String? = auth.currentUser?.uid

    private var skillsQuery: Query? = null
    private var skillsListener: ValueEventListener? = null
    private var bookingsQuery: Query? = null
    private var bookingsListener: ValueEventListener? = null

    init {
        loadData()
    }

    private fun loadData() {
        val uid = currentUserId ?: return
        
        // Load business name from database
        database.getReference("users").child(uid).child("name").get().addOnSuccessListener { snapshot ->
            val name = snapshot.getValue(String::class.java) ?: auth.currentUser?.displayName ?: "Provider"
            _uiState.value = _uiState.value.copy(businessName = name)
        }

        loadStats()
        loadBookings()
    }

    private fun loadStats() {
        val userId = currentUserId ?: return
        
        skillsListener?.let { skillsQuery?.removeEventListener(it) }
        
        skillsQuery = database.getReference("services").orderByChild("serviceProviderId").equalTo(userId)
        skillsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _uiState.value = _uiState.value.copy(totalSkills = snapshot.childrenCount.toInt())
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        skillsListener?.let { skillsQuery?.addValueEventListener(it) }
    }

    private fun loadBookings() {
        val userId = currentUserId ?: return
        
        bookingsListener?.let { bookingsQuery?.removeEventListener(it) }
        
        bookingsQuery = database.getReference("bookings").orderByChild("workerId").equalTo(userId)
        bookingsListener = object : ValueEventListener {
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
                            earnings += booking.getAmount() ?: 0.0
                        }
                        if ((booking.getRating() ?: 0.0) > 0) {
                            totalRating += booking.getRating() ?: 0.0
                            ratingCount++
                        }
                        list.add(booking)
                    }
                }

                _uiState.value = _uiState.value.copy(
                    totalJobs = totalJobs,
                    totalEarnings = "R${String.format(java.util.Locale.getDefault(), "%.2f", earnings)}",
                    averageRating = if (ratingCount > 0) String.format(java.util.Locale.getDefault(), "%.1f", totalRating / ratingCount) else "0.0",
                    recentOrders = list.sortedByDescending { it.getTimestamp() ?: 0L }.take(5),
                    isRefreshing = false
                )
            }

            override fun onCancelled(error: DatabaseError) {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
        bookingsListener?.let { bookingsQuery?.addValueEventListener(it) }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadData()
        viewModelScope.launch {
            delay(3000)
            if (_uiState.value.isRefreshing) {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        skillsListener?.let { skillsQuery?.removeEventListener(it) }
        bookingsListener?.let { bookingsQuery?.removeEventListener(it) }
    }
}
