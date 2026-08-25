package com.example.hustlefix.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustlefix.Booking
import com.example.hustlefix.SessionHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MyBookingsUiState(
    val bookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false
)

class MyBookingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(MyBookingsUiState())
    val uiState: StateFlow<MyBookingsUiState> = _uiState.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val currentUserId: String? = auth.currentUser?.uid
    private val prefs = SessionHelper.prefs(application)
    
    private var bookingsQuery: Query? = null
    private var bookingsListener: ValueEventListener? = null

    init {
        loadBookings()
    }

    private fun loadBookings() {
        val userId = currentUserId ?: return
        val role = prefs.getString("userRole", "client")
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        bookingsListener?.let { bookingsQuery?.removeEventListener(it) }
        
        val ref = database.getReference("bookings")
        bookingsQuery = if (role == "service_provider") {
            ref.orderByChild("workerId").equalTo(userId)
        } else {
            ref.orderByChild("clientId").equalTo(userId)
        }
        
        bookingsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Booking>()
                for (bookingSnapshot in snapshot.children) {
                    val booking = bookingSnapshot.getValue(Booking::class.java)
                    if (booking != null) {
                        list.add(booking)
                    }
                }
                _uiState.value = _uiState.value.copy(
                    bookings = list.sortedByDescending { it.timestamp },
                    isLoading = false,
                    isRefreshing = false
                )
            }
            override fun onCancelled(error: DatabaseError) {
                _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false)
            }
        }
        bookingsListener?.let { bookingsQuery?.addValueEventListener(it) }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadBookings()
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
