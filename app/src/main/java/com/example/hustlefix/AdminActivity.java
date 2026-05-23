package com.example.hustlefix;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminActivity extends AppCompatActivity {

    private Button btnAddWorkers, btnClearWorkers;
    private ProgressBar progressBar;
    private DatabaseReference usersRef;
    private Toolbar toolbar;
    private int workersAdded = 0;
    private final int TOTAL_WORKERS = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        initViews();
        setupToolbar();
        setupFirebase();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        btnAddWorkers = findViewById(R.id.btnAddWorkers);
        btnClearWorkers = findViewById(R.id.btnClearWorkers);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Admin Panel");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupFirebase() {
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    private void setupClickListeners() {
        btnAddWorkers.setOnClickListener(v -> addSampleWorkers());
        btnClearWorkers.setOnClickListener(v -> confirmClearAllWorkers());
    }

    private void addSampleWorkers() {
        progressBar.setVisibility(View.VISIBLE);
        btnAddWorkers.setEnabled(false);
        btnClearWorkers.setEnabled(false);
        workersAdded = 0;

        // Worker 1: Plumber
        Worker worker1 = new Worker();
        worker1.setName("John Smith");
        worker1.setEmail("john.smith@example.com");
        worker1.setPhone("0712345678");
        worker1.setSkill("Plumbing");
        worker1.setCategory("Plumber");
        worker1.setLocation("Cape Town");
        worker1.setRole("worker");
        worker1.setExperience(8);
        worker1.setCompletedJobs(156);
        worker1.setRating(4.8);
        worker1.setHourlyRate(350);
        worker1.setAbout("Expert plumber with 8 years experience. Specializing in residential and commercial plumbing.");
        worker1.setAvailable(true);
        worker1.setAvailability("Monday - Saturday, 8am - 6pm");

        // Worker 2: Electrician
        Worker worker2 = new Worker();
        worker2.setName("Sarah Johnson");
        worker2.setEmail("sarah.johnson@example.com");
        worker2.setPhone("0823456789");
        worker2.setSkill("Electrical");
        worker2.setCategory("Electrician");
        worker2.setLocation("Johannesburg");
        worker2.setRole("worker");
        worker2.setExperience(5);
        worker2.setCompletedJobs(89);
        worker2.setRating(4.9);
        worker2.setHourlyRate(400);
        worker2.setAbout("Certified electrician with 5 years experience. Specializes in home wiring and installations.");
        worker2.setAvailable(true);
        worker2.setAvailability("Monday - Friday, 9am - 5pm");

        // Worker 3: Carpenter
        Worker worker3 = new Worker();
        worker3.setName("Mike Peterson");
        worker3.setEmail("mike.peterson@example.com");
        worker3.setPhone("0834567890");
        worker3.setSkill("Carpentry");
        worker3.setCategory("Carpenter");
        worker3.setLocation("Durban");
        worker3.setRole("worker");
        worker3.setExperience(10);
        worker3.setCompletedJobs(234);
        worker3.setRating(4.7);
        worker3.setHourlyRate(320);
        worker3.setAbout("Master carpenter with 10 years experience. Custom furniture and renovations.");
        worker3.setAvailable(true);
        worker3.setAvailability("Monday - Friday, 7am - 7pm");

        // Worker 4: Painter
        Worker worker4 = new Worker();
        worker4.setName("Linda Ndlovu");
        worker4.setEmail("linda.ndlovu@example.com");
        worker4.setPhone("0845678901");
        worker4.setSkill("Painting");
        worker4.setCategory("Painter");
        worker4.setLocation("Pretoria");
        worker4.setRole("worker");
        worker4.setExperience(6);
        worker4.setCompletedJobs(112);
        worker4.setRating(4.8);
        worker4.setHourlyRate(280);
        worker4.setAbout("Professional painter, interior and exterior specialist.");
        worker4.setAvailable(true);
        worker4.setAvailability("Monday - Saturday, 8am - 5pm");

        // Worker 5: Cleaner
        Worker worker5 = new Worker();
        worker5.setName("Thabo Molefe");
        worker5.setEmail("thabo.molefe@example.com");
        worker5.setPhone("0856789012");
        worker5.setSkill("Cleaning");
        worker5.setCategory("Cleaner");
        worker5.setLocation("Johannesburg");
        worker5.setRole("worker");
        worker5.setExperience(4);
        worker5.setCompletedJobs(67);
        worker5.setRating(4.6);
        worker5.setHourlyRate(180);
        worker5.setAbout("Deep cleaning, office cleaning, and home cleaning services.");
        worker5.setAvailable(true);
        worker5.setAvailability("Monday - Sunday, 6am - 9pm");

        // Worker 6: Mechanic
        Worker worker6 = new Worker();
        worker6.setName("David Mbeki");
        worker6.setEmail("david.mbeki@example.com");
        worker6.setPhone("0867890123");
        worker6.setSkill("Mechanical");
        worker6.setCategory("Mechanic");
        worker6.setLocation("Port Elizabeth");
        worker6.setRole("worker");
        worker6.setExperience(7);
        worker6.setCompletedJobs(178);
        worker6.setRating(4.7);
        worker6.setHourlyRate(380);
        worker6.setAbout("Certified mechanic. Car repairs, diagnostics, and maintenance.");
        worker6.setAvailable(true);
        worker6.setAvailability("Monday - Saturday, 8am - 6pm");

        // Worker 7: Gardener
        Worker worker7 = new Worker();
        worker7.setName("Grace Mkhize");
        worker7.setEmail("grace.mkhize@example.com");
        worker7.setPhone("0878901234");
        worker7.setSkill("Gardening");
        worker7.setCategory("Gardener");
        worker7.setLocation("Bloemfontein");
        worker7.setRole("worker");
        worker7.setExperience(5);
        worker7.setCompletedJobs(94);
        worker7.setRating(4.5);
        worker7.setHourlyRate(200);
        worker7.setAbout("Professional gardener. Lawn mowing, tree trimming, garden design.");
        worker7.setAvailable(true);
        worker7.setAvailability("Wednesday - Sunday, 7am - 4pm");

        // Worker 8: IT Support
        Worker worker8 = new Worker();
        worker8.setName("Peter van der Merwe");
        worker8.setEmail("peter@example.com");
        worker8.setPhone("0889012345");
        worker8.setSkill("IT Support");
        worker8.setCategory("Technician");
        worker8.setLocation("Cape Town");
        worker8.setRole("worker");
        worker8.setExperience(6);
        worker8.setCompletedJobs(145);
        worker8.setRating(4.9);
        worker8.setHourlyRate(450);
        worker8.setAbout("IT specialist. Computer repairs, networking, and software installation.");
        worker8.setAvailable(true);
        worker8.setAvailability("Monday - Friday, 9am - 6pm");

        saveWorkerToFirebase("worker_john", worker1);
        saveWorkerToFirebase("worker_sarah", worker2);
        saveWorkerToFirebase("worker_mike", worker3);
        saveWorkerToFirebase("worker_linda", worker4);
        saveWorkerToFirebase("worker_thabo", worker5);
        saveWorkerToFirebase("worker_david", worker6);
        saveWorkerToFirebase("worker_grace", worker7);
        saveWorkerToFirebase("worker_peter", worker8);
    }

    private void saveWorkerToFirebase(String key, Worker worker) {
        usersRef.child(key).setValue(worker)
                .addOnSuccessListener(aVoid -> {
                    workersAdded++;
                    if (workersAdded == TOTAL_WORKERS) {
                        progressBar.setVisibility(View.GONE);
                        btnAddWorkers.setEnabled(true);
                        btnClearWorkers.setEnabled(true);
                        Toast.makeText(AdminActivity.this,
                                workersAdded + " sample workers added successfully!", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnAddWorkers.setEnabled(true);
                    btnClearWorkers.setEnabled(true);
                    Toast.makeText(AdminActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmClearAllWorkers() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Workers")
                .setMessage("Are you sure you want to delete ALL workers from the database? This action cannot be undone.")
                .setPositiveButton("Yes, Delete All", (dialog, which) -> clearAllWorkers())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearAllWorkers() {
        progressBar.setVisibility(View.VISIBLE);
        btnAddWorkers.setEnabled(false);
        btnClearWorkers.setEnabled(false);

        usersRef.orderByChild("role").equalTo("worker")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int deletedCount = 0;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            child.getRef().removeValue();
                            deletedCount++;
                        }
                        progressBar.setVisibility(View.GONE);
                        btnAddWorkers.setEnabled(true);
                        btnClearWorkers.setEnabled(true);
                        Toast.makeText(AdminActivity.this,
                                deletedCount + " workers deleted successfully", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        btnAddWorkers.setEnabled(true);
                        btnClearWorkers.setEnabled(true);
                        Toast.makeText(AdminActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}