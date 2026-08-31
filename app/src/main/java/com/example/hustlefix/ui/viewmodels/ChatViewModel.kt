package com.example.hustlefix.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.hustlefix.ChatSummary
import com.example.hustlefix.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

data class ChatListUiState(
    val chats: List<ChatSummary> = emptyList(),
    val isLoading: Boolean = false
)

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val partnerName: String = "Chat",
    val isLoading: Boolean = false
)

class ChatViewModel : ViewModel() {
    private val _listUiState = MutableStateFlow(ChatListUiState())
    val listUiState: StateFlow<ChatListUiState> = _listUiState.asStateFlow()

    private val _chatUiState = MutableStateFlow(ChatUiState())
    val chatUiState: StateFlow<ChatUiState> = _chatUiState.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val currentUserId: String? = auth.currentUser?.uid

    private var listRef: DatabaseReference? = null
    private var listListener: ValueEventListener? = null
    
    private var chatRef: DatabaseReference? = null
    private var chatListener: ValueEventListener? = null

    fun loadChatList() {
        val uid = currentUserId ?: return
        _listUiState.value = _listUiState.value.copy(isLoading = true)

        listRef?.let { ref -> listListener?.let { ref.removeEventListener(it) } }
        listRef = database.getReference("user_chats").child(uid)
        listListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chats = mutableListOf<ChatSummary>()
                for (chatSnapshot in snapshot.children) {
                    val summary = chatSnapshot.getValue(ChatSummary::class.java)
                    if (summary != null) {
                        chats.add(summary)
                        fetchPartnerProfile(summary)
                    }
                }
                _listUiState.value = _listUiState.value.copy(
                    chats = chats.sortedByDescending { it.lastTimestamp ?: 0L },
                    isLoading = false
                )
            }
            override fun onCancelled(error: DatabaseError) {
                _listUiState.value = _listUiState.value.copy(isLoading = false)
            }
        }
        listListener?.let { listRef?.addValueEventListener(it) }
    }

    private fun fetchPartnerProfile(summary: ChatSummary) {
        summary.partnerId?.let { pid ->
            database.getReference("users").child(pid).child("profileImage").get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        summary.partnerProfileUrl = snapshot.getValue(String::class.java)
                        _listUiState.value = _listUiState.value.copy(
                            chats = _listUiState.value.chats.map { if (it.chatId == summary.chatId) summary else it }
                        )
                    }
                }
        }
    }

    fun loadChat(partnerId: String, partnerName: String) {
        val uid = currentUserId ?: return
        val chatId = if (uid < partnerId) "${uid}_$partnerId" else "${partnerId}_$uid"
        
        _chatUiState.value = _chatUiState.value.copy(partnerName = partnerName, isLoading = true)
        
        chatRef?.let { ref -> chatListener?.let { ref.removeEventListener(it) } }
        chatRef = database.getReference("messages").child(chatId)
        
        chatListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Message>()
                for (ds in snapshot.children) {
                    ds.getValue(Message::class.java)?.let { list.add(it) }
                }
                _chatUiState.value = _chatUiState.value.copy(
                    messages = list.sortedBy { it.getTimestamp() ?: 0L },
                    isLoading = false
                )
            }
            override fun onCancelled(error: DatabaseError) {
                _chatUiState.value = _chatUiState.value.copy(isLoading = false)
            }
        }
        chatListener?.let { chatRef?.addValueEventListener(it) }
    }

    fun sendMessage(partnerId: String, partnerName: String, text: String) {
        if (text.trim().isEmpty()) return
        val uid = currentUserId ?: return
        val user = auth.currentUser ?: return
        val senderName = user.displayName ?: "User"
        
        val chatId = if (uid < partnerId) "${uid}_$partnerId" else "${partnerId}_$uid"
        val msgRef = database.getReference("messages").child(chatId)
        val msgId = msgRef.push().key ?: return
        
        val message = Message(msgId, uid, senderName, partnerId, partnerName, text)
        msgRef.child(msgId).setValue(message)

        // Update Index for both users
        val summaryForMe = ChatSummary(chatId, partnerId, partnerName, text, message.timestamp)
        val summaryForPartner = ChatSummary(chatId, uid, senderName, text, message.timestamp)

        database.getReference("user_chats").child(uid).child(chatId).setValue(summaryForMe)
        database.getReference("user_chats").child(partnerId).child(chatId).setValue(summaryForPartner)
    }

    fun editMessage(partnerId: String, messageId: String, newText: String) {
        if (newText.trim().isEmpty()) return
        val uid = currentUserId ?: return
        val chatId = if (uid < partnerId) "${uid}_$partnerId" else "${partnerId}_$uid"
        
        val updates = mapOf(
            "messageText" to newText,
            "edited" to true
        )
        database.getReference("messages").child(chatId).child(messageId).updateChildren(updates)
        
        // Update index if it's the last message (checking by timestamp/logic is complex, so we update anyway if it matches text)
        // For performance, we'll check the current index first
        database.getReference("user_chats").child(uid).child(chatId).child("lastMessage").get().addOnSuccessListener { snapshot ->
            // If the index's last message is what we're editing (simplified check), update it
            // Actually, we should just update it to be safe.
            val summaryUpdates = mapOf("lastMessage" to newText)
            database.getReference("user_chats").child(uid).child(chatId).updateChildren(summaryUpdates)
            database.getReference("user_chats").child(partnerId).child(chatId).updateChildren(summaryUpdates)
        }
    }

    fun deleteMessage(partnerId: String, messageId: String) {
        val uid = currentUserId ?: return
        val chatId = if (uid < partnerId) "${uid}_$partnerId" else "${partnerId}_$uid"
        
        val updates = mapOf(
            "messageText" to "This message was deleted",
            "deleted" to true
        )
        database.getReference("messages").child(chatId).child(messageId).updateChildren(updates)

        val summaryUpdates = mapOf("lastMessage" to "This message was deleted")
        database.getReference("user_chats").child(uid).child(chatId).updateChildren(summaryUpdates)
        database.getReference("user_chats").child(partnerId).child(chatId).updateChildren(summaryUpdates)
    }

    fun sendSystemMessage(partnerId: String, partnerName: String, text: String) {
        val uid = currentUserId ?: return
        val chatId = if (uid < partnerId) "${uid}_$partnerId" else "${partnerId}_$uid"
        
        database.getReference("messages").child(chatId).get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                sendMessage(partnerId, partnerName, text)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listRef?.let { ref -> listListener?.let { ref.removeEventListener(it) } }
        chatRef?.let { ref -> chatListener?.let { ref.removeEventListener(it) } }
    }
}
