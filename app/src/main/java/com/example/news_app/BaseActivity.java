package com.example.news_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    protected boolean isMuted = false;
    protected ImageView notificationIcon;
    protected ImageView navIcon; // Changed from menuIcon to navIcon

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupToolbar(boolean isHomeActivity) {
        notificationIcon = findViewById(R.id.notification);
        navIcon = findViewById(R.id.navIcon); // Updated ID

        // Set appropriate icon based on activity type
        if (isHomeActivity) {
            navIcon.setImageResource(R.drawable.menu);
            navIcon.setOnClickListener(v -> handleMenuClick());
        } else {
            navIcon.setImageResource(R.drawable.ic_back);
            navIcon.setOnClickListener(v -> onBackPressed());
        }

        loadMuteState();
        updateNotificationIcon(); // Initialize the correct icon state
        notificationIcon.setOnClickListener(v -> toggleNotificationState());

        ImageView appLogo = findViewById(R.id.applogo);
        appLogo.setOnClickListener(v -> handleLogoClick());
    }

    // Method to toggle notification state
    private void toggleNotificationState() {
        isMuted = !isMuted;
        updateNotificationIcon();
        saveMuteState(isMuted);
        showToast(isMuted ? "Notifications Muted" : "Notifications Unmuted");
    }

    // Update the notification icon based on mute state
    private void updateNotificationIcon() {
        if (notificationIcon != null) {
            notificationIcon.setImageResource(
                    isMuted ? R.drawable.mute : R.drawable.unmute
            );
        }
    }

    private void saveMuteState(boolean state) {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("mute_state", state).apply();
    }

    private void loadMuteState() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        isMuted = prefs.getBoolean("mute_state", false);
        updateNotificationIcon();
    }
    // Show toast message
    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void handleMenuClick() {
        // Default implementation - can be overridden
        // Example: toggle drawer or go back
    }

    protected void handleLogoClick() {
        // Default implementation - can be overridden
        // Example: go to home if not already there
    }
}