package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.hustlefix.AppNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationUiState(
    val notifications: List<AppNotification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false
)

class NotificationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        val uid = userId ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)

        database.getReference("notifications").child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(AppNotification::class.java) }
                    val sorted = list.sortedByDescending { it.timestamp }
                    _uiState.value = _uiState.value.copy(
                        notifications = sorted,
                        unreadCount = sorted.count { !it.isRead },
                        isLoading = false
                    )
                }
                override fun onCancelled(error: DatabaseError) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            })
    }

    fun markAsRead(notificationId: String) {
        val uid = userId ?: return
        database.getReference("notifications").child(uid).child(notificationId).child("read").setValue(true)
    }

    fun markAllAsRead() {
        val uid = userId ?: return
        _uiState.value.notifications.filter { !it.isRead }.forEach {
            database.getReference("notifications").child(uid).child(it.id).child("read").setValue(true)
        }
    }

    fun deleteNotification(notificationId: String) {
        val uid = userId ?: return
        database.getReference("notifications").child(uid).child(notificationId).removeValue()
    }
}
