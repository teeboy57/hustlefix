package com.example.hustlefix;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class BookingDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Booking Details");
        }
        
        String bookingId = getIntent().getStringExtra("bookingId");
        Toast.makeText(this, "Booking Detail: " + bookingId, Toast.LENGTH_SHORT).show();
        
        // Show booking ID in a TextView
        TextView tvBookingId = findViewById(R.id.tvBookingId);
        if (tvBookingId != null && bookingId != null) {
            tvBookingId.setText("Booking ID: " + bookingId);
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}