package com.example.hustlefix;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EntrepreneurDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FloatingActionButton fabPostService;

    private TextView tvTotalServices, tvTotalBookings, tvTotalRevenue, tvRating;
    private RecyclerView rvRecentBookings;
    private CardView cardServices, cardBookings, cardRevenue, cardAnalytics;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String currentUserId;

    private List<Booking> recentBookings;
    private BookingAdapter bookingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrepreneur_dashboard);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        initViews();
        setupToolbar();
        setupNavigationDrawer();
        setupClickListeners();
        loadDashboardData();
        loadRecentBookings();
        updateNavHeader();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        fabPostService = findViewById(R.id.fabPostService);

        tvTotalServices = findViewById(R.id.tvTotalServices);
        tvTotalBookings = findViewById(R.id.tvTotalBookings);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvRating = findViewById(R.id.tvRating);

        rvRecentBookings = findViewById(R.id.rvRecentBookings);
        rvRecentBookings.setLayoutManager(new LinearLayoutManager(this));

        cardServices = findViewById(R.id.cardServices);
        cardBookings = findViewById(R.id.cardBookings);
        cardRevenue = findViewById(R.id.cardRevenue);
        cardAnalytics = findViewById(R.id.cardAnalytics);

        fabPostService.setImageResource(R.drawable.ic_post_job);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Business");
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

                if (ivNavAvatar != null) {
                    ivNavAvatar.setImageResource(R.drawable.ic_avatar_entrepreneur);
                }
            }
        }
    }

    private void setupClickListeners() {
        fabPostService.setOnClickListener(v -> {
            Intent intent = new Intent(EntrepreneurDashboardActivity.this, PostServiceActivity.class);
            startActivity(intent);
        });

        cardServices.setOnClickListener(v -> {
            Toast.makeText(this, "My Services", Toast.LENGTH_SHORT).show();
        });

        cardBookings.setOnClickListener(v -> {
            Toast.makeText(this, "My Bookings", Toast.LENGTH_SHORT).show();
        });

        cardRevenue.setOnClickListener(v -> {
            Toast.makeText(this, "Revenue Dashboard", Toast.LENGTH_SHORT).show();
        });

        cardAnalytics.setOnClickListener(v -> {
            Toast.makeText(this, "Analytics", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadDashboardData() {
        if (currentUserId == null) return;

        DatabaseReference servicesRef = FirebaseDatabase.getInstance().getReference("services");
        servicesRef.orderByChild("entrepreneurId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long serviceCount = snapshot.getChildrenCount();
                        tvTotalServices.setText(String.valueOf(serviceCount));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvTotalServices.setText("0");
                    }
                });

        DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        bookingsRef.orderByChild("entrepreneurId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long bookingCount = snapshot.getChildrenCount();
                        tvTotalBookings.setText(String.valueOf(bookingCount));

                        double revenue = 0;
                        for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                            Booking booking = bookingSnapshot.getValue(Booking.class);
                            if (booking != null && "completed".equals(booking.getStatus())) {
                                revenue += booking.getPrice();
                            }
                        }
                        tvTotalRevenue.setText("$" + String.format("%.2f", revenue));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvTotalBookings.setText("0");
                        tvTotalRevenue.setText("$0");
                    }
                });
    }

    private void loadRecentBookings() {
        if (currentUserId == null) return;

        recentBookings = new ArrayList<>();
        DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        bookingsRef.orderByChild("entrepreneurId").equalTo(currentUserId)
                .limitToLast(5)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        recentBookings.clear();
                        for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                            Booking booking = bookingSnapshot.getValue(Booking.class);
                            if (booking != null) {
                                recentBookings.add(booking);
                            }
                        }
                        List<Booking> reversed = new ArrayList<>();
                        for (int i = recentBookings.size() - 1; i >= 0; i--) {
                            reversed.add(recentBookings.get(i));
                        }
                        bookingAdapter = new BookingAdapter(reversed, booking -> {
                            Toast.makeText(EntrepreneurDashboardActivity.this,
                                    "Booking: " + booking.getJobTitle(), Toast.LENGTH_SHORT).show();
                        });
                        rvRecentBookings.setAdapter(bookingAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_post_job) {
            Intent intent = new Intent(this, PostServiceActivity.class);
            startActivity(intent);
            return true;
        }  else if (id == R.id.nav_logout) {
            logout();
            return true;
        } else {
            return NavigationHelper.handleNavigationItem(this, id);
        }
    }

    private void logout() {
        SessionHelper.logout(this);
        finish();
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