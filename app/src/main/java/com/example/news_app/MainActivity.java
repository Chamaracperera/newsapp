package com.example.news_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class MainActivity extends AppCompatActivity {
    private static final int SPLASH_DISPLAY_TIME = 3000; // 3 seconds
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;
    private static final String CHANNEL_ID = "news_app_channel";
    private AuthPreferences authPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        // Initialize notification channel
        createNotificationChannel();

        // Request notification permission (Android 13+)
        requestNotificationPermission();

        makeStatusBarTransparent();

        // Initialize AuthPreferences
        authPreferences = new AuthPreferences(this);

        ImageView logoImage = findViewById(R.id.logoImage);
        ImageView fotlogo = findViewById(R.id.fotlogo);

        // Load fade-in animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.animator.fade_in);
        fotlogo.startAnimation(fadeIn);

        // Start with logo off-screen (left)
        logoImage.setTranslationX(750f);

        // Load and apply linear animation (fade-in + move up)
        Animation animation = AnimationUtils.loadAnimation(this, R.animator.live_path_translate);

        // Delay the animation start by 500ms
        logoImage.postDelayed(() -> {
            logoImage.startAnimation(animation);
        }, 800);

        // After splash time, check auto login
        new Handler().postDelayed(this::checkAutoLogin, SPLASH_DISPLAY_TIME);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "News Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for news notifications");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void makeStatusBarTransparent() {
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    private void checkAutoLogin() {
        if (authPreferences.shouldAutoLogin()) {
            authPreferences.attemptAutoLogin(this, new AuthPreferences.AuthCallback() {
                @Override
                public void onSuccess() {
                    navigateTo(HomeActivity.class);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(MainActivity.this,
                            "Auto-login failed: " + errorMessage,
                            Toast.LENGTH_SHORT).show();
                    navigateTo(LoginActivity.class);
                }
            });
        } else {
            navigateTo(LoginActivity.class);
        }
    }

    private void navigateTo(Class<?> destination) {
        Intent intent = new Intent(MainActivity.this, destination);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}