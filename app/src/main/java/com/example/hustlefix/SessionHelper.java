package com.example.hustlefix;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
/**
 * Shared session / role preferences used across the app.
 */
public final class SessionHelper {
    public static final String PREFS_NAME = "HustleFixPrefs";
    private static final String KEY_ROLE = "userRole";
    private static final String KEY_LOGGED_IN = "isLoggedIn";
    private static final String LEGACY_PREFS = "HustleFix";
    private static final String LEGACY_ROLE = "USER_ROLE";
    private SessionHelper() {}
    public static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    public static void saveRole(Context context, String role) {
        if (TextUtils.isEmpty(role)) return;
        prefs(context).edit().putString(KEY_ROLE, role).apply();
        context.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(LEGACY_ROLE, role)
                .apply();
    }
    public static String getRole(Context context) {
        String role = prefs(context).getString(KEY_ROLE, "");
        if (!TextUtils.isEmpty(role)) return role;
        return context.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)
                .getString(LEGACY_ROLE, "CLIENT");
    }
    public static boolean isLoggedIn(Context context) {
        return prefs(context).getBoolean(KEY_LOGGED_IN, false)
                && FirebaseAuth.getInstance().getCurrentUser() != null;
    }
    public static void setLoggedIn(Context context, boolean loggedIn) {
        prefs(context).edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply();
    }
    /** Firebase users/{uid}.role value for marketplace listing. */
    public static String firebaseRoleForAppRole(String appRole) {
        return "ENTREPRENEUR".equals(appRole) ? "worker" : "client";
    }
    public static void openDashboard(Context context, String role) {
        Intent intent;
        if ("ENTREPRENEUR".equals(role)) {
            intent = new Intent(context, EntrepreneurDashboardActivity.class);
        } else {
            intent = new Intent(context, ClientDashboardActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
    public static void openStartScreen(Context context) {
        Intent intent = new Intent(context, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
    public static void logout(Context context) {
        FirebaseAuth.getInstance().signOut();
        prefs(context).edit()
                .putBoolean(KEY_LOGGED_IN, false)
                .remove("email")
                .remove("password")
                .apply();
        openStartScreen(context);
    }
}
