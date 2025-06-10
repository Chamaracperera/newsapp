package com.example.news_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_TIME = 3000; // 3 seconds
    private AuthPreferences authPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

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

