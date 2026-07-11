package com.example.hustlefix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookingDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvServiceTitle, tvClientName, tvPrice, tvStatus, tvDate;
    private Button btnAccept, btnReject, btnComplete, btnChat;
    private ProgressBar progressBar;

    private DatabaseReference bookingsRef;
    private String bookingId;
    private boolean isEntrepreneur;
    private String clientId;
    private String entrepreneurId;
    private String clientName;
    private String entrepreneurName;
    private String serviceTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);

        bookingId = getIntent().getStringExtra("bookingId");
        isEntrepreneur = getIntent().getBooleanExtra("isEntrepreneur", false);

        initViews();
        setupToolbar();
        loadBookingData();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvServiceTitle = findViewById(R.id.tvServiceTitle);
        tvClientName = findViewById(R.id.tvClientName);
        tvPrice = findViewById(R.id.tvPrice);
        tvStatus = findViewById(R.id.tvStatus);
        tvDate = findViewById(R.id.tvDate);
        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);
        btnComplete = findViewById(R.id.btnComplete);
        btnChat = findViewById(R.id.btnChat);
        progressBar = findViewById(R.id.progressBar);

        // Show/hide entrepreneur actions
        if (isEntrepreneur) {
            btnAccept.setVisibility(View.VISIBLE);
            btnReject.setVisibility(View.VISIBLE);
            btnComplete.setVisibility(View.VISIBLE);
        } else {
            btnAccept.setVisibility(View.GONE);
            btnReject.setVisibility(View.GONE);
            btnComplete.setVisibility(View.GONE);
        }
        
        // Hide chat button initially (will be shown if status is confirmed or completed)
        btnChat.setVisibility(View.GONE);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Booking Details");
        }
    }

    private void loadBookingData() {
        setLoading(true);
        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        bookingsRef.child(bookingId).get()
                .addOnSuccessListener(snapshot -> {
                    setLoading(false);
                    if (snapshot.exists()) {
                        Booking booking = snapshot.getValue(Booking.class);
                        if (booking != null) {
                            // Store IDs and names for chat
                            clientId = booking.getClientId();
                            entrepreneurId = booking.getEntrepreneurId();
                            clientName = booking.getClientName();
                            entrepreneurName = booking.getEntrepreneurName();
                            serviceTitle = booking.getServiceTitle();
                            
                            displayBookingData(booking);
                            showChatButtonIfConfirmed(booking);
                        }
                    } else {
                        Toast.makeText(this, "Booking not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayBookingData(Booking booking) {
        tvServiceTitle.setText("Service: " + booking.getServiceTitle());
        tvClientName.setText("Client: " + booking.getClientName());
        tvPrice.setText("Price: $" + String.format("%.2f", booking.getPrice()));
        
        String status = booking.getStatus() != null ? booking.getStatus() : "pending";
        tvStatus.setText("Status: " + status.toUpperCase());
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        tvDate.setText("Date: " + sdf.format(new Date(booking.getBookingDate())));
    }

    private void showChatButtonIfConfirmed(Booking booking) {
        String status = booking.getStatus() != null ? booking.getStatus() : "";
        if (status.equals("confirmed") || status.equals("completed")) {
            btnChat.setVisibility(View.VISIBLE);
        }
    }

    private void setupClickListeners() {
        btnAccept.setOnClickListener(v -> updateBookingStatus("confirmed"));
        btnReject.setOnClickListener(v -> updateBookingStatus("cancelled"));
        btnComplete.setOnClickListener(v -> updateBookingStatus("completed"));
        btnChat.setOnClickListener(v -> openChat());
    }

    private void updateBookingStatus(String status) {
        setLoading(true);
        bookingsRef.child(bookingId).child("status").setValue(status)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(this, "Booking " + status + "!", Toast.LENGTH_SHORT).show();
                    
                    // If status is confirmed, show chat button
                    if (status.equals("confirmed")) {
                        btnChat.setVisibility(View.VISIBLE);
                    }
                    
                    // Refresh data
                    loadBookingData();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openChat() {
        // Determine who is the partner to chat with
        String partnerId;
        String partnerName;
        
        if (isEntrepreneur) {
            // Entrepreneur wants to chat with client
            partnerId = clientId;
            partnerName = clientName;
        } else {
            // Client wants to chat with entrepreneur
            partnerId = entrepreneurId;
            partnerName = entrepreneurName;
        }
        
        if (partnerId == null || partnerId.isEmpty()) {
            Toast.makeText(this, "Unable to start chat", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(BookingDetailActivity.this, ChatActivity.class);
        intent.putExtra("partnerId", partnerId);
        intent.putExtra("partnerName", partnerName);
        intent.putExtra("bookingId", bookingId);
        startActivity(intent);
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnAccept.setEnabled(!isLoading);
        btnReject.setEnabled(!isLoading);
        btnComplete.setEnabled(!isLoading);
        btnChat.setEnabled(!isLoading);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}