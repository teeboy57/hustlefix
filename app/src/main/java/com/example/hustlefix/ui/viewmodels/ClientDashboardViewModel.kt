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

data class ClientDashboardUiState(
    val clientName: String = "",
    val totalBookings: Int = 0,
    val activeBookings: Int = 0,
    val completedBookings: Int = 0,
    val recentBookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false
)

class ClientDashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ClientDashboardUiState())
    val uiState: StateFlow<ClientDashboardUiState> = _uiState.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val currentUserId: String? = auth.currentUser?.uid
    
    private var bookingsQuery: Query? = null
    private var bookingsListener: ValueEventListener? = null

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
        
        bookingsListener?.let { bookingsQuery?.removeEventListener(it) }
        
        bookingsQuery = database.getReference("bookings").orderByChild("clientId").equalTo(userId)
        bookingsListener = object : ValueEventListener {
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
                    recentBookings = if (list.isEmpty()) emptyList() else list.sortedByDescending { it.getTimestamp() ?: 0L }.take(5),
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
        // Safety timeout for refreshing state
        viewModelScope.launch {
            delay(3000)
            if (_uiState.value.isRefreshing) {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        bookingsListener?.let { bookingsQuery?.removeEventListener(it) }
    }
}
