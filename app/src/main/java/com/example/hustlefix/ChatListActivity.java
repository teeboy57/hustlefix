package com.example.hustlefix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvChats;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private DatabaseReference messagesRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private List<ChatSummary> chatList;
    private ChatListAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadChats();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvChats = findViewById(R.id.rvChats);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvChats.setLayoutManager(new LinearLayoutManager(this));
        chatList = new ArrayList<>();
        chatAdapter = new ChatListAdapter(chatList, new ChatListAdapter.OnChatClickListener() {
            @Override
            public void onChatClick(ChatSummary chat) {
                Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
                intent.putExtra("partnerId", chat.getPartnerId());
                intent.putExtra("partnerName", chat.getPartnerName());
                startActivity(intent);
            }
        });
        rvChats.setAdapter(chatAdapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Messages");
        }
    }

    private void loadChats() {
        if (currentUserId == null) return;

        setLoading(true);
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        
        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                setLoading(false);
                chatList.clear();

                if (snapshot.exists()) {
                    Map<String, ChatSummary> chatMap = new HashMap<>();
                    
                    for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                        String chatId = chatSnapshot.getKey();
                        if (chatId == null) continue;
                        
                        // Check if current user is part of this chat
                        if (!chatId.contains(currentUserId)) continue;
                        
                        for (DataSnapshot messageSnapshot : chatSnapshot.getChildren()) {
                            Message message = messageSnapshot.getValue(Message.class);
                            if (message != null) {
                                if (!chatMap.containsKey(chatId)) {
                                    ChatSummary chat = new ChatSummary();
                                    chat.setChatId(chatId);
                                    
                                    // Determine partner
                                    String[] parts = chatId.split("_");
                                    if (parts.length == 2) {
                                        if (parts[0].equals(currentUserId)) {
                                            chat.setPartnerId(parts[1]);
                                        } else {
                                            chat.setPartnerId(parts[0]);
                                        }
                                    }
                                    
                                    // Get partner name from the message
                                    if (message.getSenderId().equals(currentUserId)) {
                                        chat.setPartnerName(message.getReceiverName());
                                    } else {
                                        chat.setPartnerName(message.getSenderName());
                                    }
                                    
                                    chat.setLastMessage(message.getMessageText());
                                    chat.setLastTimestamp(message.getTimestamp());
                                    chatMap.put(chatId, chat);
                                    
                                    // Fetch partner profile image
                                    fetchPartnerProfile(chat);
                                } else {
                                    ChatSummary existing = chatMap.get(chatId);
                                    if (message.getTimestamp() > existing.getLastTimestamp()) {
                                        existing.setLastMessage(message.getMessageText());
                                        existing.setLastTimestamp(message.getTimestamp());
                                    }
                                }
                            }
                        }
                    }
                    
                    chatList.addAll(chatMap.values());
                    chatList.sort((c1, c2) -> Long.compare(c2.getLastTimestamp(), c1.getLastTimestamp()));
                    chatAdapter.notifyDataSetChanged();
                    
                    if (chatList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvChats.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvChats.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvChats.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setLoading(false);
                Toast.makeText(ChatListActivity.this, "Error loading chats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPartnerProfile(ChatSummary chat) {
        if (chat.getPartnerId() == null) return;
        
        FirebaseDatabase.getInstance().getReference("users").child(chat.getPartnerId())
                .child("profileImage").get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        chat.setPartnerProfileUrl(snapshot.getValue(String.class));
                        chatAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}