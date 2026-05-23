package com.example.hustlefix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EntrepreneurDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Views
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ImageView ivAvatar;
    private MaterialCardView cardNotifications;
    private TextView tvGreeting, tvUserName, tvBusinessType;
    private TextView tvTotalJobs, tvActiveJobs, tvCompletedJobs;
    private TextView tvTotalEarnings, tvMonthlyEarnings;
    private MaterialCardView btnPostJob, btnFindWorkers, btnQuotes, btnChat, btnEmergency, btnRatings;
    private RecyclerView recyclerJobs;
    private View emptyState;
    private MaterialButton btnEmptyPostJob;
    private TextView tvViewAll;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;
    private String currentUserId = "";

    // Data
    private List<Job> jobList;
    private JobAdapter jobAdapter;
    private ValueEventListener jobsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrepreneur_dashboard);

        sharedPreferences = SessionHelper.prefs(this);

        initViews();
        setupToolbar();
        setupNavigationDrawer();
        setupFirebase();
        loadUserData();
        setupClickListeners();
        loadJobs();
        updateNavHeader();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        ivAvatar = findViewById(R.id.ivAvatar);
        cardNotifications = findViewById(R.id.cardNotifications);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvUserName = findViewById(R.id.tvUserName);
        tvBusinessType = findViewById(R.id.tvBusinessType);
        tvTotalJobs = findViewById(R.id.tvTotalJobs);
        tvActiveJobs = findViewById(R.id.tvActiveJobs);
        tvCompletedJobs = findViewById(R.id.tvCompletedJobs);
        tvTotalEarnings = findViewById(R.id.tvTotalEarnings);
        tvMonthlyEarnings = findViewById(R.id.tvMonthlyEarnings);
        tvViewAll = findViewById(R.id.tvViewAll);

        btnPostJob = findViewById(R.id.btnPostJob);
        btnFindWorkers = findViewById(R.id.btnFindWorkers);
        btnQuotes = findViewById(R.id.btnQuotes);
        btnChat = findViewById(R.id.btnChat);
        btnEmergency = findViewById(R.id.btnEmergency);
        btnRatings = findViewById(R.id.btnRatings);

        recyclerJobs = findViewById(R.id.recyclerJobs);
        emptyState = findViewById(R.id.emptyState);
        btnEmptyPostJob = findViewById(R.id.btnEmptyPostJob);

        jobList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Entrepreneur Dashboard");
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

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserData() {
        if (currentUser == null) return;

        String userName = currentUser.getDisplayName();
        if (userName == null || userName.isEmpty()) {
            String email = currentUser.getEmail();
            if (email != null && email.contains("@")) {
                userName = email.split("@")[0];
            } else {
                userName = "Entrepreneur";
            }
        }

        tvUserName.setText(userName);
        tvBusinessType.setText("Entrepreneur");

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            tvGreeting.setText("Good Morning!");
        } else if (hour < 16) {
            tvGreeting.setText("Good Afternoon!");
        } else {
            tvGreeting.setText("Good Evening!");
        }
    }

    private void setupClickListeners() {
        btnPostJob.setOnClickListener(v -> {
            Intent intent = new Intent(EntrepreneurDashboardActivity.this, PostJobActivity.class);
            startActivity(intent);
        });

        btnFindWorkers.setOnClickListener(v -> {
            Intent intent = new Intent(EntrepreneurDashboardActivity.this, FindWorkersActivity.class);
            startActivity(intent);
        });

        btnQuotes.setOnClickListener(v -> {
            Intent intent = new Intent(EntrepreneurDashboardActivity.this, QuotesActivity.class);
            startActivity(intent);
        });

        btnChat.setOnClickListener(v -> ChatLauncher.openChatList(EntrepreneurDashboardActivity.this));

        btnEmergency.setOnClickListener(v -> {
            Intent intent = new Intent(EntrepreneurDashboardActivity.this, EmergencyRequestActivity.class);
            startActivity(intent);
        });

        btnRatings.setOnClickListener(v -> {
            Intent intent = new Intent(EntrepreneurDashboardActivity.this, RatingsActivity.class);
            startActivity(intent);
        });

        btnEmptyPostJob.setOnClickListener(v -> {
            Intent intent = new Intent(EntrepreneurDashboardActivity.this, PostJobActivity.class);
            startActivity(intent);
        });

        cardNotifications.setOnClickListener(v -> {
            Toast.makeText(this, "Notifications coming soon!", Toast.LENGTH_SHORT).show();
        });

        tvViewAll.setOnClickListener(v -> {
            if (jobList != null && !jobList.isEmpty()) {
                showAllJobsDialog();
            } else {
                Toast.makeText(this, "No jobs to display", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadJobs() {
        if (currentUserId == null) return;

        recyclerJobs.setLayoutManager(new LinearLayoutManager(this));
        jobAdapter = new JobAdapter(jobList, currentUserId, "ENTREPRENEUR");
        recyclerJobs.setAdapter(jobAdapter);

        jobAdapter.setOnJobActionListener(new JobAdapter.OnJobActionListener() {
            @Override
            public void onViewDetails(Job job) {
                showJobDetailsDialog(job);
            }

            @Override
            public void onApply(Job job) {
                ApplicationsHelper.showApplicationsForJob(EntrepreneurDashboardActivity.this, job);
            }

            @Override
            public void onViewApplications(Job job) {
                ApplicationsHelper.showApplicationsForJob(EntrepreneurDashboardActivity.this, job);
            }

            @Override
            public void onDelete(Job job) {
                deleteJob(job);
            }

            @Override
            public void onChat(Job job) {
                ChatLauncher.openChatForJob(EntrepreneurDashboardActivity.this, job);
            }
        });

        jobsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                jobList.clear();
                int totalJobs = 0;
                int activeJobs = 0;
                int completedJobs = 0;
                double totalEarnings = 0;
                double monthlyEarnings = 0;

                long monthStart = getMonthStart();

                for (DataSnapshot jobSnapshot : snapshot.getChildren()) {
                    try {
                        Job job = jobSnapshot.getValue(Job.class);
                        if (job != null) {
                            job.setJobId(jobSnapshot.getKey());
                            jobList.add(job);
                            totalJobs++;

                            if ("completed".equals(job.getStatus())) {
                                completedJobs++;
                                totalEarnings += job.getBudget();
                                if (job.getCompletedDate() != null &&
                                        job.getCompletedDate().getTime() > monthStart) {
                                    monthlyEarnings += job.getBudget();
                                }
                            } else if ("open".equals(job.getStatus()) || "in_progress".equals(job.getStatus())) {
                                activeJobs++;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                tvTotalJobs.setText(String.valueOf(totalJobs));
                tvActiveJobs.setText(String.valueOf(activeJobs));
                tvCompletedJobs.setText(String.valueOf(completedJobs));
                tvTotalEarnings.setText(String.format("R%.2f", totalEarnings));
                tvMonthlyEarnings.setText(String.format("R%.2f", monthlyEarnings));

                if (jobAdapter != null) {
                    jobAdapter.updateList(jobList);
                }

                if (jobList.isEmpty()) {
                    recyclerJobs.setVisibility(View.GONE);
                    emptyState.setVisibility(View.VISIBLE);
                } else {
                    recyclerJobs.setVisibility(View.VISIBLE);
                    emptyState.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EntrepreneurDashboardActivity.this,
                        "Failed to load jobs: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        databaseReference.child("jobs")
                .orderByChild("postedBy")
                .equalTo(currentUserId)
                .addValueEventListener(jobsListener);
    }

    private long getMonthStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTimeInMillis();
    }

    private void showJobDetailsDialog(Job job) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(job.getTitle())
                .setMessage("Budget: " + job.getFormattedBudget() + "\n" +
                        "Location: " + job.getLocation() + "\n" +
                        "Category: " + job.getCategory() + "\n" +
                        "Deadline: " + job.getDeadline() + "\n" +
                        "Status: " + job.getStatus() + "\n" +
                        "Applications: " + job.getApplicationsCount() + "\n\n" +
                        job.getDescription())
                .setPositiveButton("Close", null)
                .setNeutralButton("Applications", (dialog, which) -> viewApplications(job))
                .show();
    }

    private void showAllJobsDialog() {
        StringBuilder allJobs = new StringBuilder();
        for (int i = 0; i < Math.min(jobList.size(), 10); i++) {
            Job job = jobList.get(i);
            allJobs.append(i + 1).append(". ").append(job.getTitle())
                    .append(" - ").append(job.getFormattedBudget())
                    .append(" (").append(job.getStatus()).append(")\n");
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("My Jobs (" + jobList.size() + ")")
                .setMessage(allJobs.toString())
                .setPositiveButton("OK", null)
                .setNeutralButton("View All", (dialog, which) -> {
                    Toast.makeText(this, "Full job list coming soon", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void viewApplications(Job job) {
        ApplicationsHelper.showApplicationsForJob(this, job);
    }

    private void deleteJob(Job job) {
        if (!job.isOwner(currentUserId)) {
            Toast.makeText(this, "You can only delete your own jobs", Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Job")
                .setMessage("Delete " + job.getTitle() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    databaseReference.child("jobs").child(job.getJobId()).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Job deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startChatWithUser(String userId, String userName) {
        ChatLauncher.openChat(this, userId, userName);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_logout) {
            logout();
            return true;
        }
        return NavigationHelper.handleNavigationItem(this, item.getItemId());
    }

    private void logout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (jobsListener != null) {
                        databaseReference.child("jobs").removeEventListener(jobsListener);
                    }
                    SessionHelper.logout(EntrepreneurDashboardActivity.this);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (jobsListener != null && databaseReference != null) {
            databaseReference.child("jobs").removeEventListener(jobsListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (jobsListener != null) {
            loadJobs();
        }
    }
}