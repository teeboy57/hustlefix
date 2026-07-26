package com.example.hustlefix;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PostServiceActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private TextInputEditText etTitle, etDescription, etPrice, etLocation, etDeliveryTime;
    private ChipGroup chipGroupCategory;
    private MaterialButtonToggleGroup toggleAvailability;
    private MaterialButton btnSubmitService;
    private ProgressBar progressBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private String selectedCategory = "";
    private String selectedAvailability = "Available";
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String userRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);
        userRole = SessionHelper.getRole(this);
        initViews();
        setupToolbar();
        setupNavigationDrawer();
        setupFirebase();
        setupClickListeners();
        setupCategorySelection();
        setupAvailabilitySelection();
        updateNavHeader();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etBudget);
        etLocation = findViewById(R.id.etLocation);
        etDeliveryTime = findViewById(R.id.etDeadline);
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        toggleAvailability = findViewById(R.id.toggleUrgency);
        btnSubmitService = findViewById(R.id.btnSubmitJob);
        progressBar = findViewById(R.id.progressBar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Post Service");
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Post a Service");
        }
    }

    private void setupNavigationDrawer() {
        NavigationHelper.setupDrawer(this, drawerLayout, toolbar, navigationView);
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
                tvNavUserName.setText(name);
                tvNavUserEmail.setText(currentUser.getEmail());
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            navigateToDashboard();
            return true;
        }
        if (id == R.id.nav_post_job) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        if (id == R.id.nav_logout) {
            logout();
            return true;
        }
        return NavigationHelper.handleNavigationItem(this, id);
    }

    private void navigateToDashboard() {
        Intent intent;
        if ("service_provider".equals(userRole)) {
            intent = new Intent(PostServiceActivity.this, ServiceProviderDashboardActivity.class);
        } else {
            intent = new Intent(PostServiceActivity.this, ClientDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void logout() {
        SessionHelper.logout(this);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post_job, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save_draft) {
            saveAsDraft();
            return true;
        } else if (id == R.id.action_preview) {
            previewService();
            return true;
        } else if (id == R.id.action_clear) {
            clearForm();
            return true;
        } else if (id == R.id.action_help) {
            showHelp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("services");
    }

    private void setupClickListeners() {
        btnSubmitService.setOnClickListener(v -> postService());
        etDeliveryTime.setOnClickListener(v -> showDatePickerDialog());
        etDeliveryTime.setFocusable(false);
        etDeliveryTime.setClickable(true);
    }

    private void setupCategorySelection() {
        if (chipGroupCategory != null) {
            chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds != null && !checkedIds.isEmpty()) {
                    int checkedId = checkedIds.get(0);
                    if (checkedId == R.id.chipPlumber) {
                        selectedCategory = "Plumber";
                    } else if (checkedId == R.id.chipElectrician) {
                        selectedCategory = "Electrician";
                    } else {
                        selectedCategory = "Other";
                    }
                } else {
                    selectedCategory = "";
                }
            });
        }
    }

    private void setupAvailabilitySelection() {
        if (toggleAvailability != null) {
            toggleAvailability.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    if (checkedId == R.id.btnNormal) {
                        selectedAvailability = "Available";
                    } else if (checkedId == R.id.btnUrgent) {
                        selectedAvailability = "Urgent";
                    } else if (checkedId == R.id.btnEmergency) {
                        selectedAvailability = "Waitlist";
                    }
                }
            });
        }
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    String date = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    etDeliveryTime.setText(date);
                }, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void postService() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String deliveryTime = etDeliveryTime.getText().toString().trim();

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
        if (TextUtils.isEmpty(price)) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return;
        }
        try {
            Double.parseDouble(price);
        } catch (NumberFormatException e) {
            etPrice.setError("Please enter a valid price");
            etPrice.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(selectedCategory)) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(location)) {
            etLocation.setError("Location is required");
            etLocation.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(deliveryTime)) {
            Toast.makeText(this, "Please select estimated delivery time", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login to post a service", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);

        // Fetch current user's profile image before posting
        FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String profileImage = snapshot.child("profileImage").getValue(String.class);
                        proceedWithPosting(currentUser, title, description, price, location, deliveryTime, profileImage);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        setLoading(false);
                        Toast.makeText(PostServiceActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void proceedWithPosting(FirebaseUser currentUser, String title, String description, String price, String location, String deliveryTime, String profileImage) {
        String serviceId = databaseReference.push().getKey();
        if (serviceId == null) {
            setLoading(false);
            Toast.makeText(this, "Failed to generate service ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> service = new HashMap<>();
        service.put("serviceId", serviceId);
        service.put("title", title);
        service.put("description", description);
        service.put("price", Double.parseDouble(price));
        service.put("location", location);
        service.put("category", selectedCategory);
        service.put("deliveryTime", deliveryTime);
        service.put("availability", selectedAvailability);
        service.put("status", "active");
        service.put("serviceProviderId", currentUser.getUid());
        service.put("serviceProviderName", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");
        service.put("serviceProviderEmail", currentUser.getEmail());
        service.put("serviceProviderProfileImageUrl", profileImage);
        service.put("createdAt", System.currentTimeMillis());
        service.put("bookingsCount", 0);
        service.put("averageRating", 0);

        databaseReference.child(serviceId).setValue(service)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(PostServiceActivity.this, "Service posted successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(PostServiceActivity.this, "Failed to post service: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveAsDraft() {
        Toast.makeText(this, "Saved as draft", Toast.LENGTH_SHORT).show();
    }

    private void previewService() {
        String title = etTitle.getText().toString();
        if (!title.isEmpty()) {
            Toast.makeText(this, "Preview: " + title, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Enter service title first", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Clear Form")
                .setMessage("Are you sure you want to clear all fields?")
                .setPositiveButton("Clear", (dialog, which) -> {
                    etTitle.setText("");
                    etDescription.setText("");
                    etPrice.setText("");
                    etLocation.setText("");
                    etDeliveryTime.setText("");
                    chipGroupCategory.clearCheck();
                    selectedCategory = "";
                    Toast.makeText(this, "Form cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showHelp() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Post a Service - Help")
                .setMessage("Fill in all the required fields:\n\n" +
                        "• Service Title: A clear, descriptive title\n" +
                        "• Description: Detailed service description\n" +
                        "• Category: Select the appropriate category\n" +
                        "• Price: Your service price\n" +
                        "• Delivery Time: Estimated time to complete\n" +
                        "• Location: Where you provide the service\n\n" +
                        "Your service will be visible to all clients on the platform.")
                .setPositiveButton("Got it", null)
                .show();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSubmitService.setEnabled(!isLoading);
        btnSubmitService.setText(isLoading ? "POSTING..." : "POST SERVICE");
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}