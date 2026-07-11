package com.example.hustlefix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private MaterialCheckBox cbRememberMe;
    private TextView tvForgotPassword, btnGoRegister;
    private MaterialCardView cardGoogleLogin, cardFacebookLogin, cardAppleLogin;
    private ProgressBar progressBar;
    private SharedPreferences sharedPreferences;
    private String userRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("ROLE")) {
            userRole = intent.getStringExtra("ROLE");
            Log.d(TAG, "Role received from WelcomeActivity: " + userRole);
        } else {
            userRole = SessionHelper.getRole(this);
            Log.d(TAG, "Role loaded from session: " + userRole);
        }

        initViews();
        loadSavedCredentials();
        setupClickListeners();
        checkAlreadyLoggedIn();
        
        // Hide Facebook login button
        cardFacebookLogin.setVisibility(View.GONE);
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnGoRegister = findViewById(R.id.btnGoRegister);
        cardGoogleLogin = findViewById(R.id.cardGoogleLogin);
        cardFacebookLogin = findViewById(R.id.cardFacebookLogin);
        cardAppleLogin = findViewById(R.id.cardAppleLogin);
        progressBar = findViewById(R.id.progressBar);
        sharedPreferences = SessionHelper.prefs(this);
    }

    private void checkAlreadyLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String savedRole = sharedPreferences.getString("userRole", "");

        if (currentUser != null && isLoggedIn && !savedRole.isEmpty()) {
            Log.d(TAG, "User already logged in, navigating to dashboard");
            navigateToDashboard(savedRole);
        }
    }

    private void loadSavedCredentials() {
        boolean rememberMe = sharedPreferences.getBoolean("rememberMe", false);
        if (rememberMe) {
            etEmail.setText(sharedPreferences.getString("email", ""));
            etPassword.setText(sharedPreferences.getString("password", ""));
            cbRememberMe.setChecked(true);
        }
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> loginUser());

        btnGoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            intent.putExtra("ROLE", userRole);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        cardGoogleLogin.setOnClickListener(v -> {
            Toast.makeText(this, "Google Sign-In coming soon", Toast.LENGTH_SHORT).show();
        });

        cardFacebookLogin.setOnClickListener(v -> {
            Toast.makeText(this, "Facebook Sign-In coming soon", Toast.LENGTH_SHORT).show();
        });

        cardAppleLogin.setOnClickListener(v -> {
            Toast.makeText(this, "Apple Sign-In coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
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

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String userName = user.getDisplayName();
                        if (userName == null || userName.isEmpty()) {
                            userName = email.split("@")[0];
                        }

                        Toast.makeText(LoginActivity.this, "Welcome " + userName + "!", Toast.LENGTH_SHORT).show();

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        SessionHelper.setLoggedIn(LoginActivity.this, true);
                        SessionHelper.saveRole(LoginActivity.this, userRole);

                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("userRole", userRole);
                        editor.putString("userEmail", email);
                        editor.putBoolean("rememberMe", cbRememberMe.isChecked());

                        if (cbRememberMe.isChecked()) {
                            editor.putString("email", email);
                            editor.putString("password", password);
                        } else {
                            editor.remove("email");
                            editor.remove("password");
                        }
                        editor.apply();

                        navigateToDashboard(userRole);
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Authentication failed";

                        if (errorMessage.contains("There is no user record")) {
                            Toast.makeText(LoginActivity.this,
                                    "No account found. Please sign up first.",
                                    Toast.LENGTH_LONG).show();
                        } else if (errorMessage.contains("The password is invalid")) {
                            Toast.makeText(LoginActivity.this,
                                    "Incorrect password. Please try again.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Login failed: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void navigateToDashboard(String role) {
        if ("ENTREPRENEUR".equals(role) || "CLIENT".equals(role)) {
            SessionHelper.openDashboard(this, role);
            finish();
        } else {
            SessionHelper.openStartScreen(this);
            finish();
        }
    }

    private void showForgotPasswordDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        builder.setMessage("Enter your email address to receive password reset instructions.");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Email");
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                sendPasswordResetEmail(email);
            } else {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void sendPasswordResetEmail(String email) {
        setLoading(true);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,
                                "Password reset email sent to " + email,
                                Toast.LENGTH_LONG).show();
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Failed to send reset email";
                        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
        btnLogin.setText(isLoading ? "LOGGING IN..." : "LOGIN");
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        btnGoRegister.setEnabled(!isLoading);
        tvForgotPassword.setEnabled(!isLoading);
        cardGoogleLogin.setEnabled(!isLoading);
        cardFacebookLogin.setEnabled(!isLoading);
        cardAppleLogin.setEnabled(!isLoading);
    }
}