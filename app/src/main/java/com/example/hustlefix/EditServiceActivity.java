package com.example.hustlefix;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EditServiceActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etPrice, etLocation, etDeliveryTime;
    private MaterialButton btnUpdateService, btnDeleteService;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private DatabaseReference servicesRef;
    private FirebaseAuth mAuth;
    private String serviceId;
    private Service currentService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_service);

        // Get service data from intent
        serviceId = getIntent().getStringExtra("serviceId");
        if (serviceId == null) {
            Toast.makeText(this, "Service not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupFirebase();
        loadServiceData();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etLocation = findViewById(R.id.etLocation);
        etDeliveryTime = findViewById(R.id.etDeliveryTime);
        btnUpdateService = findViewById(R.id.btnUpdateService);
        btnDeleteService = findViewById(R.id.btnDeleteService);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Service");
        }
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        servicesRef = FirebaseDatabase.getInstance().getReference("services");
    }

    private void loadServiceData() {
        setLoading(true);
        servicesRef.child(serviceId).get()
                .addOnSuccessListener(snapshot -> {
                    setLoading(false);
                    if (snapshot.exists()) {
                        currentService = snapshot.getValue(Service.class);
                        if (currentService != null) {
                            displayServiceData();
                        } else {
                            Toast.makeText(this, "Service data not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Service not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Error loading service: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayServiceData() {
        etTitle.setText(currentService.getTitle());
        etDescription.setText(currentService.getDescription());
        etPrice.setText(String.valueOf(currentService.getPrice()));
        etLocation.setText(currentService.getLocation());
        etDeliveryTime.setText(currentService.getDeliveryTime());
    }

    private void setupClickListeners() {
        btnUpdateService.setOnClickListener(v -> updateService());
        btnDeleteService.setOnClickListener(v -> confirmDelete());
    }

    private void updateService() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String deliveryTime = etDeliveryTime.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Service title is required");
            etTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Service description is required");
            etDescription.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                etPrice.setError("Price must be greater than 0");
                etPrice.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etPrice.setError("Please enter a valid price");
            etPrice.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(location)) {
            etLocation.setError("Location is required");
            etLocation.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(deliveryTime)) {
            etDeliveryTime.setError("Delivery time is required");
            etDeliveryTime.requestFocus();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login to update service", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("description", description);
        updates.put("price", price);
        updates.put("location", location);
        updates.put("deliveryTime", deliveryTime);

        servicesRef.child(serviceId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(EditServiceActivity.this, "Service updated successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(EditServiceActivity.this, "Failed to update: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void confirmDelete() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Service")
                .setMessage("Are you sure you want to delete '" + currentService.getTitle() + "'? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteService())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteService() {
        setLoading(true);
        servicesRef.child(serviceId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(EditServiceActivity.this, "Service deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(EditServiceActivity.this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnUpdateService.setEnabled(!isLoading);
        btnDeleteService.setEnabled(!isLoading);
        btnUpdateService.setText(isLoading ? "UPDATING..." : "UPDATE SERVICE");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}