package com.example.news_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.google.firebase.FirebaseApp;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_TIME = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView logoImage = findViewById(R.id.logoImage);

        // Start with logo off-screen (left)
        logoImage.setTranslationX(750f);

        // Load and apply linear animation (fade-in + move up)
        Animation animation = AnimationUtils.loadAnimation(this, R.animator.live_path_translate);

        // Delay the animation start by 500ms
        logoImage.postDelayed(() -> {
            logoImage.startAnimation(animation);
        }, 800);

        // Move to LoginActivity after splash delay
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DISPLAY_TIME);
    }
}
