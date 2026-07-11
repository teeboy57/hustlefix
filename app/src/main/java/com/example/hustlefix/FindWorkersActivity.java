package com.example.hustlefix;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
public class FindWorkersActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerWorkers;
    private EditText etSearch;
    private ImageView ivClearSearch;
    private ChipGroup chipGroupCategories;
    private TextView tvResultsCount;
    private ProgressBar progressBar;
    private View emptyState;
    private Button btnClearFilters;
    private DatabaseReference databaseReference;
    private List<Worker> workerList;
    private WorkerAdapter adapter;
    private String currentFilter = "All";
    private String searchQuery = "";
    private FirebaseUser currentUser;
    private ValueEventListener workersListener;
    // Job details for sending quotes
    private DatabaseReference jobsRef;
    private List<Job> jobList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_workers);
        initViews();
        setupToolbar();
        setupFirebase();
        setupSearch();
        setupCategoryFilters();
        loadWorkers();
        btnClearFilters.setOnClickListener(v -> {
            currentFilter = "All";
            searchQuery = "";
            etSearch.setText("");
            Chip allChip = findViewById(R.id.chipAll);
            if (allChip != null) allChip.setChecked(true);
            filterWorkers();
        });
    }
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerWorkers = findViewById(R.id.recyclerWorkers);
        etSearch = findViewById(R.id.etSearch);
        ivClearSearch = findViewById(R.id.ivClearSearch);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        tvResultsCount = findViewById(R.id.tvResultsCount);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
        btnClearFilters = findViewById(R.id.btnClearFilters);
        recyclerWorkers.setLayoutManager(new LinearLayoutManager(this));
        workerList = new ArrayList<>();
        adapter = new WorkerAdapter(workerList, this);
        recyclerWorkers.setAdapter(adapter);
        jobList = new ArrayList<>();
        jobsRef = FirebaseDatabase.getInstance().getReference("jobs");
    }
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Find Workers");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_navigation, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return NavigationHelper.onOptionsItemSelected(this, item) || super.onOptionsItemSelected(item);
    }
    private void setupFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase();
                ivClearSearch.setVisibility(searchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                filterWorkers();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        ivClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            searchQuery = "";
            ivClearSearch.setVisibility(View.GONE);
            filterWorkers();
        });
    }
    private void setupCategoryFilters() {
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds != null && !checkedIds.isEmpty()) {
                Chip chip = findViewById(checkedIds.get(0));
                String text = chip.getText().toString();
                currentFilter = text.replaceAll("[^a-zA-Z]", "");
                if (currentFilter.equals("All")) currentFilter = "All";
            } else {
                currentFilter = "All";
                Chip allChip = findViewById(R.id.chipAll);
                if (allChip != null) allChip.setChecked(true);
            }
            filterWorkers();
        });
    }
    private void loadWorkers() {
        progressBar.setVisibility(View.VISIBLE);
        workersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                workerList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Worker worker = data.getValue(Worker.class);
                    if (worker != null && "worker".equals(worker.getRole())) {
                        worker.setId(data.getKey());
                        if (currentUser == null || !data.getKey().equals(currentUser.getUid())) {
                            workerList.add(worker);
                        }
                    }
                }
                progressBar.setVisibility(View.GONE);
                filterWorkers();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FindWorkersActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        databaseReference.orderByChild("role").equalTo("worker").addValueEventListener(workersListener);
    }
    private void filterWorkers() {
        List<Worker> filtered = new ArrayList<>();
        for (Worker worker : workerList) {
            boolean categoryMatch = currentFilter.equals("All") ||
                    (worker.getCategory() != null && worker.getCategory().equalsIgnoreCase(currentFilter));
            boolean searchMatch = searchQuery.isEmpty() ||
                    (worker.getName() != null && worker.getName().toLowerCase().contains(searchQuery)) ||
                    (worker.getSkill() != null && worker.getSkill().toLowerCase().contains(searchQuery)) ||
                    (worker.getLocation() != null && worker.getLocation().toLowerCase().contains(searchQuery));
            if (categoryMatch && searchMatch) {
                filtered.add(worker);
            }
        }
        adapter.updateList(filtered);
        tvResultsCount.setText(filtered.size() + " workers found");
        if (filtered.isEmpty()) {
            recyclerWorkers.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerWorkers.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }
    // Method to show job details and send quote
    public void showJobDetailsDialog(Job job) {
        if (job == null) {
            Toast.makeText(this, "Job not found", Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(job.getTitle())
                .setMessage("Budget: " + job.getFormattedBudget() + "\n" +
                        "Location: " + job.getLocation() + "\n" +
                        "Category: " + job.getCategory() + "\n" +
                        "Deadline: " + job.getDeadline() + "\n\n" +
                        job.getDescription())
                .setPositiveButton("Send Quote", (dialog, which) -> {
                    // Check if user is logged in
                    if (currentUser == null) {
                        Toast.makeText(FindWorkersActivity.this,
                                "Please login to send a quote", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Open SendQuoteActivity
                    Intent intent = new Intent(FindWorkersActivity.this, SendQuoteActivity.class);
                    intent.putExtra("job_id", job.getJobId());
                    intent.putExtra("job_title", job.getTitle());
                    intent.putExtra("job_description", job.getDescription());
                    intent.putExtra("job_budget", job.getBudget());
                    intent.putExtra("client_id", job.getPostedBy());
                    intent.putExtra("client_name", job.getPostedByName());
                    startActivity(intent);
                })
                .setNegativeButton("Close", null)
                .setNeutralButton("Chat", (dialog, which) ->
                        ChatLauncher.openChat(FindWorkersActivity.this, job.getPostedBy(), job.getPostedByName()))
                .show();
    }
    // Method to show available jobs for quoting
    public void showAvailableJobsDialog() {
        progressBar.setVisibility(View.VISIBLE);
        jobsRef.orderByChild("status").equalTo("open").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                jobList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Job job = data.getValue(Job.class);
                    if (job != null) {
                        job.setJobId(data.getKey());
                        // Don't show user's own jobs
                        if (currentUser != null && !job.getPostedBy().equals(currentUser.getUid())) {
                            jobList.add(job);
                        }
                    }
                }
                progressBar.setVisibility(View.GONE);
                if (jobList.isEmpty()) {
                    Toast.makeText(FindWorkersActivity.this,
                            "No available jobs at the moment", Toast.LENGTH_SHORT).show();
                    return;
                }
                showJobSelectionDialog();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FindWorkersActivity.this,
                        "Failed to load jobs", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showJobSelectionDialog() {
        String[] jobTitles = new String[jobList.size()];
        for (int i = 0; i < jobList.size(); i++) {
            jobTitles[i] = jobList.get(i).getTitle() + " - " + jobList.get(i).getFormattedBudget();
        }
        new AlertDialog.Builder(this)
                .setTitle("Select Job to Quote")
                .setItems(jobTitles, (dialog, which) -> {
                    Job selectedJob = jobList.get(which);
                    showJobDetailsDialog(selectedJob);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    public void hireWorker(Worker worker) {
        if (currentUser == null) {
            Toast.makeText(this, "Please login to hire workers", Toast.LENGTH_SHORT).show();
            return;
        }
        // Show hiring dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_hire_worker, null);
        TextView tvWorkerName = dialogView.findViewById(R.id.tvWorkerName);
        EditText etJobTitle = dialogView.findViewById(R.id.etJobTitle);
        EditText etJobDescription = dialogView.findViewById(R.id.etJobDescription);
        EditText etBudget = dialogView.findViewById(R.id.etBudget);
        EditText etDeadline = dialogView.findViewById(R.id.etDeadline);
        RatingBar rbProposedRating = dialogView.findViewById(R.id.rbProposedRating);
        tvWorkerName.setText(worker.getName());
        new MaterialAlertDialogBuilder(this)
                .setTitle("Hire " + worker.getName())
                .setView(dialogView)
                .setPositiveButton("Send Offer", (dialog, which) -> {
                    String jobTitle = etJobTitle.getText().toString().trim();
                    String jobDescription = etJobDescription.getText().toString().trim();
                    String budgetStr = etBudget.getText().toString().trim();
                    String deadline = etDeadline.getText().toString().trim();
                    float proposedRating = rbProposedRating.getRating();
                    if (jobTitle.isEmpty()) {
                        Toast.makeText(this, "Please enter a job title", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (jobDescription.isEmpty()) {
                        Toast.makeText(this, "Please enter job description", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (budgetStr.isEmpty()) {
                        Toast.makeText(this, "Please enter budget", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double budget = Double.parseDouble(budgetStr);
                    // Create hire request in Firebase
                    DatabaseReference hireRef = FirebaseDatabase.getInstance().getReference("hire_requests");
                    String requestId = hireRef.push().getKey();
                    if (requestId != null) {
                        FindWorkersActivity.HireRequest hireRequest = new FindWorkersActivity.HireRequest(
                                requestId,
                                currentUser.getUid(),
                                currentUser.getDisplayName(),
                                worker.getId(),
                                worker.getName(),
                                jobTitle,
                                jobDescription,
                                budget,
                                deadline,
                                proposedRating,
                                System.currentTimeMillis(),
                                "pending"
                        );
                        hireRef.child(requestId).setValue(hireRequest)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(FindWorkersActivity.this,
                                            "Hire request sent to " + worker.getName(), Toast.LENGTH_LONG).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(FindWorkersActivity.this,
                                            "Failed to send request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    public void viewWorkerProfile(Worker worker) {
        Intent intent = new Intent(FindWorkersActivity.this, WorkerProfileActivity.class);
        intent.putExtra("worker_id", worker.getId());
        intent.putExtra("worker_name", worker.getName());
        startActivity(intent);
    }
    public void chatWithWorker(Worker worker) {
        ChatLauncher.openChat(this, worker.getId(), worker.getName());
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (workersListener != null && databaseReference != null) {
            databaseReference.removeEventListener(workersListener);
        }
    }
    // HireRequest Model Class
    public static class HireRequest {
        private String id;
        private String clientId;
        private String clientName;
        private String workerId;
        private String workerName;
        private String jobTitle;
        private String jobDescription;
        private double budget;
        private String deadline;
        private float proposedRating;
        private long timestamp;
        private String status;
        public HireRequest() {}
        public HireRequest(String id, String clientId, String clientName, String workerId,
                           String workerName, String jobTitle, String jobDescription,
                           double budget, String deadline, float proposedRating,
                           long timestamp, String status) {
            this.id = id;
            this.clientId = clientId;
            this.clientName = clientName;
            this.workerId = workerId;
            this.workerName = workerName;
            this.jobTitle = jobTitle;
            this.jobDescription = jobDescription;
            this.budget = budget;
            this.deadline = deadline;
            this.proposedRating = proposedRating;
            this.timestamp = timestamp;
            this.status = status;
        }
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public String getClientName() { return clientName; }
        public void setClientName(String clientName) { this.clientName = clientName; }
        public String getWorkerId() { return workerId; }
        public void setWorkerId(String workerId) { this.workerId = workerId; }
        public String getWorkerName() { return workerName; }
        public void setWorkerName(String workerName) { this.workerName = workerName; }
        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
        public String getJobDescription() { return jobDescription; }
        public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }
        public double getBudget() { return budget; }
        public void setBudget(double budget) { this.budget = budget; }
        public String getDeadline() { return deadline; }
        public void setDeadline(String deadline) { this.deadline = deadline; }
        public float getProposedRating() { return proposedRating; }
        public void setProposedRating(float proposedRating) { this.proposedRating = proposedRating; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}