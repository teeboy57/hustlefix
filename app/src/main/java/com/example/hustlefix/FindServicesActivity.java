package com.example.hustlefix;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FindServicesActivity extends AppCompatActivity {

    private static final String TAG = "FindServices";
    
    private Toolbar toolbar;
    private RecyclerView rvServices;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private DatabaseReference servicesRef;
    private List<Service> serviceList;
    private ServiceDiscoveryAdapter serviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_find_services);
            initViews();
            setupToolbar();
            loadServices();
        } catch (Exception e) {
            ErrorUtils.showError(this, e);
            finish();
        }
    }

    private void initViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            rvServices = findViewById(R.id.rvServices);
            progressBar = findViewById(R.id.progressBar);
            tvEmpty = findViewById(R.id.tvEmpty);

            rvServices.setLayoutManager(new LinearLayoutManager(this));
            serviceList = new ArrayList<>();
            serviceAdapter = new ServiceDiscoveryAdapter(serviceList, new ServiceDiscoveryAdapter.OnServiceClickListener() {
                @Override
                public void onServiceClick(Service service) {
                    try {
                        Log.d(TAG, "Service clicked: " + service.getTitle());
                        Intent intent = new Intent(FindServicesActivity.this, ServiceDetailActivity.class);
                        intent.putExtra("serviceId", service.getServiceId());
                        startActivity(intent);
                    } catch (Exception e) {
                        ErrorUtils.showError(FindServicesActivity.this, "Error opening service: " + e.getMessage());
                    }
                }
            });
            rvServices.setAdapter(serviceAdapter);
        } catch (Exception e) {
            ErrorUtils.showError(this, e);
        }
    }

    private void setupToolbar() {
        try {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Find Services");
            }
        } catch (Exception e) {
            ErrorUtils.showError(this, e);
        }
    }

    private void loadServices() {
        try {
            setLoading(true);
            servicesRef = FirebaseDatabase.getInstance().getReference("services");
            servicesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        setLoading(false);
                        serviceList.clear();

                        if (snapshot.exists()) {
                            for (DataSnapshot serviceSnapshot : snapshot.getChildren()) {
                                Service service = serviceSnapshot.getValue(Service.class);
                                if (service != null && "active".equals(service.getStatus())) {
                                    serviceList.add(service);
                                }
                            }
                            serviceAdapter.notifyDataSetChanged();
                            
                            if (serviceList.isEmpty()) {
                                tvEmpty.setVisibility(View.VISIBLE);
                                rvServices.setVisibility(View.GONE);
                            } else {
                                tvEmpty.setVisibility(View.GONE);
                                rvServices.setVisibility(View.VISIBLE);
                            }
                        } else {
                            tvEmpty.setVisibility(View.VISIBLE);
                            rvServices.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        ErrorUtils.showError(FindServicesActivity.this, "Error loading services: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    setLoading(false);
                    ErrorUtils.showError(FindServicesActivity.this, "Error loading services: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            ErrorUtils.showError(this, e);
        }
    }

    private void setLoading(boolean isLoading) {
        try {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            // Ignore
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}