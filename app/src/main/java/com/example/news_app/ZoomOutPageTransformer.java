package com.example.news_app;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class ZoomOutPageTransformer implements ViewPager2.PageTransformer {
    private static final float MIN_SCALE = 0.80f;  // Make this smaller for more shrink on sides (try 0.8f, 0.75f)
    private static final float MIN_ALPHA = 0.6f;   // Transparency on sides

    @Override
    public void transformPage(@NonNull View page, float position) {
        if (position < -1) {
            // [-Infinity,-1)
            page.setAlpha(0f);
        } else if (position <= 1) {
            // [-1,1]
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            float alphaFactor = Math.max(MIN_ALPHA, 1 - Math.abs(position));

            page.setScaleY(scaleFactor);
            page.setScaleX(scaleFactor);
            page.setAlpha(alphaFactor);

            // Optional: Add horizontal translation to improve effect (feels more natural)
            float translationX = -position * page.getWidth() * 0.1f;
            page.setTranslationX(translationX);

        } else {
            // (1,+Infinity]
            page.setAlpha(0f);
        }
    }
}
