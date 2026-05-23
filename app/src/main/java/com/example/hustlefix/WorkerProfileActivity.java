package com.example.hustlefix;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class WorkerProfileActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView ivProfileImage;
    private TextView tvInitials;
    private TextView tvWorkerName, tvWorkerSkill, tvWorkerLocation;
    private TextView tvRating, tvExperience, tvCompletedJobs, tvHourlyRate;
    private TextView tvAvailability, tvAbout;
    private RatingBar rbRating;
    private Button btnHire, btnChat;
    private LinearLayout btnCall, btnMessage;

    private DatabaseReference workerRef;
    private String workerId;
    private Worker worker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_profile);

        workerId = getIntent().getStringExtra("worker_id");
        if (workerId == null) {
            workerId = ChatLauncher.resolveOtherUserId(getIntent());
        }
        if (workerId == null) {
            Toast.makeText(this, "Worker not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadWorkerData();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvInitials = findViewById(R.id.tvInitials);
        tvWorkerName = findViewById(R.id.tvWorkerName);
        tvWorkerSkill = findViewById(R.id.tvWorkerSkill);
        tvWorkerLocation = findViewById(R.id.tvWorkerLocation);
        tvRating = findViewById(R.id.tvRating);
        tvExperience = findViewById(R.id.tvExperience);
        tvCompletedJobs = findViewById(R.id.tvCompletedJobs);
        tvHourlyRate = findViewById(R.id.tvHourlyRate);
        tvAvailability = findViewById(R.id.tvAvailability);
        tvAbout = findViewById(R.id.tvAbout);
        rbRating = findViewById(R.id.rbRating);
        btnHire = findViewById(R.id.btnHire);
        btnChat = findViewById(R.id.btnChat);
        btnCall = findViewById(R.id.btnCall);
        btnMessage = findViewById(R.id.btnMessage);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Worker Profile");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadWorkerData() {
        workerRef = FirebaseDatabase.getInstance().getReference("users").child(workerId);

        workerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                worker = snapshot.getValue(Worker.class);
                if (worker != null) {
                    displayWorkerData();
                } else {
                    Toast.makeText(WorkerProfileActivity.this, "Worker not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WorkerProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayWorkerData() {
        // Name and basic info
        tvWorkerName.setText(worker.getName() != null ? worker.getName() : "Unknown");
        tvWorkerSkill.setText(worker.getSkill() != null ? worker.getSkill() : "No skill specified");
        tvWorkerLocation.setText(worker.getLocation() != null ? worker.getLocation() : "Location not specified");

        // Stats
        tvRating.setText(worker.getFormattedRating());
        tvExperience.setText(worker.getExperience() + " years");
        tvCompletedJobs.setText(worker.getCompletedJobs() + " jobs completed");
        tvHourlyRate.setText(worker.getFormattedHourlyRate() + " / hour");
        rbRating.setRating((float) worker.getRating());

        // About
        if (worker.getAbout() != null && !worker.getAbout().isEmpty()) {
            tvAbout.setText(worker.getAbout());
        } else {
            tvAbout.setText("No description provided.");
        }

        // Availability
        if (worker.isAvailable()) {
            tvAvailability.setText("● Available for work");
            tvAvailability.setTextColor(0xFF4CAF50);
        } else {
            tvAvailability.setText("● Currently unavailable");
            tvAvailability.setTextColor(0xFFFF4444);
        }

        if (worker.getAvailability() != null && !worker.getAvailability().isEmpty()) {
            tvAvailability.append("\n" + worker.getAvailability());
        }

        // Profile image or initials
        if (worker.getProfileImage() != null && !worker.getProfileImage().isEmpty()) {
            Glide.with(this)
                    .load(worker.getProfileImage())
                    .placeholder(R.drawable.ic_profile_default)
                    .error(R.drawable.ic_profile_default)
                    .circleCrop()
                    .into(ivProfileImage);
            ivProfileImage.setVisibility(View.VISIBLE);
            tvInitials.setVisibility(View.GONE);
        } else {
            ivProfileImage.setVisibility(View.GONE);
            tvInitials.setVisibility(View.VISIBLE);
            tvInitials.setText(worker.getInitials());
        }
    }

    private void setupClickListeners() {
        // Hire button - opens FindWorkersActivity or Chat
        btnHire.setOnClickListener(v -> {
            // Show dialog with hiring options
            new androidx.appcompat.app.AlertDialog.Builder(WorkerProfileActivity.this)
                    .setTitle("Hire " + worker.getName())
                    .setMessage("How would you like to proceed?")
                    .setPositiveButton("Send Message", (dialog, which) -> {
                        // Open chat to discuss the job
                        ChatLauncher.openChat(WorkerProfileActivity.this, workerId, worker.getName());
                    })
                    .setNegativeButton("Find Workers", (dialog, which) -> {
                        // Go to Find Workers section
                        Intent intent = new Intent(WorkerProfileActivity.this, FindWorkersActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .setNeutralButton("Cancel", null)
                    .show();
        });

        // Chat button - opens direct chat
        btnChat.setOnClickListener(v -> {
            ChatLauncher.openChat(this, workerId, worker.getName());
        });

        // Call button
        btnCall.setOnClickListener(v -> {
            if (worker.getPhone() != null && !worker.getPhone().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + worker.getPhone()));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            }
        });

        // SMS button
        btnMessage.setOnClickListener(v -> {
            if (worker.getPhone() != null && !worker.getPhone().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("sms:" + worker.getPhone()));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            }
        });
    }
}