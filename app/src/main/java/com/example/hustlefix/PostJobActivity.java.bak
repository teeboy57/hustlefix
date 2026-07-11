package com.example.hustlefix;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PostJobActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextInputEditText etTitle, etDescription, etBudget, etLocation, etDeadline;
    private ChipGroup chipGroupCategory;
    private MaterialButtonToggleGroup toggleUrgency;
    private MaterialButton btnSubmitJob;
    private ProgressBar progressBar;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private String selectedCategory = "";
    private String selectedUrgency = "Normal";

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;
    private String userRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);

        sharedPreferences = SessionHelper.prefs(this);
        userRole = SessionHelper.getRole(this);

        initViews();
        setupToolbar();
        setupNavigationDrawer();
        setupFirebase();
        setupClickListeners();
        setupCategorySelection();
        setupUrgencySelection();
        updateNavHeader();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etBudget = findViewById(R.id.etBudget);
        etLocation = findViewById(R.id.etLocation);
        etDeadline = findViewById(R.id.etDeadline);

        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        toggleUrgency = findViewById(R.id.toggleUrgency);
        btnSubmitJob = findViewById(R.id.btnSubmitJob);
        progressBar = findViewById(R.id.progressBar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Post a Job");
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

        if (item.getItemId() == R.id.nav_home) {
            navigateToDashboard();
            return true;
        }
        if (item.getItemId() == R.id.nav_post_job) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        if (item.getItemId() == R.id.nav_logout) {
            logout();
            return true;
        }
        return NavigationHelper.handleNavigationItem(this, item.getItemId());
    }

    private void navigateToDashboard() {
        Intent intent;
        if ("ENTREPRENEUR".equals(userRole)) {
            intent = new Intent(PostJobActivity.this, EntrepreneurDashboardActivity.class);
        } else {
            intent = new Intent(PostJobActivity.this, ClientDashboardActivity.class);
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
            previewJob();
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
        databaseReference = FirebaseDatabase.getInstance().getReference("jobs");
    }

    private void setupClickListeners() {
        btnSubmitJob.setOnClickListener(v -> postJob());
        etDeadline.setOnClickListener(v -> showDatePickerDialog());
        etDeadline.setFocusable(false);
        etDeadline.setClickable(true);
    }

    private void setupCategorySelection() {
        if (chipGroupCategory != null) {
            chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds != null && !checkedIds.isEmpty()) {
                    int checkedId = checkedIds.get(0);
                    Chip selectedChip = findViewById(checkedId);
                    if (selectedChip != null) {
                        String rawCategory = selectedChip.getText().toString();
                        selectedCategory = rawCategory.replaceAll("[^a-zA-Z\\s]", "").trim();
                    }
                } else {
                    selectedCategory = "";
                }
            });
        }
    }

    private void setupUrgencySelection() {
        if (toggleUrgency != null) {
            toggleUrgency.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    if (checkedId == R.id.btnNormal) {
                        selectedUrgency = "Normal";
                    } else if (checkedId == R.id.btnUrgent) {
                        selectedUrgency = "Urgent";
                    } else if (checkedId == R.id.btnEmergency) {
                        selectedUrgency = "Emergency";
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
                    etDeadline.setText(date);
                }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void postJob() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String budget = etBudget.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String deadline = etDeadline.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Job title is required");
            etTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Job description is required");
            etDescription.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(budget)) {
            etBudget.setError("Budget is required");
            etBudget.requestFocus();
            return;
        }

        try {
            Double.parseDouble(budget);
        } catch (NumberFormatException e) {
            etBudget.setError("Please enter a valid budget amount");
            etBudget.requestFocus();
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

        if (TextUtils.isEmpty(deadline)) {
            Toast.makeText(this, "Please select a deadline", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login to post a job", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);

        String jobId = databaseReference.push().getKey();
        if (jobId == null) {
            setLoading(false);
            Toast.makeText(this, "Failed to generate job ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> job = new HashMap<>();
        job.put("jobId", jobId);
        job.put("title", title);
        job.put("description", description);
        job.put("budget", Double.parseDouble(budget));
        job.put("location", location);
        job.put("category", selectedCategory);
        job.put("deadline", deadline);
        job.put("urgency", selectedUrgency);
        job.put("status", "open");
        job.put("postedBy", currentUser.getUid());
        job.put("postedByName", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");
        job.put("postedByEmail", currentUser.getEmail());
        job.put("timestamp", System.currentTimeMillis());
        job.put("applicationsCount", 0);

        databaseReference.child(jobId).setValue(job)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(PostJobActivity.this, "Job posted successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(PostJobActivity.this, "Failed to post job: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveAsDraft() {
        Toast.makeText(this, "Saved as draft", Toast.LENGTH_SHORT).show();
    }

    private void previewJob() {
        String title = etTitle.getText().toString();
        if (!title.isEmpty()) {
            Toast.makeText(this, "Preview: " + title, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Enter job title first", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Clear Form")
                .setMessage("Are you sure you want to clear all fields?")
                .setPositiveButton("Clear", (dialog, which) -> {
                    etTitle.setText("");
                    etDescription.setText("");
                    etBudget.setText("");
                    etLocation.setText("");
                    etDeadline.setText("");
                    chipGroupCategory.clearCheck();
                    selectedCategory = "";
                    Toast.makeText(this, "Form cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showHelp() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Post a Job - Help")
                .setMessage("Fill in all the required fields:\n\n" +
                        "• Job Title: A clear, descriptive title\n" +
                        "• Description: Detailed job requirements\n" +
                        "• Category: Select the appropriate category\n" +
                        "• Budget: Your budget for this job\n" +
                        "• Deadline: When you need the job completed\n" +
                        "• Location: Where the job will be performed\n\n" +
                        "Jobs will be visible to all workers on the platform.")
                .setPositiveButton("Got it", null)
                .show();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSubmitJob.setEnabled(!isLoading);
        btnSubmitJob.setText(isLoading ? "POSTING..." : "POST JOB");
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