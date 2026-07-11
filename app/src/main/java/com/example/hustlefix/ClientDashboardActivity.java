package com.example.hustlefix;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
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

public class ClientDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "ClientDash";
    
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private TextView tvTotalBookings;
    private TextView tvActiveBookings;
    private TextView tvCompletedBookings;
    private TextView tvClientName;
    private TextView tvDate;
    private RecyclerView rvRecentBookings;
    
    private CardView cardFindServices;
    private CardView cardMyBookings;
    private CardView cardSavedServices;
    private CardView cardMessages;
    
    // Recent Messages Section
    private LinearLayout llRecentMessages;
    private TextView tvRecentMessage1, tvRecentMessage2, tvRecentMessage3;
    private TextView tvMessagePartner1, tvMessagePartner2, tvMessagePartner3;
    private TextView tvMessageTime1, tvMessageTime2, tvMessageTime3;
    private View divider1, divider2;

    private FirebaseAuth mAuth;
    private String currentUserId;

    private List<Booking> recentBookings;
    private BookingAdapter bookingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_dashboard);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        initViews();
        setupToolbar();
        setupNavigationDrawer();
        setupClickListeners();
        loadDashboardData();
        loadRecentBookings();
        loadRecentMessages();
        updateNavHeader();
        setCurrentDate();
        setClientName();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        tvTotalBookings = findViewById(R.id.tvTotalBookings);
        tvActiveBookings = findViewById(R.id.tvActiveBookings);
        tvCompletedBookings = findViewById(R.id.tvCompletedBookings);
        tvClientName = findViewById(R.id.tvClientName);
        tvDate = findViewById(R.id.tvDate);

        rvRecentBookings = findViewById(R.id.rvRecentBookings);
        rvRecentBookings.setLayoutManager(new LinearLayoutManager(this));

        cardFindServices = findViewById(R.id.cardFindServices);
        cardMyBookings = findViewById(R.id.cardMyBookings);
        cardSavedServices = findViewById(R.id.cardSavedServices);
        cardMessages = findViewById(R.id.cardMessages);
        
        // Recent Messages
        llRecentMessages = findViewById(R.id.llRecentMessages);
        tvRecentMessage1 = findViewById(R.id.tvRecentMessage1);
        tvRecentMessage2 = findViewById(R.id.tvRecentMessage2);
        tvRecentMessage3 = findViewById(R.id.tvRecentMessage3);
        tvMessagePartner1 = findViewById(R.id.tvMessagePartner1);
        tvMessagePartner2 = findViewById(R.id.tvMessagePartner2);
        tvMessagePartner3 = findViewById(R.id.tvMessagePartner3);
        tvMessageTime1 = findViewById(R.id.tvMessageTime1);
        tvMessageTime2 = findViewById(R.id.tvMessageTime2);
        tvMessageTime3 = findViewById(R.id.tvMessageTime3);
        divider1 = findViewById(R.id.divider1);
        divider2 = findViewById(R.id.divider2);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dashboard");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void updateNavHeader() {
        if (navigationView != null && navigationView.getHeaderView(0) != null) {
            View headerView = navigationView.getHeaderView(0);
            TextView tvNavUserName = headerView.findViewById(R.id.tvNavUserName);
            TextView tvNavUserEmail = headerView.findViewById(R.id.tvNavUserEmail);
            ImageView ivNavAvatar = headerView.findViewById(R.id.ivNavAvatar);

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String name = currentUser.getDisplayName();
                if (name == null || name.isEmpty()) {
                    String email = currentUser.getEmail();
                    if (email != null && email.contains("@")) {
                        name = email.split("@")[0];
                    } else {
                        name = "User";
                    }
                }
                if (tvNavUserName != null) tvNavUserName.setText(name);
                if (tvNavUserEmail != null) tvNavUserEmail.setText(currentUser.getEmail());

                if (ivNavAvatar != null) {
                    ivNavAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                }
            }
        }
    }

    private void setCurrentDate() {
        if (tvDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault());
            tvDate.setText(sdf.format(new Date()));
        }
    }

    private void setClientName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && tvClientName != null) {
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                String email = user.getEmail();
                if (email != null && email.contains("@")) {
                    name = email.split("@")[0];
                } else {
                    name = "Client";
                }
            }
            tvClientName.setText("Welcome, " + name + "!");
        }
    }

    private void setupClickListeners() {
        cardFindServices.setOnClickListener(v -> {
            Log.d(TAG, "Find Services clicked");
            startActivity(new Intent(this, FindServicesActivity.class));
        });

        cardMyBookings.setOnClickListener(v -> {
            Log.d(TAG, "My Bookings clicked");
            startActivity(new Intent(this, MyBookingsActivity.class));
        });

        cardSavedServices.setOnClickListener(v -> {
            Log.d(TAG, "Saved Services clicked");
            startActivity(new Intent(this, SavedServicesActivity.class));
        });

        cardMessages.setOnClickListener(v -> {
            Log.d(TAG, "Messages clicked");
            Toast.makeText(ClientDashboardActivity.this, "Opening Messages...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ClientDashboardActivity.this, ChatListActivity.class);
            startActivity(intent);
        });
        
        // Click on recent messages to open chat
        llRecentMessages.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatListActivity.class));
        });
    }

    private void loadDashboardData() {
        if (currentUserId == null) return;

        DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        bookingsRef.orderByChild("clientId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int total = 0;
                        int active = 0;
                        int completed = 0;

                        for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                            Booking booking = bookingSnapshot.getValue(Booking.class);
                            if (booking != null) {
                                total++;
                                String status = booking.getStatus() != null ? booking.getStatus() : "";
                                if (status.equals("completed")) {
                                    completed++;
                                } else if (!status.equals("cancelled")) {
                                    active++;
                                }
                            }
                        }

                        if (tvTotalBookings != null) tvTotalBookings.setText(String.valueOf(total));
                        if (tvActiveBookings != null) tvActiveBookings.setText(String.valueOf(active));
                        if (tvCompletedBookings != null) tvCompletedBookings.setText(String.valueOf(completed));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (tvTotalBookings != null) tvTotalBookings.setText("0");
                        if (tvActiveBookings != null) tvActiveBookings.setText("0");
                        if (tvCompletedBookings != null) tvCompletedBookings.setText("0");
                    }
                });
    }

    private void loadRecentBookings() {
        if (currentUserId == null) return;

        recentBookings = new ArrayList<>();
        DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        bookingsRef.orderByChild("clientId").equalTo(currentUserId)
                .limitToLast(5)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        recentBookings.clear();
                        for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                            Booking booking = bookingSnapshot.getValue(Booking.class);
                            if (booking != null) {
                                recentBookings.add(booking);
                            }
                        }

                        TextView tvEmpty = findViewById(R.id.tvEmptyBookings);
                        if (recentBookings.isEmpty()) {
                            if (tvEmpty != null) {
                                tvEmpty.setVisibility(View.VISIBLE);
                                rvRecentBookings.setVisibility(View.GONE);
                            }
                            return;
                        }

                        List<Booking> reversed = new ArrayList<>();
                        for (int i = recentBookings.size() - 1; i >= 0; i--) {
                            reversed.add(recentBookings.get(i));
                        }

                        bookingAdapter = new BookingAdapter(reversed, booking -> {
                            Intent intent = new Intent(ClientDashboardActivity.this, BookingDetailActivity.class);
                            intent.putExtra("bookingId", booking.getBookingId());
                            startActivity(intent);
                        });
                        rvRecentBookings.setAdapter(bookingAdapter);
                        
                        if (tvEmpty != null) {
                            tvEmpty.setVisibility(View.GONE);
                            rvRecentBookings.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    private void loadRecentMessages() {
        if (currentUserId == null) return;

        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        
        messagesRef.orderByChild("chatId").startAt(currentUserId).endAt(currentUserId + "\uf8ff")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Map<String, ChatSummary> chatMap = new HashMap<>();
                            
                            for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                                for (DataSnapshot messageSnapshot : chatSnapshot.getChildren()) {
                                    Message message = messageSnapshot.getValue(Message.class);
                                    if (message != null) {
                                        String chatId = message.getChatId();
                                        if (!chatMap.containsKey(chatId)) {
                                            ChatSummary chat = new ChatSummary();
                                            chat.setChatId(chatId);
                                            if (message.getSenderId().equals(currentUserId)) {
                                                chat.setPartnerId(message.getReceiverId());
                                                chat.setPartnerName(message.getReceiverName());
                                            } else {
                                                chat.setPartnerId(message.getSenderId());
                                                chat.setPartnerName(message.getSenderName());
                                            }
                                            chat.setLastMessage(message.getMessageText());
                                            chat.setLastTimestamp(message.getTimestamp());
                                            chatMap.put(chatId, chat);
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
                            
                            List<ChatSummary> sortedChats = new ArrayList<>(chatMap.values());
                            sortedChats.sort((c1, c2) -> Long.compare(c2.getLastTimestamp(), c1.getLastTimestamp()));
                            
                            displayRecentMessages(sortedChats);
                        } else {
                            llRecentMessages.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        llRecentMessages.setVisibility(View.GONE);
                    }
                });
    }

    private void displayRecentMessages(List<ChatSummary> chats) {
        int count = Math.min(chats.size(), 3);
        
        if (count == 0) {
            llRecentMessages.setVisibility(View.GONE);
            return;
        }
        
        llRecentMessages.setVisibility(View.VISIBLE);
        
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        
        ChatSummary chat1 = chats.get(0);
        tvMessagePartner1.setText(chat1.getPartnerName());
        tvRecentMessage1.setText(chat1.getLastMessage());
        tvMessageTime1.setText(sdf.format(new Date(chat1.getLastTimestamp())));
        
        if (count > 1) {
            ChatSummary chat2 = chats.get(1);
            divider1.setVisibility(View.VISIBLE);
            tvMessagePartner2.setText(chat2.getPartnerName());
            tvRecentMessage2.setText(chat2.getLastMessage());
            tvMessageTime2.setText(sdf.format(new Date(chat2.getLastTimestamp())));
        } else {
            divider1.setVisibility(View.GONE);
        }
        
        if (count > 2) {
            ChatSummary chat3 = chats.get(2);
            divider2.setVisibility(View.VISIBLE);
            tvMessagePartner3.setText(chat3.getPartnerName());
            tvRecentMessage3.setText(chat3.getLastMessage());
            tvMessageTime3.setText(sdf.format(new Date(chat3.getLastTimestamp())));
        } else {
            divider2.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_find_workers) {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, FindServicesActivity.class));
            return true;
        } else if (id == R.id.nav_my_bookings) {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, MyBookingsActivity.class));
            return true;
        } else if (id == R.id.nav_saved) {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, SavedServicesActivity.class));
            return true;
        } else if (id == R.id.nav_messages) {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, ChatListActivity.class));
            return true;
        } else if (id == R.id.nav_settings) {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.nav_logout) {
            drawerLayout.closeDrawer(GravityCompat.START);
            logout();
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    SessionHelper.logout(this);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}