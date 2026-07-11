package com.example.hustlefix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public abstract class BaseDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;
    protected FirebaseAuth mAuth;
    protected SharedPreferences sharedPreferences;
    protected FirebaseUser currentUser;

    protected String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        sharedPreferences = SessionHelper.prefs(this);
        userRole = SessionHelper.getRole(this);
    }

    protected void setupNavigationDrawer() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            if (drawerLayout != null) {
                ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                        this, drawerLayout, toolbar,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close);
                drawerLayout.addDrawerListener(toggle);
                toggle.syncState();
            }
        }

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
    }

    protected void updateNavHeader() {
        if (navigationView != null && navigationView.getHeaderView(0) != null) {
            View headerView = navigationView.getHeaderView(0);
            TextView tvUserName = headerView.findViewById(R.id.tvNavUserName);
            TextView tvUserEmail = headerView.findViewById(R.id.tvNavUserEmail);
            ImageView ivAvatar = headerView.findViewById(R.id.ivNavAvatar);

            if (currentUser != null) {
                String name = currentUser.getDisplayName();
                if (name == null || name.isEmpty()) {
                    String email = currentUser.getEmail();
                    if (email != null && email.contains("@")) {
                        name = email.split("@")[0];
                    } else {
                        name = "User";
                    }
                }
                if (tvUserName != null) tvUserName.setText(name);
                if (tvUserEmail != null) tvUserEmail.setText(currentUser.getEmail());
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            if (drawerLayout != null) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            return true;
        } else if (id == R.id.nav_post_job) {
            startActivity(new Intent(this, PostServiceActivity.class));
        } else if (id == R.id.nav_find_workers) {
            startActivity(new Intent(this, FindWorkersActivity.class));
        } else if (id == R.id.nav_quotes) {
            startActivity(new Intent(this, QuotesActivity.class));
        } else if (id == R.id.nav_chat) {
            ChatLauncher.openChatList(this);
        } else if (id == R.id.nav_ratings) {
            startActivity(new Intent(this, RatingsActivity.class));
        } else if (id == R.id.nav_emergency) {
            startActivity(new Intent(this, EmergencyRequestActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_logout) {
            logout();
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    protected void logout() {
        SessionHelper.logout(this);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Optional: Auto logout when app is closed
        // Uncomment the line below if you want to logout when app is closed
        // logout();
    }
}