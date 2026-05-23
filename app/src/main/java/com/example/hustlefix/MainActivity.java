package com.example.hustlefix;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnEntrepreneur, btnClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        btnEntrepreneur = findViewById(R.id.btnEntrepreneur);
        btnClient = findViewById(R.id.btnClient);

        btnEntrepreneur.setOnClickListener(v -> {
            // Navigate directly to Login with role
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra("ROLE", "ENTREPRENEUR");
            startActivity(intent);
        });

        btnClient.setOnClickListener(v -> {
            // Navigate directly to Login with role
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra("ROLE", "CLIENT");
            startActivity(intent);
        });
    }
}