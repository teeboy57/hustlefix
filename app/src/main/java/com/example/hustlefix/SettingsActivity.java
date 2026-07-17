package com.example.hustlefix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private MaterialCardView cardProfile;
    private View cardPrivacy;
    private View cardLanguage;
    private View cardHelp;
    private View cardAbout;
    private View cardAdmin;

    private View cardNotifications;
    private View cardTheme;

    private MaterialButton cardLogout;

    private Switch switchNotifications;
    private Switch switchDarkMode;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvLanguage;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = SessionHelper.prefs(this);

        initViews();
        setupToolbar();
        loadUserInfo();
        setupClickListeners();
        loadSettings();
        setupAdminReveal();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);

        // ✅ All MaterialCardView
        cardProfile = findViewById(R.id.cardProfile);
        cardPrivacy = findViewById(R.id.cardPrivacy);
        cardLanguage = findViewById(R.id.cardLanguage);
        cardHelp = findViewById(R.id.cardHelp);
        cardAbout = findViewById(R.id.cardAbout);
        cardAdmin = findViewById(R.id.cardAdmin);

        // ✅ View (could be LinearLayout or any other view)
        cardNotifications = findViewById(R.id.cardNotifications);
        cardTheme = findViewById(R.id.cardTheme);

        // ✅ MaterialButton
        cardLogout = findViewById(R.id.cardLogout);

        switchNotifications = findViewById(R.id.switchNotifications);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvLanguage = findViewById(R.id.tvLanguage);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return NavigationHelper.onOptionsItemSelected(this, item) || super.onOptionsItemSelected(item);
    }

    private void loadUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                String email = user.getEmail();
                if (email != null && email.contains("@")) {
                    name = email.split("@")[0];
                } else {
                    name = "User";
                }
            }
            tvUserName.setText(name);
            tvUserEmail.setText(user.getEmail());
        } else {
            tvUserName.setText("Guest User");
            tvUserEmail.setText("Not logged in");
        }
    }

    private void setupClickListeners() {
        // ✅ Profile click - check for null
        if (cardProfile != null) {
            cardProfile.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        // ✅ Privacy click
        if (cardPrivacy != null) {
            cardPrivacy.setOnClickListener(v -> showPrivacyDialog());
        }

        // ✅ Language click
        if (cardLanguage != null) {
            cardLanguage.setOnClickListener(v -> showLanguageDialog());
        }

        // ✅ Help click
        if (cardHelp != null) {
            cardHelp.setOnClickListener(v -> showHelpDialog());
        }

        // ✅ About click
        if (cardAbout != null) {
            cardAbout.setOnClickListener(v -> showAboutDialog());
        }

        // ✅ Logout click - MaterialButton
        if (cardLogout != null) {
            cardLogout.setOnClickListener(v -> logout());
        }

        // ✅ Admin click
        if (cardAdmin != null) {
            cardAdmin.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, AdminActivity.class);
                startActivity(intent);
            });
        }

        // ✅ Notifications Switch
        if (switchNotifications != null) {
            switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("notifications_enabled", isChecked);
                editor.apply();
                String message = isChecked ? "Notifications enabled" : "Notifications disabled";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            });
        }

        // ✅ Dark Mode Switch
        if (switchDarkMode != null) {
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("dark_mode_enabled", isChecked);
                editor.apply();
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                Toast.makeText(this, "Theme will change on next restart", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupAdminReveal() {
        // ✅ Long press on About card to reveal Admin panel
        if (cardAbout != null && cardAdmin != null) {
            cardAbout.setOnLongClickListener(v -> {
                if (cardAdmin.getVisibility() == View.GONE) {
                    cardAdmin.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, "Admin mode enabled", Toast.LENGTH_SHORT).show();
                } else {
                    cardAdmin.setVisibility(View.GONE);
                    Toast.makeText(SettingsActivity.this, "Admin mode disabled", Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }
    }

    private void showLanguageDialog() {
        final String[] languages = {"English", "Afrikaans", "isiZulu", "isiXhosa"};
        final String[] languageCodes = {
                LanguageManager.LANGUAGE_ENGLISH,
                LanguageManager.LANGUAGE_AFRIKAANS,
                LanguageManager.LANGUAGE_ZULU,
                LanguageManager.LANGUAGE_XHOSA
        };

        String currentLang = LanguageManager.getLanguage(this);
        int currentIndex = 0;
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLang)) {
                currentIndex = i;
                break;
            }
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Select Language / Khetha Ulimi / Kies Taal");
        builder.setSingleChoiceItems(languages, currentIndex, (dialog, which) -> {
            String selectedCode = languageCodes[which];
            if (!selectedCode.equals(currentLang)) {
                LanguageManager.changeLanguage(SettingsActivity.this, selectedCode);
            }
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void loadSettings() {
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode_enabled", false);

        if (switchNotifications != null) {
            switchNotifications.setChecked(notificationsEnabled);
        }
        if (switchDarkMode != null) {
            switchDarkMode.setChecked(darkModeEnabled);
        }

        // Update language display
        String currentLang = LanguageManager.getLanguage(this);
        String displayName;
        switch (currentLang) {
            case LanguageManager.LANGUAGE_AFRIKAANS:
                displayName = "Afrikaans";
                break;
            case LanguageManager.LANGUAGE_ZULU:
                displayName = "isiZulu";
                break;
            case LanguageManager.LANGUAGE_XHOSA:
                displayName = "isiXhosa";
                break;
            default:
                displayName = "English";
        }
        if (tvLanguage != null) {
            tvLanguage.setText(displayName);
        }
    }

    private void showPrivacyDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Privacy Policy")
                .setMessage("HustleFix is committed to protecting your privacy. We collect only necessary information to connect you with workers and clients. Your data is never shared without your consent.\n\n" +
                        "• Personal information is encrypted\n" +
                        "• Location data is only used for job matching\n" +
                        "• You can delete your account anytime")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showHelpDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Help & Support")
                .setMessage("Need help? Contact us:\n\n" +
                        "✉ Email: support@hustlefix.com\n" +
                        "📞 Phone: +27 123 456 789\n" +
                        "🌐 Website: www.hustlefix.com\n\n" +
                        "Or visit our FAQ section in the app.")
                .setPositiveButton("OK", null)
                .setNeutralButton("Email Support", (dialog, which) -> {
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("message/rfc822");
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@hustlefix.com"});
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "HustleFix Support Request");
                    startActivity(Intent.createChooser(emailIntent, "Send email"));
                })
                .show();
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("About HustleFix")
                .setMessage("HustleFix v2.0\n\n" +
                        "Connect • Work • Grow\n\n" +
                        "HustleFix connects clients with skilled workers in your area. \n\n" +
                        "© 2024 HustleFix. All rights reserved.\n\n" +
                        "Made with ❤️ in South Africa")
                .setPositiveButton("OK", null)
                .show();
    }

    private void logout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    SessionHelper.logout(SettingsActivity.this);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}