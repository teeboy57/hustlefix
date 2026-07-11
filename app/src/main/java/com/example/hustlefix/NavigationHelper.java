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

/**
 * Shared navigation drawer and menu handling for the app.
 */
public final class NavigationHelper {

    private NavigationHelper() {}

    public static void setupDrawer(
            AppCompatActivity activity,
            DrawerLayout drawerLayout,
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
        if (itemId == R.id.nav_home) {
            openHome(activity);
            return true;
        }
        if (itemId == R.id.nav_post_job) {
            activity.startActivity(new Intent(activity, PostServiceActivity.class));
            if (!(activity instanceof PostServiceActivity)) {
                activity.finish();
            }
            return true;
        }
        if (itemId == R.id.nav_find_workers) {
            activity.startActivity(new Intent(activity, FindWorkersActivity.class));
            if (!(activity instanceof FindWorkersActivity)) {
                activity.finish();
            }
            return true;
        }
        if (itemId == R.id.nav_quotes) {
            activity.startActivity(new Intent(activity, QuotesActivity.class));
            if (!(activity instanceof QuotesActivity)) {
                activity.finish();
            }
            return true;
        }
        if (itemId == R.id.nav_chat) {
            ChatLauncher.openChatList(activity);
            return true;
        }
        if (itemId == R.id.nav_ratings) {
            activity.startActivity(new Intent(activity, RatingsActivity.class));
            if (!(activity instanceof RatingsActivity)) {
                activity.finish();
            }
            return true;
        }
        if (itemId == R.id.nav_emergency) {
            activity.startActivity(new Intent(activity, EmergencyRequestActivity.class));
            return true;
        }
        if (itemId == R.id.nav_settings) {
            activity.startActivity(new Intent(activity, SettingsActivity.class));
            return true;
        }
        if (itemId == R.id.nav_logout) {
            confirmLogout(activity);
            return true;
        }
        return false;
    }

    public static void openHome(Activity activity) {
        String role = SessionHelper.getRole(activity);
        if (SessionHelper.isLoggedIn(activity)) {
            SessionHelper.openDashboard(activity, role);
        } else {
            activity.startActivity(new Intent(activity, WelcomeActivity.class));
        }
        if (!(activity instanceof ClientDashboardActivity)
                && !(activity instanceof EntrepreneurDashboardActivity)) {
            activity.finish();
        }
    }

    public static void confirmLogout(Activity activity) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.logout)
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton(R.string.yes, (d, w) -> {
                    SessionHelper.logout(activity);
                    activity.finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    /** Toolbar menu (⋮) for screens without a drawer layout. */
    public static void showNavigationDialog(AppCompatActivity activity) {
        String role = SessionHelper.getRole(activity);
        String[] items = buildMenuLabels(role);
        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.dashboard)
                .setItems(items, (dialog, which) -> navigateByIndex(activity, which, role))
                .show();
    }

    private static String[] buildMenuLabels(String role) {
        if ("ENTREPRENEUR".equals(role)) {
            return new String[]{
                    "Home", "Post a Job", "Find Workers", "Quotes & Offers",
                    "Messages", "Ratings", "Emergency", "Settings", "Logout"
            };
        }
        return new String[]{
                "Home", "Post a Job", "Find Workers", "Quotes & Offers",
                "Messages", "Ratings", "Emergency", "Settings", "Logout"
        };
    }

    private static void navigateByIndex(Activity activity, int index, String role) {
        switch (index) {
            case 0:
                openHome(activity);
                break;
            case 1:
                activity.startActivity(new Intent(activity, PostServiceActivity.class));
                break;
            case 2:
                activity.startActivity(new Intent(activity, FindWorkersActivity.class));
                break;
            case 3:
                activity.startActivity(new Intent(activity, QuotesActivity.class));
                break;
            case 4:
                ChatLauncher.openChatList(activity);
                break;
            case 5:
                activity.startActivity(new Intent(activity, RatingsActivity.class));
                break;
            case 6:
                activity.startActivity(new Intent(activity, EmergencyRequestActivity.class));
                break;
            case 7:
                activity.startActivity(new Intent(activity, SettingsActivity.class));
                break;
            case 8:
                confirmLogout(activity);
                break;
            default:
                break;
        }
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
