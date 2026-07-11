package com.example.hustlefix;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
public class BaseDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;
    protected FirebaseAuth mAuth;
    protected String userRole;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        userRole = SessionHelper.getRole(this);
    }
    protected void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    protected void setupNavigationDrawer() {
        NavigationHelper.setupDrawer(this, drawerLayout, toolbar, navigationView);
    }
    protected void updateNavHeader() {
        if (navigationView != null && navigationView.getHeaderView(0) != null) {
            View headerView = navigationView.getHeaderView(0);
            TextView tvNavUserName = headerView.findViewById(R.id.tvNavUserName);
            TextView tvNavUserEmail = headerView.findViewById(R.id.tvNavUserEmail);
            ImageView ivNavAvatar = headerView.findViewById(R.id.ivNavAvatar);
            FirebaseUser currentUser = mAuth.getCurrentUser();
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
                tvNavUserName.setText(name);
                tvNavUserEmail.setText(currentUser.getEmail());
            }
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_post_job) {
            startActivity(new Intent(this, PostServiceActivity.class));
            return true;
        } else if (id == R.id.nav_my_bookings) {
            Toast.makeText(this, "My Bookings", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.nav_my_services) {
            Toast.makeText(this, "My Services", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.nav_analytics) {
            Toast.makeText(this, "Analytics", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.nav_logout) {
            logout();
            return true;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    private void logout() {
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
}