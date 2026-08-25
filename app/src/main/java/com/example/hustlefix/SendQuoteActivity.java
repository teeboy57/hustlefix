package com.example.hustlefix;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;
public class SendQuoteActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView tvJobTitle, tvJobBudget, tvJobDescription;
    private EditText etQuoteAmount, etTimeline, etMessage;
    private Button btnSendQuote;
    private ProgressBar progressBar;
    private DatabaseReference quotesRef;
    private FirebaseUser currentUser;
    private String jobId;
    private String jobTitle;
    private String jobDescription;
    private double jobBudget;
    private String clientId;
    private String clientName;
    private String workerDisplayName = "Worker";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_quote);
        jobId = getIntent().getStringExtra("job_id");
        if (jobId == null) {
            jobId = getIntent().getStringExtra("jobId");
        }
        jobTitle = getIntent().getStringExtra("job_title");
        jobDescription = getIntent().getStringExtra("job_description");
        jobBudget = getIntent().getDoubleExtra("job_budget", 0);
        clientId = getIntent().getStringExtra("client_id");
        clientName = getIntent().getStringExtra("client_name");
        if (jobId == null) {
            Toast.makeText(this, "Job not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initViews();
        setupToolbar();
        setupFirebase();
        displayJobInfo();
        setupClickListeners();
    }
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvJobTitle = findViewById(R.id.tvJobTitle);
        tvJobBudget = findViewById(R.id.tvJobBudget);
        tvJobDescription = findViewById(R.id.tvJobDescription);
        etQuoteAmount = findViewById(R.id.etQuoteAmount);
        etTimeline = findViewById(R.id.etTimeline);
        etMessage = findViewById(R.id.etMessage);
        btnSendQuote = findViewById(R.id.btnSendQuote);
        progressBar = findViewById(R.id.progressBar);
    }
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Send Quote");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    private void setupFirebase() {
        quotesRef = FirebaseDatabase.getInstance().getReference("quotes");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
            workerDisplayName = currentUser.getDisplayName();
        }
        FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid())
                .child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        if (name != null && !name.isEmpty()) {
                            workerDisplayName = name;
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
    private void displayJobInfo() {
        tvJobTitle.setText(jobTitle != null ? jobTitle : "Unknown Job");
        tvJobBudget.setText(String.format("Budget: R%.2f", jobBudget));
        tvJobDescription.setText(jobDescription != null ? jobDescription : "No description provided");
        // Suggest a quote amount
        etQuoteAmount.setText(String.valueOf(jobBudget * 0.9));
    }
    private void setupClickListeners() {
        btnSendQuote.setOnClickListener(v -> sendQuote());
    }
    private void sendQuote() {
        String amountStr = etQuoteAmount.getText().toString().trim();
        String timeline = etTimeline.getText().toString().trim();
        String message = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            etQuoteAmount.setError("Amount is required");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etQuoteAmount.setError("Amount must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            etQuoteAmount.setError("Invalid amount");
            return;
        }
        if (TextUtils.isEmpty(timeline)) {
            etTimeline.setError("Timeline is required");
            return;
        }
        setLoading(true);
        
        Quote quote = new Quote(
                jobId, jobTitle,
                currentUser.getUid(), workerDisplayName,
                clientId, clientName,
                message, amount, timeline
        );
        
        quotesRef.child(jobId).child(currentUser.getUid()).setValue(quote)
                .addOnSuccessListener(aVoid -> {
                    // Update job application count
                    FirebaseDatabase.getInstance().getReference("jobs").child(jobId).child("applicationsCount")
                        .runTransaction(new com.google.firebase.database.Transaction.Handler() {
                            @Override
                            public com.google.firebase.database.Transaction.Result doTransaction(com.google.firebase.database.MutableData currentData) {
                                Integer count = currentData.getValue(Integer.class);
                                if (count == null) count = 0;
                                currentData.setValue(count + 1);
                                return com.google.firebase.database.Transaction.success(currentData);
                            }
                            @Override
                            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {}
                        });
                        
                    setLoading(false);
                    Toast.makeText(SendQuoteActivity.this,
                            "Quote sent successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(SendQuoteActivity.this,
                            "Failed to send quote: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSendQuote.setEnabled(!isLoading);
        btnSendQuote.setText(isLoading ? "SENDING..." : "SEND QUOTE");
    }
}