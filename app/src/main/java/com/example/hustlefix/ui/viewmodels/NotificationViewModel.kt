package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustlefix.AppNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<AppNotification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false
)

class NotificationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid
    
    private var notifRef: DatabaseReference? = null
    private var notifListener: ValueEventListener? = null
    
    private var broadcastRef: DatabaseReference? = null
    private var broadcastListener: ValueEventListener? = null

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        val uid = userId ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)

        // 1. Listen to User-Specific Notifications
        notifListener?.let { notifRef?.removeEventListener(it) }
        notifRef = database.getReference("notifications").child(uid)
        notifListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val personalList = snapshot.children.mapNotNull { it.getValue(AppNotification::class.java) }
                mergeAndSort(personalList, emptyList()) // Broadcasts handled separately below
            }
            override fun onCancelled(error: DatabaseError) {
                _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false)
            }
        }
        notifListener?.let { notifRef?.addValueEventListener(it) }

        // 2. Listen to Global Broadcasts
        broadcastListener?.let { broadcastRef?.removeEventListener(it) }
        broadcastRef = database.getReference("broadcasts")
        broadcastListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val broadcasts = snapshot.children.mapNotNull { ds ->
                    ds.getValue(AppNotification::class.java)?.apply {
                        this.id = ds.key
                        this.type = "system_broadcast"
                    }
                }
                database.getReference("notifications").child(uid).get().addOnSuccessListener { personalSnapshot ->
                    val personal = personalSnapshot.children.mapNotNull { it.getValue(AppNotification::class.java) }
                    mergeAndSort(personal, broadcasts)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
        broadcastListener?.let { broadcastRef?.addValueEventListener(it) }
    }

    private fun mergeAndSort(personal: List<AppNotification>, broadcasts: List<AppNotification>) {
        val all = (personal + broadcasts).sortedByDescending { it.timestamp }
        _uiState.value = _uiState.value.copy(
            notifications = all,
            unreadCount = all.count { !it.isRead },
            isLoading = false,
            isRefreshing = false
        )
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadNotifications()
        viewModelScope.launch {
            delay(3000)
            if (_uiState.value.isRefreshing) {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    fun markAsRead(notificationId: String) {
        val uid = userId ?: return
        database.getReference("notifications").child(uid).child(notificationId).child("read").setValue(true)
    }

    fun markAllAsRead() {
        val uid = userId ?: return
        _uiState.value.notifications.filter { !it.isRead }.forEach {
            if (it.type != "system_broadcast") {
                database.getReference("notifications").child(uid).child(it.id).child("read").setValue(true)
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        val uid = userId ?: return
        database.getReference("notifications").child(uid).child(notificationId).removeValue()
    }

    override fun onCleared() {
        super.onCleared()
        notifListener?.let { notifRef?.removeEventListener(it) }
        broadcastListener?.let { broadcastRef?.removeEventListener(it) }
    }
}
