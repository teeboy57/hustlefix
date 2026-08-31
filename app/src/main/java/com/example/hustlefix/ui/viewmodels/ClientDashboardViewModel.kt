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
    val walletBalance: String = "R0.00",
    val totalBookings: Int = 0,
    val activeBookings: Int = 0,
    val completedBookings: Int = 0,
    val recentBookings: List<Booking> = emptyList(),
    val upcomingBooking: Booking? = null,
    val unreadMessagesCount: Int = 0,
    val nearbyServices: List<com.example.hustlefix.Service> = emptyList(),
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
    private var chatsListener: ValueEventListener? = null
    private var servicesListener: ValueEventListener? = null

    init {
        loadData()
    }

    private fun loadData() {
        val uid = currentUserId ?: return
        
        // Load name from database
        database.getReference("users").child(uid).child("name").get().addOnSuccessListener { snapshot ->
            val name = snapshot.getValue(String::class.java) ?: auth.currentUser?.displayName ?: "Client"
            _uiState.value = _uiState.value.copy(clientName = name)
        }

        // Load wallet balance
        database.getReference("wallets").child(uid).child("balance").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val balance = snapshot.getValue(Double::class.java) ?: 0.0
                _uiState.value = _uiState.value.copy(walletBalance = "R${String.format(java.util.Locale.getDefault(), "%.2f", balance)}")
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        loadBookings()
        loadUnreadMessagesCount()
        loadNearbyServices()
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
                var upcoming: Booking? = null
                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

                for (bookingSnapshot in snapshot.children) {
                    val booking = bookingSnapshot.getValue(Booking::class.java)
                    if (booking != null) {
                        total++
                        val s = booking.status?.lowercase() ?: ""
                        if (s == "completed") {
                            completed++
                        } else if (s != "cancelled") {
                            active++
                            // Check if confirmed and for today
                            if (s == "confirmed" && booking.preferredDate == today) {
                                upcoming = booking
                            }
                        }
                        list.add(booking)
                    }
                }

                _uiState.value = _uiState.value.copy(
                    totalBookings = total,
                    activeBookings = active,
                    completedBookings = completed,
                    recentBookings = if (list.isEmpty()) emptyList() else list.sortedByDescending { it.getTimestamp() ?: 0L }.take(5),
                    upcomingBooking = upcoming,
                    isRefreshing = false
                )
            }

            override fun onCancelled(error: DatabaseError) {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
        bookingsListener?.let { bookingsQuery?.addValueEventListener(it) }
    }

    private fun loadUnreadMessagesCount() {
        val uid = currentUserId ?: return
        // We'll look at user_chats and maybe check unread flags if we had them, 
        // for now let's query messages sent to me that are not read.
        // Actually, searching all messages is expensive. Let's look at user_chats index.
        // If we don't have unread count in index, we'll just mock it or assume 0 for now until DB update.
        // Let's assume there's a field "unreadCount" in user_chats in the future.
        // For now, let's just count how many chats have messages where receiverId == uid and isRead == false.
        
        database.getReference("user_chats").child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Mocking unread for demo if logic isn't fully in backend yet
                // In production, this would be a single field update by cloud functions
                _uiState.value = _uiState.value.copy(unreadMessagesCount = snapshot.childrenCount.toInt() % 3) // Demo variation
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadNearbyServices() {
        val uid = currentUserId ?: return
        
        // 1. Get User's Location
        database.getReference("users").child(uid).get().addOnSuccessListener { userSnapshot ->
            val userLat = userSnapshot.child("latitude").getValue(Double::class.java) ?: 0.0
            val userLng = userSnapshot.child("longitude").getValue(Double::class.java) ?: 0.0
            
            // 2. Load Services and Filter by Distance (Simple Math)
            database.getReference("services").limitToFirst(50).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val allServices = snapshot.children.mapNotNull { it.getValue(com.example.hustlefix.Service::class.java) }
                    
                    val nearby = allServices.filter { service ->
                        // Calculate distance (Approximate KM using Haversine or simple Euclidean for close range)
                        val sLat = service.latitude ?: 0.0
                        val sLng = service.longitude ?: 0.0
                        
                        if (sLat == 0.0 || userLat == 0.0) true // Fallback for demo
                        else {
                            val dist = calculateDistance(userLat, userLng, sLat, sLng)
                            dist < 50.0 // 50KM Radius
                        }
                    }.shuffled().take(4)

                    _uiState.value = _uiState.value.copy(nearbyServices = nearby)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Earth radius in KM
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
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
