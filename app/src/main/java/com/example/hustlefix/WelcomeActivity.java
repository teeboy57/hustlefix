package com.example.hustlefix;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private Button btnEntrepreneur, btnClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        btnEntrepreneur = findViewById(R.id.btnEntrepreneur);
        btnClient = findViewById(R.id.btnClient);

        btnEntrepreneur.setOnClickListener(v -> goToLogin("ENTREPRENEUR"));
        btnClient.setOnClickListener(v -> goToLogin("CLIENT"));
    }

    private void goToLogin(String role) {
        SessionHelper.saveRole(this, role);
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        intent.putExtra("ROLE", role);
        startActivity(intent);
    }
}
