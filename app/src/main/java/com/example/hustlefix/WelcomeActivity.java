package com.example.hustlefix;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
public class WelcomeActivity extends AppCompatActivity {
    private Button btnServiceProvider, btnClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        btnServiceProvider = findViewById(R.id.btnServiceProvider);
        btnClient = findViewById(R.id.btnClient);

        // Global Banner is just decorative/text, no need to link unless dynamic

        btnServiceProvider.setOnClickListener(v -> goToLogin("service_provider"));
        btnClient.setOnClickListener(v -> goToLogin("CLIENT"));
    }
    private void goToLogin(String role) {
        SessionHelper.saveRole(this, role);
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        intent.putExtra("ROLE", role);
        startActivity(intent);
    }
}
