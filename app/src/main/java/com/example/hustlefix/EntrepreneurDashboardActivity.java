package com.example.hustlefix;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EntrepreneurDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "EntrepDash";
    
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private TextView tvTotalServices;
    private TextView tvTotalBookings;
    private TextView tvTotalRevenue;
    private TextView tvRating;
    private TextView tvBusinessName;
    private TextView tvDate;
    private RecyclerView rvRecentBookings;
    
    private CardView cardServices;
    private CardView cardBookings;
    private CardView cardRevenue;
    private CardView cardAnalytics;
    private CardView cardPostService;
    private CardView cardMyServices;
    private CardView cardMyBookings;
    private CardView cardAnalyticsQuick;

    private FirebaseAuth mAuth;
    private String currentUserId;

    private List<Booking> recentBookings;
    private BookingAdapter bookingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrepreneur_dashboard);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        initViews();
        setupToolbar();
        setupNavigationDrawer();
        setupClickListeners();
        loadDashboardData();
        loadRecentBookings();
        updateNavHeader();
        setCurrentDate();
        setBusinessName();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        tvTotalServices = findViewById(R.id.tvTotalServices);
        tvTotalBookings = findViewById(R.id.tvTotalBookings);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvRating = findViewById(R.id.tvRating);
        tvBusinessName = findViewById(R.id.tvBusinessName);
        tvDate = findViewById(R.id.tvDate);

        rvRecentBookings = findViewById(R.id.rvRecentBookings);
        rvRecentBookings.setLayoutManager(new LinearLayoutManager(this));

        cardServices = findViewById(R.id.cardServices);
        cardBookings = findViewById(R.id.cardBookings);
        cardRevenue = findViewById(R.id.cardRevenue);
        cardAnalytics = findViewById(R.id.cardAnalytics);
        cardPostService = findViewById(R.id.cardPostService);
        cardMyServices = findViewById(R.id.cardMyServices);
        cardMyBookings = findViewById(R.id.cardMyBookings);
        cardAnalyticsQuick = findViewById(R.id.cardAnalyticsQuick);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Business");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
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
                if (tvNavUserName != null) tvNavUserName.setText(name);
                if (tvNavUserEmail != null) tvNavUserEmail.setText(currentUser.getEmail());

                if (ivNavAvatar != null) {
                    ivNavAvatar.setImageResource(R.drawable.ic_avatar_entrepreneur);
                }
            }
        }
    }

    private void setCurrentDate() {
        if (tvDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault());
            tvDate.setText(sdf.format(new Date()));
        }
    }

    private void setBusinessName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && tvBusinessName != null) {
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                String email = user.getEmail();
                if (email != null && email.contains("@")) {
                    name = email.split("@")[0];
                } else {
                    name = "Your Business";
                }
            }
            tvBusinessName.setText(name + "'s Business");
        }
    }

    private void setupClickListeners() {
        cardServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Services clicked");
                startActivity(new Intent(EntrepreneurDashboardActivity.this, MyServicesActivity.class));
            }
        });

        cardBookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Bookings clicked");
                startActivity(new Intent(EntrepreneurDashboardActivity.this, MyBookingsActivity.class));
            }
        });

        cardRevenue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Revenue clicked");
                startActivity(new Intent(EntrepreneurDashboardActivity.this, RevenueActivity.class));
            }
        });

        cardAnalytics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Analytics clicked");
                Toast.makeText(EntrepreneurDashboardActivity.this, "Opening Analytics...", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(EntrepreneurDashboardActivity.this, AnalyticsActivity.class));
            }
        });

        cardPostService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Post Service quick action");
                startActivity(new Intent(EntrepreneurDashboardActivity.this, PostServiceActivity.class));
            }
        });

        cardMyServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "My Services quick action");
                startActivity(new Intent(EntrepreneurDashboardActivity.this, MyServicesActivity.class));
            }
        });

        cardMyBookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "My Bookings quick action");
                startActivity(new Intent(EntrepreneurDashboardActivity.this, MyBookingsActivity.class));
            }
        });

        cardAnalyticsQuick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Analytics quick action clicked!");
                Toast.makeText(EntrepreneurDashboardActivity.this, "Opening Analytics...", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(EntrepreneurDashboardActivity.this, AnalyticsActivity.class));
            }
        });
    }

    private void loadDashboardData() {
        if (currentUserId == null) return;

        DatabaseReference servicesRef = FirebaseDatabase.getInstance().getReference("services");
        servicesRef.orderByChild("entrepreneurId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long count = snapshot.getChildrenCount();
                        if (tvTotalServices != null) {
                            tvTotalServices.setText(String.valueOf(count));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (tvTotalServices != null) {
                            tvTotalServices.setText("0");
                        }
                    }
                });

        DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        bookingsRef.orderByChild("entrepreneurId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long count = snapshot.getChildrenCount();
                        if (tvTotalBookings != null) {
                            tvTotalBookings.setText(String.valueOf(count));
                        }

                        double revenue = 0;
                        double totalRating = 0;
                        int ratingCount = 0;
                        
                        for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                            Booking booking = bookingSnapshot.getValue(Booking.class);
                            if (booking != null) {
                                if ("completed".equals(booking.getStatus())) {
                                    revenue += booking.getPrice();
                                }
                                if (booking.getRating() > 0) {
                                    totalRating += booking.getRating();
                                    ratingCount++;
                                }
                            }
                        }
                        
                        if (tvTotalRevenue != null) {
                            tvTotalRevenue.setText("$" + String.format("%.2f", revenue));
                        }
                        if (tvRating != null) {
                            if (ratingCount > 0) {
                                tvRating.setText(String.format("%.1f ★", totalRating / ratingCount));
                            } else {
                                tvRating.setText("0.0 ★");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (tvTotalBookings != null) tvTotalBookings.setText("0");
                        if (tvTotalRevenue != null) tvTotalRevenue.setText("$0");
                        if (tvRating != null) tvRating.setText("0.0 ★");
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

                        TextView tvEmpty = findViewById(R.id.tvEmptyBookings);
                        if (recentBookings.isEmpty()) {
                            if (tvEmpty != null) {
                                tvEmpty.setVisibility(View.VISIBLE);
                                rvRecentBookings.setVisibility(View.GONE);
                            }
                            return;
                        }

                        List<Booking> reversed = new ArrayList<>();
                        for (int i = recentBookings.size() - 1; i >= 0; i--) {
                            reversed.add(recentBookings.get(i));
                        }

                        bookingAdapter = new BookingAdapter(reversed, new BookingAdapter.OnBookingClickListener() {
                            @Override
                            public void onBookingClick(Booking booking) {
                                Intent intent = new Intent(EntrepreneurDashboardActivity.this, BookingDetailActivity.class);
                                intent.putExtra("bookingId", booking.getBookingId());
                                startActivity(intent);
                            }
                        });
                        rvRecentBookings.setAdapter(bookingAdapter);
                        
                        if (tvEmpty != null) {
                            tvEmpty.setVisibility(View.GONE);
                            rvRecentBookings.setVisibility(View.VISIBLE);
                        }
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
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, PostServiceActivity.class));
            return true;
        } else if (id == R.id.nav_my_services) {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, MyServicesActivity.class));
            return true;
        } else if (id == R.id.nav_my_bookings) {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, MyBookingsActivity.class));
            return true;
        } else if (id == R.id.nav_analytics) {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(this, "Opening Analytics...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AnalyticsActivity.class));
            return true;
        } else if (id == R.id.nav_settings) {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.nav_logout) {
            drawerLayout.closeDrawer(GravityCompat.START);
            logout();
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    SessionHelper.logout(this);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}