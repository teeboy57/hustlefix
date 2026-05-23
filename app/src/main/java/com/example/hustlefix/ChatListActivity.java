package com.example.hustlefix;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerChats;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private final List<ChatSummary> chatSummaries = new ArrayList<>();
    private ChatListAdapter adapter;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerChats = findViewById(R.id.recyclerChats);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new ChatListAdapter();
        recyclerChats.setLayoutManager(new LinearLayoutManager(this));
        recyclerChats.setAdapter(adapter);

        loadChats();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return NavigationHelper.onOptionsItemSelected(this, item) || super.onOptionsItemSelected(item);
    }

    private void loadChats() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        chatSummaries.clear();

        String myId = currentUser.getUid();
        FirebaseDatabase.getInstance().getReference("userChats").child(myId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userChatsSnapshot) {
                        Set<String> roomIds = new HashSet<>();
                        for (DataSnapshot child : userChatsSnapshot.getChildren()) {
                            if (child.getKey() != null) {
                                roomIds.add(child.getKey());
                            }
                        }
                        if (roomIds.isEmpty()) {
                            loadFromAllChats(roomIds);
                        } else {
                            fetchChatInfos(roomIds);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loadFromAllChats(new HashSet<>());
                    }
                });
    }

    private void loadFromAllChats(Set<String> roomIds) {
        String myId = currentUser.getUid();
        FirebaseDatabase.getInstance().getReference("chats")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                            String chatRoomId = chatSnapshot.getKey();
                            if (chatRoomId != null && chatRoomId.contains(myId)) {
                                roomIds.add(chatRoomId);
                            }
                        }
                        fetchChatInfos(roomIds);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showResults();
                    }
                });
    }

    private void fetchChatInfos(Set<String> roomIds) {
        if (roomIds.isEmpty()) {
            showResults();
            return;
        }

        final int total = roomIds.size();
        final int[] loaded = {0};

        for (String chatRoomId : roomIds) {
            FirebaseDatabase.getInstance().getReference("chats")
                    .child(chatRoomId)
                    .child("info")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot infoSnapshot) {
                            if (infoSnapshot.exists()) {
                                addSummaryFromInfo(chatRoomId, infoSnapshot);
                            }
                            if (++loaded[0] >= total) {
                                showResults();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            if (++loaded[0] >= total) {
                                showResults();
                            }
                        }
                    });
        }
    }

    private void addSummaryFromInfo(String chatRoomId, DataSnapshot infoSnapshot) {
        String myId = currentUser.getUid();
        String otherUserId = resolveOtherUserId(chatRoomId, myId, infoSnapshot);
        if (TextUtils.isEmpty(otherUserId)) return;

        String otherUserName = infoSnapshot.child("participantNames").child(otherUserId).getValue(String.class);
        String lastMessage = infoSnapshot.child("lastMessage").getValue(String.class);
        Long lastMessageTime = infoSnapshot.child("lastMessageTime").getValue(Long.class);

        chatSummaries.add(new ChatSummary(
                chatRoomId,
                otherUserId,
                otherUserName != null ? otherUserName : "User",
                lastMessage != null ? lastMessage : "",
                lastMessageTime != null ? lastMessageTime : 0L
        ));
    }

    private String resolveOtherUserId(String chatRoomId, String myId, DataSnapshot infoSnapshot) {
        DataSnapshot participants = infoSnapshot.child("participants");
        if (participants.exists()) {
            for (DataSnapshot p : participants.getChildren()) {
                String uid = p.getKey();
                if (uid != null && !uid.equals(myId)) {
                    return uid;
                }
            }
        }
        String[] parts = chatRoomId.split("_");
        if (parts.length == 2) {
            if (parts[0].equals(myId)) return parts[1];
            if (parts[1].equals(myId)) return parts[0];
        }
        return null;
    }

    private void showResults() {
        Collections.sort(chatSummaries, (a, b) -> Long.compare(b.lastMessageTime, a.lastMessageTime));
        progressBar.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
        if (chatSummaries.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerChats.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerChats.setVisibility(View.VISIBLE);
        }
    }

    private static class ChatSummary {
        final String chatRoomId;
        final String otherUserId;
        final String otherUserName;
        final String lastMessage;
        final long lastMessageTime;

        ChatSummary(String chatRoomId, String otherUserId, String otherUserName,
                    String lastMessage, long lastMessageTime) {
            this.chatRoomId = chatRoomId;
            this.otherUserId = otherUserId;
            this.otherUserName = otherUserName;
            this.lastMessage = lastMessage;
            this.lastMessageTime = lastMessageTime;
        }
    }

    private class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_conversation, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChatSummary summary = chatSummaries.get(position);
            holder.tvChatName.setText(summary.otherUserName);
            holder.tvLastMessage.setText(
                    TextUtils.isEmpty(summary.lastMessage) ? "No messages yet" : summary.lastMessage);
            holder.itemView.setOnClickListener(v ->
                    ChatLauncher.openChat(ChatListActivity.this, summary.otherUserId, summary.otherUserName));
        }

        @Override
        public int getItemCount() {
            return chatSummaries.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView tvChatName;
            final TextView tvLastMessage;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvChatName = itemView.findViewById(R.id.tvChatName);
                tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            }
        }
    }
}
