package com.example.hustlefix;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public final class NavigationHelper {
    private NavigationHelper() {}

    public static void setupDrawer(
            AppCompatActivity activity, DrawerLayout drawerLayout,
            Toolbar toolbar,
            NavigationView navigationView) {
        if (drawerLayout == null || toolbar == null || navigationView == null) {
            return;
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            boolean handled = handleNavigationItem(activity, item.getItemId());
            drawerLayout.closeDrawer(GravityCompat.START);
            return handled;
        });

        updateDrawerHeader(activity, navigationView);
    }

    public static void updateDrawerHeader(Activity activity, NavigationView navigationView) {
        if (navigationView == null || navigationView.getHeaderView(0) == null) {
            return;
        }

        View header = navigationView.getHeaderView(0);
        TextView tvName = header.findViewById(R.id.tvNavUserName);
        TextView tvEmail = header.findViewById(R.id.tvNavUserEmail);
        ImageView ivAvatar = header.findViewById(R.id.ivNavAvatar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                String email = user.getEmail();
                name = email != null && email.contains("@") ? email.split("@")[0] : "User";
            }
            if (tvName != null) tvName.setText(name);
            if (tvEmail != null) tvEmail.setText(user.getEmail());
        }

        if (ivAvatar != null) {
            ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        }
    }

    public static boolean handleNavigationItem(Activity activity, int itemId) {
        // Redirect everything to MainActivity for Compose Navigation
        Intent intent = new Intent(activity, MainActivity.class);
        
        if (itemId == R.id.nav_home) {
            openHome(activity);
            return true;
        }

        // For now, we just go to MainActivity. In the future, we can add extras to navigate to specific screens.
        activity.startActivity(intent);
        return true;
    }

    public static void openHome(Activity activity) {
        String role = SessionHelper.getRole(activity);
        if (SessionHelper.isLoggedIn(activity)) {
            SessionHelper.openDashboard(activity, role);
        } else {
            activity.startActivity(new Intent(activity, MainActivity.class));
        }
        
        if (!(activity instanceof MainActivity)) {
            activity.finish();
        }
    }

    public static void confirmLogout(Activity activity) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (d, w) -> {
                    SessionHelper.logout(activity);
                    activity.finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    public static void showNavigationDialog(AppCompatActivity activity) {
        String role = SessionHelper.getRole(activity);
        String[] items = buildMenuLabels(role);
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Dashboard")
                .setItems(items, (dialog, which) -> navigateByIndex(activity, which, role))
                .show();
    }

    private static String[] buildMenuLabels(String role) {
        if ("service_provider".equals(role)) {
            return new String[]{
                    "Home", "Post a Service", "My Services", "My Bookings",
                    "My Wallet", "Analytics", "Settings", "Logout"
            };
        }
        return new String[]{
                "Home", "Find Services", "My Bookings", "My Wallet", "Settings", "Logout"
        };
    }

    private static void navigateByIndex(Activity activity, int index, String role) {
        openHome(activity);
    }

    public static boolean onOptionsItemSelected(AppCompatActivity activity, @NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_nav_menu) {
            showNavigationDialog(activity);
            return true;
        }
        if (item.getItemId() == R.id.action_home) {
            openHome(activity);
            return true;
        }
        return false;
    }
}
