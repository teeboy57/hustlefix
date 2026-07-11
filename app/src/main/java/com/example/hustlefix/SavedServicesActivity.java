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

public class SavedServicesActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvSavedServices;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private DatabaseReference savedRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private List<Service> savedServiceList;
    private ServiceDiscoveryAdapter serviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_services);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        initViews();
        setupToolbar();
        loadSavedServices();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvSavedServices = findViewById(R.id.rvSavedServices);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvSavedServices.setLayoutManager(new LinearLayoutManager(this));
        savedServiceList = new ArrayList<>();
        serviceAdapter = new ServiceDiscoveryAdapter(savedServiceList, new ServiceDiscoveryAdapter.OnServiceClickListener() {
            @Override
            public void onServiceClick(Service service) {
                Intent intent = new Intent(SavedServicesActivity.this, ServiceDetailActivity.class);
                intent.putExtra("serviceId", service.getServiceId());
                startActivity(intent);
            }
        });
        rvSavedServices.setAdapter(serviceAdapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Saved Services");
        }
    }

    private void loadSavedServices() {
        if (currentUserId == null) {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        savedRef = FirebaseDatabase.getInstance().getReference("saved_services").child(currentUserId);
        savedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                savedServiceList.clear();

                if (snapshot.exists()) {
                    List<String> serviceIds = new ArrayList<>();
                    for (DataSnapshot savedSnapshot : snapshot.getChildren()) {
                        String serviceId = savedSnapshot.getValue(String.class);
                        if (serviceId != null) {
                            serviceIds.add(serviceId);
                        }
                    }

                    if (!serviceIds.isEmpty()) {
                        loadServiceDetails(serviceIds);
                    } else {
                        setLoading(false);
                        showEmptyState();
                    }
                } else {
                    setLoading(false);
                    showEmptyState();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setLoading(false);
                Toast.makeText(SavedServicesActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadServiceDetails(List<String> serviceIds) {
        final List<Service> tempList = new ArrayList<>();
        final int[] count = {0};

        for (String serviceId : serviceIds) {
            FirebaseDatabase.getInstance().getReference("services").child(serviceId).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            Service service = snapshot.getValue(Service.class);
                            if (service != null) {
                                tempList.add(service);
                            }
                        }
                        count[0]++;
                        if (count[0] == serviceIds.size()) {
                            setLoading(false);
                            if (!tempList.isEmpty()) {
                                savedServiceList.clear();
                                savedServiceList.addAll(tempList);
                                serviceAdapter.notifyDataSetChanged();
                                rvSavedServices.setVisibility(View.VISIBLE);
                                tvEmpty.setVisibility(View.GONE);
                            } else {
                                showEmptyState();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        count[0]++;
                        if (count[0] == serviceIds.size()) {
                            setLoading(false);
                            if (!tempList.isEmpty()) {
                                savedServiceList.clear();
                                savedServiceList.addAll(tempList);
                                serviceAdapter.notifyDataSetChanged();
                                rvSavedServices.setVisibility(View.VISIBLE);
                                tvEmpty.setVisibility(View.GONE);
                            } else {
                                showEmptyState();
                            }
                        }
                    });
        }
    }

    private void showEmptyState() {
        tvEmpty.setVisibility(View.VISIBLE);
        rvSavedServices.setVisibility(View.GONE);
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}