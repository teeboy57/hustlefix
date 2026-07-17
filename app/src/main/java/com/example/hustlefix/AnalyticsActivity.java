package com.example.hustlefix;

import android.os.Bundle;
import android.view.View;
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

import java.util.Calendar;
import java.util.Locale;

public class AnalyticsActivity extends AppCompatActivity {

    private static final String TAG = "AnalyticsActivity";
    
    private Toolbar toolbar;
    private ProgressBar progressBar;
    
    private TextView tvTotalServices;
    private TextView tvTotalBookings;
    private TextView tvTotalRevenue;
    private TextView tvAvgRating;
    private TextView tvCompletedBookings;
    private TextView tvPendingBookings;
    private TextView tvCancelledBookings;
    private TextView tvMonthlyRevenue;
    private TextView tvWeeklyBookings;
    private TextView tvTodayBookings;
    private TextView tvTotalClients;
    private TextView tvAvgPrice;
    private TextView tvCompletionRate;

    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadAnalyticsData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        
        tvTotalServices = findViewById(R.id.tvTotalServices);
        tvTotalBookings = findViewById(R.id.tvTotalBookings);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvAvgRating = findViewById(R.id.tvAvgRating);
        tvCompletedBookings = findViewById(R.id.tvCompletedBookings);
        tvPendingBookings = findViewById(R.id.tvPendingBookings);
        tvCancelledBookings = findViewById(R.id.tvCancelledBookings);
        tvMonthlyRevenue = findViewById(R.id.tvMonthlyRevenue);
        tvWeeklyBookings = findViewById(R.id.tvWeeklyBookings);
        tvTodayBookings = findViewById(R.id.tvTodayBookings);
        tvTotalClients = findViewById(R.id.tvTotalClients);
        tvAvgPrice = findViewById(R.id.tvAvgPrice);
        tvCompletionRate = findViewById(R.id.tvCompletionRate);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Analytics");
        }
    }

    private void loadAnalyticsData() {
        if (currentUserId == null) return;

        setLoading(true);

        DatabaseReference servicesRef = FirebaseDatabase.getInstance().getReference("services");
        servicesRef.orderByChild("serviceProviderId").equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        tvTotalServices.setText(String.valueOf(snapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        tvTotalServices.setText("0");
                    }
                });

        DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        bookingsRef.orderByChild("serviceProviderId").equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        setLoading(false);
                        
                        if (!snapshot.exists()) {
                            setDefaultValues();
                            return;
                        }

                        int totalBookings = 0;
                        int completed = 0;
                        int pending = 0;
                        int cancelled = 0;
                        double totalRevenue = 0;
                        double totalRating = 0;
                        int ratingCount = 0;
                        double totalPrice = 0;
                        
                        double monthlyRevenue = 0;
                        int weeklyBookings = 0;
                        int todayBookings = 0;
                        
                        java.util.HashSet<String> uniqueClients = new java.util.HashSet<>();
                        
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        long todayStart = calendar.getTimeInMillis();
                        
                        Calendar weekAgo = Calendar.getInstance();
                        weekAgo.add(Calendar.DAY_OF_YEAR, -7);
                        weekAgo.set(Calendar.HOUR_OF_DAY, 0);
                        weekAgo.set(Calendar.MINUTE, 0);
                        weekAgo.set(Calendar.SECOND, 0);
                        weekAgo.set(Calendar.MILLISECOND, 0);
                        long weekStart = weekAgo.getTimeInMillis();
                        
                        Calendar monthAgo = Calendar.getInstance();
                        monthAgo.add(Calendar.MONTH, -1);
                        monthAgo.set(Calendar.HOUR_OF_DAY, 0);
                        monthAgo.set(Calendar.MINUTE, 0);
                        monthAgo.set(Calendar.SECOND, 0);
                        monthAgo.set(Calendar.MILLISECOND, 0);
                        long monthStart = monthAgo.getTimeInMillis();

                        for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                            Booking booking = bookingSnapshot.getValue(Booking.class);
                            if (booking == null) continue;
                            
                            totalBookings++;
                            long bookingDate = booking.getBookingDate();
                            double price = booking.getPrice();
                            totalPrice += price;
                            
                            if (booking.getClientId() != null) {
                                uniqueClients.add(booking.getClientId());
                            }
                            
                            String status = booking.getStatus() != null ? booking.getStatus() : "";
                            switch (status.toLowerCase()) {
                                case "completed":
                                    completed++;
                                    totalRevenue += price;
                                    if (booking.getRating() > 0) {
                                        totalRating += booking.getRating();
                                        ratingCount++;
                                    }
                                    break;
                                case "pending":
                                    pending++;
                                    break;
                                case "cancelled":
                                    cancelled++;
                                    break;
                                default:
                                    break;
                            }
                            
                            if (booking.getTimestamp() >= monthStart) {
                                monthlyRevenue += price;
                            }
                            if (booking.getTimestamp() >= weekStart) {
                                weeklyBookings++;
                            }
                            if (booking.getTimestamp() >= todayStart) {
                                todayBookings++;
                            }
                        }

                        tvTotalBookings.setText(String.valueOf(totalBookings));
                        tvCompletedBookings.setText(String.valueOf(completed));
                        tvPendingBookings.setText(String.valueOf(pending));
                        tvCancelledBookings.setText(String.valueOf(cancelled));
                        
                        tvTotalRevenue.setText("$" + String.format("%.2f", totalRevenue));
                        tvMonthlyRevenue.setText("$" + String.format("%.2f", monthlyRevenue));
                        tvWeeklyBookings.setText(String.valueOf(weeklyBookings));
                        tvTodayBookings.setText(String.valueOf(todayBookings));
                        
                        if (ratingCount > 0) {
                            double avgRating = totalRating / ratingCount;
                            tvAvgRating.setText(String.format(Locale.getDefault(), "%.1f \u2605", avgRating));
                        } else {
                            tvAvgRating.setText("0.0 \u2605");
                        }
                        
                        tvTotalClients.setText(String.valueOf(uniqueClients.size()));
                        
                        if (totalBookings > 0) {
                            double avgPrice = totalPrice / totalBookings;
                            tvAvgPrice.setText("$" + String.format("%.2f", avgPrice));
                            int completionRateValue = (completed * 100) / totalBookings;
                            tvCompletionRate.setText(completionRateValue + "%");
                        } else {
                            tvAvgPrice.setText("$0.00");
                            tvCompletionRate.setText("0%");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        setLoading(false);
                        setDefaultValues();
                    }
                });
    }

    private void setDefaultValues() {
        tvTotalServices.setText("0");
        tvTotalBookings.setText("0");
        tvTotalRevenue.setText("$0.00");
        tvAvgRating.setText("0.0 \u2605");
        tvCompletedBookings.setText("0");
        tvPendingBookings.setText("0");
        tvCancelledBookings.setText("0");
        tvMonthlyRevenue.setText("$0.00");
        tvWeeklyBookings.setText("0");
        tvTodayBookings.setText("0");
        tvTotalClients.setText("0");
        tvAvgPrice.setText("$0.00");
        tvCompletionRate.setText("0%");
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