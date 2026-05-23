package com.example.hustlefix;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class HireWorkerActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvWorkerName;
    private EditText etJobTitle, etJobDescription, etBudget, etDeadline;
    private RatingBar rbProposedRating;
    private Button btnSubmitHire;
    private ProgressBar progressBar;

    private DatabaseReference hireRequestsRef;
    private FirebaseUser currentUser;
    private String workerId;
    private String workerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hire_worker);

        // Get worker info from intent
        workerId = getIntent().getStringExtra("worker_id");
        workerName = getIntent().getStringExtra("worker_name");

        if (workerId == null || workerName == null) {
            Toast.makeText(this, "Worker information missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupFirebase();
        setupClickListeners();

        tvWorkerName.setText(workerName);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvWorkerName = findViewById(R.id.tvWorkerName);
        etJobTitle = findViewById(R.id.etJobTitle);
        etJobDescription = findViewById(R.id.etJobDescription);
        etBudget = findViewById(R.id.etBudget);
        etDeadline = findViewById(R.id.etDeadline);
        rbProposedRating = findViewById(R.id.rbProposedRating);
        btnSubmitHire = findViewById(R.id.btnSubmitHire);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Hire Worker");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupFirebase() {
        hireRequestsRef = FirebaseDatabase.getInstance().getReference("hire_requests");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please login to hire workers", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupClickListeners() {
        etDeadline.setOnClickListener(v -> showDatePickerDialog());
        etDeadline.setFocusable(false);
        etDeadline.setClickable(true);

        btnSubmitHire.setOnClickListener(v -> submitHireRequest());
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

    private void submitHireRequest() {
        String jobTitle = etJobTitle.getText().toString().trim();
        String jobDescription = etJobDescription.getText().toString().trim();
        String budgetStr = etBudget.getText().toString().trim();
        String deadline = etDeadline.getText().toString().trim();
        float proposedRating = rbProposedRating.getRating();

        // Validate inputs
        if (TextUtils.isEmpty(jobTitle)) {
            etJobTitle.setError("Job title is required");
            etJobTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(jobDescription)) {
            etJobDescription.setError("Job description is required");
            etJobDescription.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(budgetStr)) {
            etBudget.setError("Budget is required");
            etBudget.requestFocus();
            return;
        }

        double budget;
        try {
            budget = Double.parseDouble(budgetStr);
            if (budget <= 0) {
                etBudget.setError("Budget must be greater than 0");
                etBudget.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etBudget.setError("Please enter a valid budget amount");
            etBudget.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(deadline)) {
            Toast.makeText(this, "Please select a deadline", Toast.LENGTH_SHORT).show();
            return;
        }

        if (proposedRating == 0) {
            Toast.makeText(this, "Please set an expected rating", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // Create hire request
        String requestId = hireRequestsRef.push().getKey();
        if (requestId == null) {
            setLoading(false);
            Toast.makeText(this, "Failed to create request", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> hireRequest = new HashMap<>();
        hireRequest.put("id", requestId);
        hireRequest.put("clientId", currentUser.getUid());
        hireRequest.put("clientName", currentUser.getDisplayName() != null ?
                currentUser.getDisplayName() : currentUser.getEmail());
        hireRequest.put("workerId", workerId);
        hireRequest.put("workerName", workerName);
        hireRequest.put("jobTitle", jobTitle);
        hireRequest.put("jobDescription", jobDescription);
        hireRequest.put("budget", budget);
        hireRequest.put("deadline", deadline);
        hireRequest.put("proposedRating", proposedRating);
        hireRequest.put("timestamp", System.currentTimeMillis());
        hireRequest.put("status", "pending");

        hireRequestsRef.child(requestId).setValue(hireRequest)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    showSuccessDialog();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(HireWorkerActivity.this,
                            "Failed to send request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Request Sent!")
                .setMessage("Your hire request has been sent to " + workerName +
                        ". They will review and respond shortly.\n\n" +
                        "You can track the status in your Quotes section.")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSubmitHire.setEnabled(!isLoading);
        btnSubmitHire.setText(isLoading ? "SENDING..." : "SEND HIRE REQUEST");
    }
}