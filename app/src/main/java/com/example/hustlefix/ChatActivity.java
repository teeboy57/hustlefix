package com.example.hustlefix;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton, emojiButton;
    private List<ChatMessage> messageList;
    private MessageAdapter adapter;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference chatRef;
    private DatabaseReference messagesRef;
    private ValueEventListener messagesListener;
    private String chatRoomId;
    private String otherUserId;
    private String otherUserName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        otherUserId = ChatLauncher.resolveOtherUserId(getIntent());
        otherUserName = ChatLauncher.resolveOtherUserName(getIntent());
        if (otherUserId == null) {
            ChatLauncher.openChatList(this);
            finish();
            return;
        }
        initViews();
        setupToolbar();
        setupFirebase();
        createChatRoom();
        setupRecyclerView();
        setupClickListeners();
        loadMessages();
        markMessagesAsRead();
    }
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        emojiButton = findViewById(R.id.emojiButton);
        messageList = new ArrayList<>();
    }
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(otherUserName != null ? otherUserName : "Chat");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void createChatRoom() {
        if (currentUser != null && otherUserId != null) {
            chatRoomId = ChatLauncher.buildChatRoomId(currentUser.getUid(), otherUserId);
            chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatRoomId);
            messagesRef = chatRef.child("messages");
            String myName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User";
            ChatLauncher.ensureChatRoomIndexed(
                    chatRoomId,
                    currentUser.getUid(),
                    myName,
                    otherUserId,
                    otherUserName != null ? otherUserName : "User"
            );
        }
    }
    private void setupRecyclerView() {
        adapter = new MessageAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    private void setupClickListeners() {
        emojiButton.setOnClickListener(v -> showEmojiPicker());
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
            }
        });
        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTypingStatus(true);
                new Handler().postDelayed(() -> updateTypingStatus(false), 1500);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    private void updateTypingStatus(boolean isTyping) {
        if (chatRef != null && currentUser != null) {
            chatRef.child("typing").child(currentUser.getUid()).setValue(isTyping);
        }
    }
    private void showEmojiPicker() {
        String[] emojis = {"ðŸ˜Š", "ðŸ˜‚", "â¤ï¸", "ðŸ‘", "ðŸŽ‰", "ðŸ”¥", "âœ¨", "ðŸ’ª", "ðŸ‘‹", "ðŸ™"};
        new AlertDialog.Builder(this)
                .setTitle("Select Emoji")
                .setItems(emojis, (dialog, which) -> {
                    int cursorPos = messageInput.getSelectionStart();
                    String currentText = messageInput.getText().toString();
                    String newText = currentText.substring(0, cursorPos) + emojis[which] + currentText.substring(cursorPos);
                    messageInput.setText(newText);
                    messageInput.setSelection(cursorPos + emojis[which].length());
                })
                .show();
    }
    private void sendMessage(String message) {
        if (currentUser == null) return;
        String messageId = messagesRef.push().getKey();
        if (messageId == null) return;
        ChatMessage msg = new ChatMessage(
                messageId,
                message,
                currentUser.getUid(),
                currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User",
                System.currentTimeMillis(),
                false
        );
        messagesRef.child(messageId).setValue(msg)
                .addOnSuccessListener(aVoid -> {
                    messageInput.setText("");
                    // Update last message in chat info
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("lastMessage", message);
                    updates.put("lastMessageTime", System.currentTimeMillis());
                    updates.put("lastMessageSender", currentUser.getUid());
                    chatRef.child("info").updateChildren(updates);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void loadMessages() {
        if (messagesRef == null) return;
        messagesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ChatMessage message = data.getValue(ChatMessage.class);
                    if (message != null) {
                        message.setMessageId(data.getKey());
                        message.setSent(message.getSenderId().equals(currentUser.getUid()));
                        messageList.add(message);
                    }
                }
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        };
        messagesRef.addValueEventListener(messagesListener);
    }
    private void markMessagesAsRead() {
        if (messagesRef == null || currentUser == null) return;
        messagesRef.orderByChild("senderId").equalTo(otherUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            data.getRef().child("isRead").setValue(true);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesRef != null && messagesListener != null) {
            messagesRef.removeEventListener(messagesListener);
        }
    }
    // ChatMessage Model Class
    public static class ChatMessage {
        private String messageId;
        private String text;
        private String senderId;
        private String senderName;
        private long timestamp;
        private boolean isRead;
        private boolean isSent;
        public ChatMessage() {}
        public ChatMessage(String messageId, String text, String senderId, String senderName, long timestamp, boolean isRead) {
            this.messageId = messageId;
            this.text = text;
            this.senderId = senderId;
            this.senderName = senderName;
            this.timestamp = timestamp;
            this.isRead = isRead;
        }
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getSenderId() { return senderId; }
        public void setSenderId(String senderId) { this.senderId = senderId; }
        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public boolean isRead() { return isRead; }
        public void setRead(boolean read) { isRead = read; }
        public boolean isSent() { return isSent; }
        public void setSent(boolean sent) { isSent = sent; }
        public String getFormattedTime() {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
    // MessageAdapter
    private class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
        private List<ChatMessage> messages;
        MessageAdapter(List<ChatMessage> messages) { this.messages = messages; }
        @Override
        public int getItemViewType(int position) {
            return messages.get(position).isSent() ? 1 : 0;
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layout = viewType == 1 ? R.layout.item_message_sent : R.layout.item_message_received;
            return new ViewHolder(getLayoutInflater().inflate(layout, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChatMessage message = messages.get(position);
            holder.messageText.setText(message.getText());
            holder.timeText.setText(message.getFormattedTime());
        }
        @Override
        public int getItemCount() { return messages.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText;
            ViewHolder(View itemView) {
                super(itemView);
                messageText = itemView.findViewById(R.id.messageText);
                timeText = itemView.findViewById(R.id.timeText);
            }
        }
    }
}