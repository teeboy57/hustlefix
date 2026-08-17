package com.example.hustlefix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ServiceDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvTitle, tvDescription, tvPrice, tvLocation, tvServiceProvider;
    private ImageView ivServiceImage;
    private RecyclerView rvServiceImages;
    private Button btnBookNow;
    private Button btnSave;
    private ProgressBar progressBar;

    private DatabaseReference bookingsRef;
    private FirebaseAuth mAuth;
    private String serviceId;
    private String currentUserId;
    private Service service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_service_detail);

            mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                currentUserId = currentUser.getUid();
            }

            serviceId = getIntent().getStringExtra("serviceId");

            initViews();
            setupToolbar();
            loadServiceData();
            setupClickListeners();
        } catch (Exception e) {
            ErrorUtils.showError(this, "Error loading service details: " + e.getMessage());
            finish();
        }
    }

    private void initViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            tvTitle = findViewById(R.id.tvServiceTitle);
            tvDescription = findViewById(R.id.tvServiceDescription);
            tvPrice = findViewById(R.id.tvServicePrice);
            tvLocation = findViewById(R.id.tvServiceLocation);
            tvServiceProvider = findViewById(R.id.tvServiceProviderName);
            ivServiceImage = findViewById(R.id.ivServiceImage);
            rvServiceImages = findViewById(R.id.rvServiceImages);
            btnBookNow = findViewById(R.id.btnBookNow);
            btnSave = findViewById(R.id.btnSave);
            progressBar = findViewById(R.id.progressBar);
        } catch (Exception e) {
            ErrorUtils.showError(this, "Error initializing views: " + e.getMessage());
        }
    }

    private void setupToolbar() {
        try {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Service Details");
            }
        } catch (Exception e) {
            ErrorUtils.showError(this, e);
        }
    }

    private void loadServiceData() {
        try {
            setLoading(true);
            FirebaseDatabase.getInstance().getReference("services").child(serviceId).get()
                    .addOnSuccessListener(snapshot -> {
                        try {
                            setLoading(false);
                            if (snapshot.exists()) {
                                service = snapshot.getValue(Service.class);
                                if (service != null) {
                                    displayServiceData();
                                } else {
                                    ErrorUtils.showError(ServiceDetailActivity.this, "Service data is invalid");
                                    finish();
                                }
                            } else {
                                ErrorUtils.showError(ServiceDetailActivity.this, "Service not found");
                                finish();
                            }
                        } catch (Exception e) {
                            ErrorUtils.showError(ServiceDetailActivity.this, "Error loading service: " + e.getMessage());
                        }
                    })
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        ErrorUtils.showError(ServiceDetailActivity.this, "Error loading service: " + e.getMessage());
                    });
        } catch (Exception e) {
            ErrorUtils.showError(this, e);
        }
    }

    private void displayServiceData() {
        try {
            tvTitle.setText(service.getTitle() != null ? service.getTitle() : "No Title");
            tvDescription.setText(service.getDescription() != null ? service.getDescription() : "No Description");
            tvPrice.setText("R" + String.format("%.2f", service.getPrice()));
            tvLocation.setText(service.getLocation() != null ? service.getLocation() : "Not specified");
            tvServiceProvider.setText(service.getserviceProviderName() != null ? service.getserviceProviderName() : "Unknown");
            
            if (service.getServiceImageUrls() != null && !service.getServiceImageUrls().isEmpty()) {
                rvServiceImages.setVisibility(View.VISIBLE);
                ivServiceImage.setVisibility(View.GONE);
                ServiceImageAdapter adapter = new ServiceImageAdapter(service.getServiceImageUrls());
                rvServiceImages.setAdapter(adapter);
            } else if (service.getServiceImageUrl() != null && !service.getServiceImageUrl().isEmpty()) {
                ivServiceImage.setVisibility(View.VISIBLE);
                rvServiceImages.setVisibility(View.GONE);
                Glide.with(this).load(service.getServiceImageUrl()).into(ivServiceImage);
            } else {
                ivServiceImage.setVisibility(View.GONE);
                rvServiceImages.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            ErrorUtils.showError(this, e);
        }
    }

    private void setupClickListeners() {
        try {
            btnBookNow.setOnClickListener(v -> bookService());
            btnSave.setOnClickListener(v -> saveService());
            
            // Link to Worker Profile for social proof
            if (tvServiceProvider != null) {
                tvServiceProvider.setOnClickListener(v -> openWorkerProfile());
            }
        } catch (Exception e) {
            ErrorUtils.showError(this, e);
        }
    }

    private void openWorkerProfile() {
        if (service == null || service.getserviceProviderId() == null) {
            Toast.makeText(this, "Profile not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, WorkerProfileActivity.class);
        intent.putExtra("worker_id", service.getserviceProviderId());
        startActivity(intent);
    }

    private void saveService() {
        try {
            if (currentUserId == null) {
                ErrorUtils.showError(this, "Please login to save");
                return;
            }

            if (service == null || service.getServiceId() == null) {
                ErrorUtils.showError(this, "Service not loaded");
                return;
            }

            DatabaseReference savedRef = FirebaseDatabase.getInstance().getReference("saved_services").child(currentUserId);
            savedRef.child(service.getServiceId()).setValue(service.getServiceId())
                    .addOnSuccessListener(aVoid -> {
                        ErrorUtils.showSuccess(ServiceDetailActivity.this, "Service saved!");
                    })
                    .addOnFailureListener(e -> {
                        ErrorUtils.showError(ServiceDetailActivity.this, "Failed to save: " + e.getMessage());
                    });
        } catch (Exception e) {
            ErrorUtils.showError(this, "Error saving service: " + e.getMessage());
        }
    }

    private void bookService() {
        try {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                ErrorUtils.showError(this, "Please login to book");
                return;
            }

            if (service == null) {
                ErrorUtils.showError(this, "Service data not loaded. Please try again.");
                return;
            }

            if (service.getServiceId() == null || service.getserviceProviderId() == null) {
                ErrorUtils.showError(this, "Service data is incomplete. Please try again.");
                return;
            }

            setLoading(true);
            bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
            String bookingId = bookingsRef.push().getKey();

            if (bookingId == null) {
                setLoading(false);
                ErrorUtils.showError(this, "Failed to create booking");
                return;
            }

            Map<String, Object> booking = new HashMap<>();
            booking.put("bookingId", bookingId);
            booking.put("serviceId", service.getServiceId());
            booking.put("serviceTitle", service.getTitle() != null ? service.getTitle() : "Service");
            booking.put("price", service.getPrice());
            booking.put("clientId", user.getUid());
            booking.put("clientName", user.getDisplayName() != null ? user.getDisplayName() : "Client");
            booking.put("serviceProviderId", service.getserviceProviderId());
            booking.put("serviceProviderName", service.getserviceProviderName() != null ? service.getserviceProviderName() : "ServiceProvider");
            booking.put("providerProfileImageUrl", service.getServiceProviderProfileImageUrl());
            booking.put("status", "pending");
            booking.put("timestamp", System.currentTimeMillis());
            booking.put("bookingDate", System.currentTimeMillis());
            booking.put("notes", "");
            booking.put("rating", 0);

            bookingsRef.child(bookingId).setValue(booking)
                    .addOnSuccessListener(aVoid -> {
                        setLoading(false);
                        ErrorUtils.showSuccess(ServiceDetailActivity.this, "Booking sent successfully!");
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        ErrorUtils.showError(ServiceDetailActivity.this, "Failed to book: " + e.getMessage());
                    });
        } catch (Exception e) {
            setLoading(false);
            ErrorUtils.showError(this, "Booking error: " + e.getMessage());
        }
    }

    private void setLoading(boolean isLoading) {
        try {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnBookNow.setEnabled(!isLoading);
            btnBookNow.setText(isLoading ? "BOOKING..." : "BOOK NOW");
            if (btnSave != null) {
                btnSave.setEnabled(!isLoading);
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}