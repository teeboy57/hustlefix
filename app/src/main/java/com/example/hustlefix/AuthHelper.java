package com.example.hustlefix;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class AuthHelper {
    public static final int RC_GOOGLE_SIGN_IN = 9001;

    private AuthHelper() {}

    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }

    public static boolean isGoogleConfigured(Context context) {
        String clientId = getWebClientId(context);
        return !TextUtils.isEmpty(clientId)
                && !clientId.startsWith("YOUR_")
                && !clientId.contains("placeholder");
    }

    public static void signInWithGoogle(AppCompatActivity activity, String userRole, AuthCallback callback) {
        if (!isGoogleConfigured(activity)) {
            callback.onError(activity.getString(R.string.auth_google_not_configured));
            return;
        }

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getWebClientId(activity))
                .requestEmail()
                .build();

        GoogleSignInClient client = GoogleSignIn.getClient(activity, options);
        activity.startActivityForResult(client.getSignInIntent(), RC_GOOGLE_SIGN_IN);
        activity.getIntent().putExtra("PENDING_AUTH_ROLE", userRole);
    }

    public static void handleGoogleSignInResult(
            AppCompatActivity activity, int requestCode,
            int resultCode,
            android.content.Intent data, String userRole,
            AuthCallback callback) {

        if (requestCode != RC_GOOGLE_SIGN_IN) {
            return;
        }

        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account == null || account.getIdToken() == null) {
                callback.onError("Google sign-in failed");
                return;
            }

            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            signInWithCredential(activity, credential, userRole, account.getDisplayName(), account.getEmail(), callback);
        } catch (ApiException e) {
            callback.onError("Google sign-in failed: " + e.getMessage());
        }
    }

    public static void signInWithApple(AppCompatActivity activity, String userRole, AuthCallback callback) {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("apple.com");
        provider.setScopes(Arrays.asList("email", "name"));

        FirebaseAuth.getInstance()
                .startActivityForSignInWithProvider(activity, provider.build())
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        ensureUserProfile(user, userRole, user.getDisplayName(), user.getEmail());
                    }
                    completeSocialLogin(activity, user, userRole, callback);
                })
                .addOnFailureListener(e ->
                        callback.onError("Apple sign-in failed. Enable Apple provider in Firebase Console.\n" + e.getMessage()));
    }

    private static void signInWithCredential(
            Activity activity, AuthCredential credential,
            String userRole, String displayName,
            String email,
            AuthCallback callback) {

        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null && displayName != null && !displayName.isEmpty()
                                && (user.getDisplayName() == null || user.getDisplayName().isEmpty())) {
                            user.updateProfile(new UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName)
                                    .build());
                        }
                        ensureUserProfile(user, userRole, displayName, email);
                        completeSocialLogin(activity, user, userRole, callback);
                    } else {
                        String msg = task.getException() != null ? task.getException().getMessage() : "Sign-in failed";
                        callback.onError(msg);
                    }
                });
    }

    private static void completeSocialLogin(Activity activity, FirebaseUser user, String userRole, AuthCallback callback) {
        if (user == null) {
            callback.onError("Sign-in failed");
            return;
        }

        SessionHelper.setLoggedIn(activity, true);
        SessionHelper.saveRole(activity, userRole);
        SessionHelper.prefs(activity).edit()
                .putBoolean("isLoggedIn", true)
                .putString("userRole", userRole)
                .putString("userEmail", user.getEmail() != null ? user.getEmail() : "")
                .apply();

        callback.onSuccess();
    }

    private static void ensureUserProfile(FirebaseUser user, String userRole, String name, String email) {
        String uid = user.getUid();
        String firebaseRole = SessionHelper.firebaseRoleForAppRole(userRole);

        String displayName = name;
        if (displayName == null || displayName.isEmpty()) {
            displayName = user.getDisplayName();
        }
        if (displayName == null || displayName.isEmpty()) {
            String userEmail = email != null ? email : user.getEmail();
            if (userEmail != null && userEmail.contains("@")) {
                displayName = userEmail.split("@")[0];
            } else {
                displayName = "User";
            }
        }

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", displayName);
        userMap.put("email", email != null ? email : user.getEmail());
        userMap.put("role", firebaseRole);
        userMap.put("available", true);
        userMap.put("rating", 0);
        userMap.put("isVerified", false);
        userMap.put("isSuspended", false);
        userMap.put("createdAt", System.currentTimeMillis());

        FirebaseDatabase.getInstance().getReference("users").child(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        FirebaseDatabase.getInstance().getReference("users").child(uid).setValue(userMap);
                    }
                });
    }

    private static String getWebClientId(Context context) {
        int resId = context.getResources().getIdentifier("default_web_client_id", "string", context.getPackageName());
        if (resId != 0) {
            String generated = context.getString(resId);
            if (!TextUtils.isEmpty(generated) && !generated.startsWith("YOUR_")) {
                return generated;
            }
        }
        return context.getString(R.string.default_web_client_id);
    }
}