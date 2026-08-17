package com.example.hustlefix;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class PaymentMethodsActivity extends AppCompatActivity {

    private RadioButton rbCard, rbMobileMoney, rbCash;
    private MaterialButton btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_methods);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        loadSavedPreference();
        setupClickListeners();
    }

    private void initViews() {
        rbCard = findViewById(R.id.rbCard);
        rbMobileMoney = findViewById(R.id.rbMobileMoney);
        rbCash = findViewById(R.id.rbCash);
        btnSave = findViewById(R.id.btnSavePayment);

        findViewById(R.id.cardPaymentMethod).setOnClickListener(v -> selectOption("CARD"));
        findViewById(R.id.cardMobileMoney).setOnClickListener(v -> selectOption("MOBILE_MONEY"));
        findViewById(R.id.cardCash).setOnClickListener(v -> selectOption("CASH"));
    }

    private void selectOption(String option) {
        rbCard.setChecked("CARD".equals(option));
        rbMobileMoney.setChecked("MOBILE_MONEY".equals(option));
        rbCash.setChecked("CASH".equals(option));
    }

    private void loadSavedPreference() {
        String pref = SessionHelper.prefs(this).getString("payment_preference", "CASH");
        selectOption(pref);
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> {
            String selected = "CASH";
            if (rbCard.isChecked()) selected = "CARD";
            else if (rbMobileMoney.isChecked()) selected = "MOBILE_MONEY";

            SessionHelper.prefs(this).edit().putString("payment_preference", selected).apply();
            Toast.makeText(this, "Payment preference saved!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
