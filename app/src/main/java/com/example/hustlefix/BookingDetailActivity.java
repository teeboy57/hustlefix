package com.example.hustlefix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BookingDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvServiceTitle, tvClientName, tvPrice, tvStatus, tvDate, tvPaymentStatus;
    private Button btnAccept, btnReject, btnComplete, btnChat, btnPay;
    private ProgressBar progressBar;

    // Rating views
    private View cardRating;
    private RatingBar ratingBar;
    private EditText etReview;
    private Button btnSubmitRating;

    private DatabaseReference bookingsRef;
    private DatabaseReference usersRef;
    private String bookingId;
    private boolean isServiceProvider;

    private String clientId;
    private String serviceProviderId;
    private String clientName;
    private String serviceProviderName;
    private String serviceTitle;
    
    private Booking currentBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);

        bookingId = getIntent().getStringExtra("bookingId");
        isServiceProvider = getIntent().getBooleanExtra("isServiceProvider", false);

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
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);
        btnComplete = findViewById(R.id.btnComplete);
        btnChat = findViewById(R.id.btnChat);
        btnPay = findViewById(R.id.btnPay);
        progressBar = findViewById(R.id.progressBar);

        // Rating Section
        cardRating = findViewById(R.id.cardRating);
        ratingBar = findViewById(R.id.ratingBar);
        etReview = findViewById(R.id.etReview);
        btnSubmitRating = findViewById(R.id.btnSubmitRating);

        if (isServiceProvider) {
            btnAccept.setVisibility(View.VISIBLE);
            btnReject.setVisibility(View.VISIBLE);
            btnComplete.setVisibility(View.VISIBLE);
        } else {
            btnAccept.setVisibility(View.GONE);
            btnReject.setVisibility(View.GONE);
            btnComplete.setVisibility(View.GONE);
        }

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
                        currentBooking = snapshot.getValue(Booking.class);
                        if (currentBooking != null) {
                            clientId = currentBooking.getClientId();
                            serviceProviderId = currentBooking.getServiceProviderId();
                            clientName = currentBooking.getClientName();
                            serviceProviderName = currentBooking.getServiceProviderName();
                            serviceTitle = currentBooking.getServiceTitle();

                            displayBookingData(currentBooking);
                            showChatButtonIfConfirmed(currentBooking);
                            checkRatingStatus(currentBooking);
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
        tvClientName.setText(isServiceProvider ? "Client: " + booking.getClientName() : "Provider: " + booking.getServiceProviderName());
        tvPrice.setText("Price: R" + String.format("%.2f", booking.getPrice()));

        String status = booking.getStatus() != null ? booking.getStatus() : "pending";
        tvStatus.setText("Status: " + status.toUpperCase());

        String payment = booking.getPaymentStatus();
        if (tvPaymentStatus != null) {
            tvPaymentStatus.setText("Payment: " + payment);
            if ("RELEASED".equals(payment)) tvPaymentStatus.setTextColor(0xFF4CAF50);
            else if ("ESCROW".equals(payment)) tvPaymentStatus.setTextColor(0xFF2196F3);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        tvDate.setText("Date: " + sdf.format(new Date(booking.getBookingDate())));
        
        // Show/Hide Pay button for client
        if (!isServiceProvider && "confirmed".equalsIgnoreCase(status) && "UNPAID".equals(payment)) {
            if (btnPay != null) btnPay.setVisibility(View.VISIBLE);
        } else {
            if (btnPay != null) btnPay.setVisibility(View.GONE);
        }

        // Only allow completing if paid or mock paid
        if (isServiceProvider && "confirmed".equalsIgnoreCase(status)) {
            btnComplete.setVisibility(View.VISIBLE);
        }

        // Update status badge background based on status
        if (status.equalsIgnoreCase("completed")) {
            tvStatus.setBackgroundResource(R.drawable.badge_accepted); // Green-ish badge
        } else if (status.equalsIgnoreCase("cancelled")) {
            tvStatus.setBackgroundResource(R.drawable.badge_red);
        } else {
            tvStatus.setBackgroundResource(R.drawable.badge_pending);
        }
    }

    private void showChatButtonIfConfirmed(Booking booking) {
        String status = booking.getStatus() != null ? booking.getStatus() : "";
        if (status.equals("confirmed") || status.equals("completed")) {
            btnChat.setVisibility(View.VISIBLE);
        }
    }

    private void checkRatingStatus(Booking booking) {
        if (!isServiceProvider && "completed".equalsIgnoreCase(booking.getStatus())) {
            FirebaseDatabase.getInstance().getReference("ratings")
                    .orderByChild("jobId").equalTo(bookingId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                cardRating.setVisibility(View.GONE);
                            } else {
                                cardRating.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        } else {
            cardRating.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        btnAccept.setOnClickListener(v -> updateBookingStatus("confirmed"));
        btnReject.setOnClickListener(v -> updateBookingStatus("cancelled"));
        btnComplete.setOnClickListener(v -> completeJobAndReleaseFunds());
        btnChat.setOnClickListener(v -> openChat());
        if (btnPay != null) btnPay.setOnClickListener(v -> showPaymentSelectionDialog());
        btnSubmitRating.setOnClickListener(v -> submitRating());
    }

    private void showPaymentSelectionDialog() {
        String[] options = {"Credit/Debit Card", "Mobile Money", "Cash on Delivery"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Payment Method")
                .setItems(options, (dialog, which) -> {
                    payToEscrow();
                })
                .show();
    }

    private void payToEscrow() {
        setLoading(true);
        bookingsRef.child(bookingId).child("paymentStatus").setValue("ESCROW")
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(this, "Funds secured in Escrow!", Toast.LENGTH_SHORT).show();
                    loadBookingData();
                });
    }

    private void completeJobAndReleaseFunds() {
        setLoading(true);
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "completed");
        
        if (currentBooking != null && "ESCROW".equals(currentBooking.getPaymentStatus())) {
            updates.put("paymentStatus", "RELEASED");
        }
        
        bookingsRef.child(bookingId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(this, "Job completed and funds released!", Toast.LENGTH_LONG).show();
                    loadBookingData();
                })
                .addOnFailureListener(e -> setLoading(false));
    }

    private void updateBookingStatus(String status) {
        setLoading(true);
        bookingsRef.child(bookingId).child("status").setValue(status)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(this, "Booking " + status + "!", Toast.LENGTH_SHORT).show();

                    if (status.equals("confirmed")) {
                        btnChat.setVisibility(View.VISIBLE);
                    }
                    
                    if (status.equals("completed") && !isServiceProvider) {
                        cardRating.setVisibility(View.VISIBLE);
                    }

                    loadBookingData();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openChat() {
        String partnerId;
        String partnerName;

        if (isServiceProvider) {
            partnerId = clientId;
            partnerName = clientName;
        } else {
            partnerId = serviceProviderId;
            partnerName = serviceProviderName;
        }

        if (partnerId == null || partnerId.isEmpty()) {
            Toast.makeText(this, "Unable to start chat", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(BookingDetailActivity.this, ChatActivity.class);
        intent.putExtra("partnerId", partnerId);
        intent.putExtra("partnerName", partnerName);
        startActivity(intent);
    }

    private void submitRating() {
        float ratingValue = ratingBar.getRating();
        String review = etReview.getText().toString().trim();

        if (ratingValue == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("ratings");
        String ratingId = ratingsRef.push().getKey();

        Rating ratingObj = new Rating(
                bookingId,
                serviceTitle,
                clientId,
                clientName,
                serviceProviderId,
                serviceProviderName,
                ratingValue,
                review,
                false
        );
        ratingObj.setId(ratingId);

        if (ratingId != null) {
            ratingsRef.child(ratingId).setValue(ratingObj)
                    .addOnSuccessListener(aVoid -> {
                        bookingsRef.child(bookingId).child("rating").setValue(ratingValue);
                        updateProviderRating(serviceProviderId, ratingValue);
                        cardRating.setVisibility(View.GONE);
                        Toast.makeText(BookingDetailActivity.this, "Rating submitted!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        Toast.makeText(BookingDetailActivity.this, "Failed to submit rating", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateProviderRating(String providerId, float newRating) {
        usersRef = FirebaseDatabase.getInstance().getReference("users").child(providerId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long completedJobs = 0;
                    if (snapshot.hasChild("completedJobs")) {
                        Object val = snapshot.child("completedJobs").getValue();
                        completedJobs = val instanceof Long ? (Long) val : 0;
                    }
                    
                    double currentRating = 0.0;
                    if (snapshot.hasChild("rating")) {
                        Object val = snapshot.child("rating").getValue();
                        currentRating = val instanceof Number ? ((Number) val).doubleValue() : 0.0;
                    }

                    double totalRatingScore = (currentRating * completedJobs) + newRating;
                    completedJobs++;
                    double newAverageRating = totalRatingScore / completedJobs;

                    usersRef.child("rating").setValue(newAverageRating);
                    usersRef.child("completedJobs").setValue(completedJobs);
                }
                setLoading(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setLoading(false);
            }
        });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnAccept.setEnabled(!isLoading);
        btnReject.setEnabled(!isLoading);
        btnComplete.setEnabled(!isLoading);
        btnChat.setEnabled(!isLoading);
        if (btnSubmitRating != null) btnSubmitRating.setEnabled(!isLoading);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
