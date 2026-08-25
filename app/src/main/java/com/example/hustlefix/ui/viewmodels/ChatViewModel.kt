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
        listRef = database.getReference("messages")
        listListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatMap = mutableMapOf<String, ChatSummary>()
                for (chatSnapshot in snapshot.children) {
                    val chatId = chatSnapshot.key ?: continue
                    if (!chatId.contains(uid)) continue

                    val messages = mutableListOf<Message>()
                    for (msgSnapshot in chatSnapshot.children) {
                        msgSnapshot.getValue(Message::class.java)?.let { messages.add(it) }
                    }

                    if (messages.isNotEmpty()) {
                        val lastMsg = messages.maxByOrNull { it.timestamp }
                        if (lastMsg != null) {
                            val partnerId = if (lastMsg.senderId == uid) lastMsg.receiverId else lastMsg.senderId
                            val partnerName = if (lastMsg.senderId == uid) lastMsg.receiverName else lastMsg.senderName

                            val summary = ChatSummary().apply {
                                this.chatId = chatId
                                this.partnerId = partnerId
                                this.partnerName = partnerName
                                this.lastMessage = lastMsg.messageText
                                this.lastTimestamp = lastMsg.timestamp ?: 0L
                            }
                            chatMap[chatId] = summary
                            fetchPartnerProfile(summary)
                        }
                    }
                }
                _listUiState.value = _listUiState.value.copy(
                    chats = chatMap.values.sortedByDescending { it.lastTimestamp ?: 0L },
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
    }

    override fun onCleared() {
        super.onCleared()
        listRef?.let { ref -> listListener?.let { ref.removeEventListener(it) } }
        chatRef?.let { ref -> chatListener?.let { ref.removeEventListener(it) } }
    }
}
