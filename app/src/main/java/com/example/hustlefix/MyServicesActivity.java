package com.example.hustlefix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyServicesActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvServices;
    private ProgressBar progressBar;
    private View tvEmpty;

    private DatabaseReference servicesRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private List<Service> serviceList;
    private ServiceAdapter serviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_services);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        initViews();
        setupToolbar();
        loadServices();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvServices = findViewById(R.id.rvServices);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        androidx.recyclerview.widget.GridLayoutManager gridLayoutManager = new androidx.recyclerview.widget.GridLayoutManager(this, 2);
        rvServices.setLayoutManager(gridLayoutManager);
        
        // Add padding for grid
        int spacing = (int) (8 * getResources().getDisplayMetrics().density);
        rvServices.setPadding(spacing, spacing, spacing, spacing);
        rvServices.setClipToPadding(false);

        serviceList = new ArrayList<>();
        serviceAdapter = new ServiceAdapter(serviceList, new ServiceAdapter.OnServiceClickListener() {
            @Override
            public void onServiceClick(Service service) {
                // Navigate to service detail
                Toast.makeText(MyServicesActivity.this, "Service: " + service.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEditClick(Service service) {
                // Open EditServiceActivity
                Intent intent = new Intent(MyServicesActivity.this, EditServiceActivity.class);
                intent.putExtra("serviceId", service.getServiceId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Service service) {
                // Delete service
                deleteService(service);
            }
        });
        rvServices.setAdapter(serviceAdapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Services");
        }
    }

    private void loadServices() {
        if (currentUserId == null) {
            Toast.makeText(this, "Please login to view your services", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        servicesRef = FirebaseDatabase.getInstance().getReference("services");
        servicesRef.orderByChild("serviceProviderId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        setLoading(false);
                        serviceList.clear();

                        if (snapshot.exists()) {
                            for (DataSnapshot serviceSnapshot : snapshot.getChildren()) {
                                Service service = serviceSnapshot.getValue(Service.class);
                                if (service != null) {
                                    serviceList.add(service);
                                }
                            }
                            serviceAdapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(View.GONE);
                            rvServices.setVisibility(View.VISIBLE);
                        } else {
                            tvEmpty.setVisibility(View.VISIBLE);
                            rvServices.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        setLoading(false);
                        Toast.makeText(MyServicesActivity.this, "Error loading services: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteService(Service service) {
        if (service == null || service.getServiceId() == null) {
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Service")
                .setMessage("Are you sure you want to delete '" + service.getTitle() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    servicesRef.child(service.getServiceId()).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(MyServicesActivity.this, "Service deleted successfully", Toast.LENGTH_SHORT).show();
                                loadServices();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(MyServicesActivity.this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    public void openPostService(View view) {
        startActivity(new Intent(this, PostServiceActivity.class));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}