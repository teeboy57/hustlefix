package com.example.hustlefix;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView btnGoLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private String userRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getIntent().hasExtra("ROLE")) {
            userRole = getIntent().getStringExtra("ROLE");
        } else {
            userRole = SessionHelper.getRole(this);
        }

        initViews();
        setupFirebase();
        setupClickListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoLogin = findViewById(R.id.btnGoLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> registerUser());
        
        btnGoLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.putExtra("ROLE", userRole);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Name is required");
            etFullName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        setLoading(false);
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Registration failed";
                        Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        setLoading(false);
                        Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build();

                    user.updateProfile(profileUpdate).addOnCompleteListener(profileTask -> {
                        saveUserProfile(user, fullName, email, phone);
                    });
                });
    }

    private void saveUserProfile(FirebaseUser user, String fullName, String email, String phone) {
        String uid = user.getUid();
        String firebaseRole = SessionHelper.firebaseRoleForAppRole(userRole);
        
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", fullName);
        userMap.put("email", email);
        userMap.put("role", firebaseRole);
        userMap.put("available", true);
        userMap.put("rating", 0);
        userMap.put("completedJobs", 0);
        
        if (!TextUtils.isEmpty(phone)) {
            userMap.put("phone", phone);
        }
        
        if ("worker".equals(firebaseRole)) {
            userMap.put("skill", "General");
            userMap.put("category", "General");
            userMap.put("location", "");
        }

        FirebaseDatabase.getInstance().getReference("users").child(uid).setValue(userMap)
                .addOnCompleteListener(dbTask -> {
                    setLoading(false);
                    SessionHelper.saveRole(RegisterActivity.this, userRole);
                    mAuth.signOut();
                    Toast.makeText(RegisterActivity.this,
                            "Account created! Please sign in.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("ROLE", userRole);
                    startActivity(intent);
                    finish();
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!isLoading);
        btnRegister.setText(isLoading ? "REGISTERING..." : "REGISTER");
        etFullName.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etPhone.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        etConfirmPassword.setEnabled(!isLoading);
        btnGoLogin.setEnabled(!isLoading);
    }
}