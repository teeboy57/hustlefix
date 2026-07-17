package com.example.hustlefix;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminActivity extends AppCompatActivity {

    private Button btnAddWorkers;
    private Button btnClearWorkers;
    private Toolbar toolbar;

    private DatabaseReference databaseReference;

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
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Admin");
        }
    }

    private void setupFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void setupClickListeners() {
        btnAddWorkers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminActivity.this, "Add Workers - Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });

        btnClearWorkers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminActivity.this, "Clear Workers - Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}