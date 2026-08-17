package com.example.hustlefix;

import android.util.Log;

import android.os.Bundle;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

public class MyBookingsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvBookings;
    private ProgressBar progressBar;
    private View tvEmpty;

    private DatabaseReference bookingsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private List<Booking> bookingList;
    private BookingAdapter bookingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            Log.d("MyBookings", "User ID: " + currentUserId);
        } else {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            finish();
        }

        initViews();
        setupToolbar();
        loadBookings();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvBookings = findViewById(R.id.rvBookings);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        bookingList = new ArrayList<>();
        bookingAdapter = new BookingAdapter(bookingList, new BookingAdapter.OnBookingClickListener() {
            @Override
            public void onBookingClick(Booking booking) {
                // Navigate to booking detail
                Intent intent = new Intent(MyBookingsActivity.this, BookingDetailActivity.class);
                intent.putExtra("bookingId", booking.getBookingId());
                startActivity(intent);
            }
        });
        rvBookings.setAdapter(bookingAdapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Bookings");
        }
    }

    private void loadBookings() {
        if (currentUserId == null) return;

        setLoading(true);
        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        
        String role = SessionHelper.getRole(this);
        String queryField = "service_provider".equals(role) ? "serviceProviderId" : "clientId";
        
        Log.d("MyBookings", "Loading bookings for role: " + role + " using field: " + queryField);

        // Query bookings based on role
        bookingsRef.orderByChild(queryField).equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d("MyBookings", "Snapshot exists: " + snapshot.exists());
                        Log.d("MyBookings", "Children count: " + snapshot.getChildrenCount());
                        setLoading(false);
                        bookingList.clear();

                        if (snapshot.exists()) {
                            for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                                Booking booking = bookingSnapshot.getValue(Booking.class);
                                if (booking != null) {
                                    bookingList.add(booking);
                                }
                            }
                        }

                        if (bookingList.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            rvBookings.setVisibility(View.GONE);
                        } else {
                            // Reverse to show newest first
                            java.util.Collections.reverse(bookingList);
                            bookingAdapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(View.GONE);
                            rvBookings.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        setLoading(false);
                        Toast.makeText(MyBookingsActivity.this, "Error loading bookings: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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