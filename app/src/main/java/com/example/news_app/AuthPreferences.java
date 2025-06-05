package com.example.news_app;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;

public class AuthPreferences {
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_REMEMBER_ME = "remember_me";

    public AuthPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Save login state with all required user data
    public void saveLoginState(boolean isLoggedIn, String username, String email, boolean rememberMe) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.apply();
    }

    // Clear all saved preferences
    public void clearLoginState() {
        editor.clear();
        editor.apply();
    }

    // Check if user should be automatically logged in
    public boolean shouldAutoLogin() {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false) &&
                sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Get saved username
    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, "");
    }

    // Get saved email
    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, "");
    }

    // Check if "Remember Me" was enabled
    public boolean isRememberMeEnabled() {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
    }

    // Verify Firebase authentication status
    public boolean isUserAuthenticated() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    // Interface for login callback
    public interface AuthCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    // Attempt automatic login
    public void attemptAutoLogin(@NonNull Context context, @NonNull AuthCallback callback) {
        if (!shouldAutoLogin()) {
            callback.onFailure("Auto login not enabled");
            return;
        }

        // Check if Firebase already has an active session
        if (isUserAuthenticated()) {
            callback.onSuccess();
        } else {
            // No active session, require manual login
            callback.onFailure("Session expired. Please log in again.");
        }

        String email = getEmail();
        if (email.isEmpty()) {
            callback.onFailure("No saved email found");
            return;
        }

        // Check if already authenticated
        if (isUserAuthenticated()) {
            callback.onSuccess();
            return;
        }

        // Try silent login (Note: Firebase persists session by default)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, "dummy_password")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        clearLoginState();
                        callback.onFailure(task.getException() != null ?
                                task.getException().getMessage() : "Auto login failed");
                    }
                });
    }
}