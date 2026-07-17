package com.example.hustlefix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button btnServiceProvider;
    private Button btnClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Check if user is already logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // User is logged in, check role
            String role = SessionHelper.getRole(this);
            if (role != null && !role.isEmpty()) {
                if ("service_provider".equals(role)) {
                    startActivity(new Intent(this, ServiceProviderDashboardActivity.class));
                    finish();
                    return;
                } else if ("client".equals(role)) {
                    startActivity(new Intent(this, ClientDashboardActivity.class));
                    finish();
                    return;
                }
            }
        }

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnServiceProvider = findViewById(R.id.btnServiceProvider);
        btnClient = findViewById(R.id.btnClient);
    }

    private void setupClickListeners() {
        btnServiceProvider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                intent.putExtra("ROLE", "service_provider");
                startActivity(intent);
            }
        });

        btnClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                intent.putExtra("ROLE", "client");
                startActivity(intent);
            }
        });
    }
}