package com.example.hustlefix;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvChatPartner;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageView btnSend;
    private ProgressBar progressBar;

    private DatabaseReference messagesRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String chatId;
    private String partnerId;
    private String partnerName;

    private List<Message> messageList;
    private MessageAdapter messageAdapter;
    private ValueEventListener messageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get partner info from intent
        partnerId = getIntent().getStringExtra(ChatLauncher.EXTRA_OTHER_USER_ID);
        partnerName = getIntent().getStringExtra(ChatLauncher.EXTRA_OTHER_USER_NAME);

        if (partnerId == null) {
            partnerId = getIntent().getStringExtra("partnerId");
        }
        if (partnerName == null) {
            partnerName = getIntent().getStringExtra("partnerName");
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        if (currentUserId == null || partnerId == null) {
            Toast.makeText(this, "Error: Missing user info", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Generate chat ID
        chatId = generateChatId(currentUserId, partnerId);

        initViews();
        setupToolbar();
        loadMessages();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvChatPartner = findViewById(R.id.tvChatPartner);
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        progressBar = findViewById(R.id.progressBar);

        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId);
        rvMessages.setAdapter(messageAdapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chat");
        }
        String displayName = partnerName != null ? partnerName : "User";
        tvChatPartner.setText(displayName);
    }

    private String generateChatId(String id1, String id2) {
        if (id1.compareTo(id2) < 0) {
            return id1 + "_" + id2;
        } else {
            return id2 + "_" + id1;
        }
    }

    private void loadMessages() {
        setLoading(true);
        messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId);
        
        // Remove any existing listener
        if (messageListener != null) {
            messagesRef.removeEventListener(messageListener);
        }
        
        messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                setLoading(false);
                messageList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                        Message message = messageSnapshot.getValue(Message.class);
                        if (message != null) {
                            messageList.add(message);
                        }
                    }
                    messageAdapter.notifyDataSetChanged();
                    rvMessages.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setLoading(false);
                Toast.makeText(ChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
            }
        };
        
        messagesRef.orderByChild("timestamp").addValueEventListener(messageListener);
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            return;
        }

        String senderName = user.getDisplayName() != null ? user.getDisplayName() : "User";

        String messageId = messagesRef.push().getKey();
        if (messageId == null) {
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
            return;
        }

        Message message = new Message(
                messageId,
                currentUserId,
                senderName,
                partnerId,
                partnerName != null ? partnerName : "Partner",
                messageText
        );

        messagesRef.child(messageId).setValue(message)
                .addOnSuccessListener(aVoid -> {
                    etMessage.setText("");
                    rvMessages.scrollToPosition(messageList.size() - 1);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listener to prevent memory leaks
        if (messageListener != null && messagesRef != null) {
            messagesRef.removeEventListener(messageListener);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}