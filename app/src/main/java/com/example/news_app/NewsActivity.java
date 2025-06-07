package com.example.news_app;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NewsActivity extends BaseActivity {
    private static final String TAG = "NewsActivity";

    private ImageView newsImage, likeIcon;
    private TextView newsTitle, newsDate, newsCategory, newsDescription, likeCount, newsTime;
    private DatabaseReference dbRef;
    private String newsId;
    private NewsItem newsItem;
    private LinearLayout likeContainer;
    private boolean isLiked = false;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        setupToolbar(false,false); // Initialize toolbar from BaseActivity
        initializeViews();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login to view news", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Check if we received a full NewsItem object or just an ID
        if (getIntent().hasExtra("news_item")) {
            newsItem = getIntent().getParcelableExtra("news_item");
            if (newsItem != null) {
                newsId = newsItem.getId(); // Set newsId from the item
                populateNewsDetails(newsItem);
                setupFirebaseListener();
            } else {
                showErrorAndFinish("Invalid news data");
            }
        } else if (getIntent().hasExtra("news_id")) {
            newsId = getIntent().getStringExtra("news_id");
            if (newsId != null && !newsId.isEmpty()) {
                setupFirebaseListener();
            } else {
                showErrorAndFinish("Missing news ID");
            }
        } else {
            showErrorAndFinish("No news data provided");
        }
    }

    private void initializeViews() {
        newsImage = findViewById(R.id.newsImage);
        newsTitle = findViewById(R.id.newsTitle);
        newsDate = findViewById(R.id.newsDate);
        newsCategory = findViewById(R.id.newsCategory);
        newsDescription = findViewById(R.id.newsDescription);
        likeCount = findViewById(R.id.likeCount);
        newsTime = findViewById(R.id.newsTime);
        likeIcon = findViewById(R.id.likeIcon);
        likeContainer = findViewById(R.id.likeContainer);

        // Set click listener for like button
        likeContainer.setOnClickListener(v -> toggleLike());

    }

    private void setupFirebaseListener() {
        dbRef = FirebaseDatabase.getInstance().getReference("events").child(newsId);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                NewsItem firebaseItem = snapshot.getValue(NewsItem.class);
                if (firebaseItem != null) {
                    newsItem = firebaseItem;
                    populateNewsDetails(newsItem);

                    // Force UI update
                    runOnUiThread(() -> {
                        updateLikeStateFromFirebase();
                        updateLikeUI();
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading news: " + error.getMessage());
                likeContainer.setEnabled(true);
            }
        });
    }

    private void populateNewsDetails(NewsItem newsItem) {
        // Set text values
        newsTitle.setText(newsItem.getTitle());
        newsCategory.setText(newsItem.getCategory());

        // Handle line breaks in description
        String description = newsItem.getDescription();
        if (description != null) {
            // Replace "\n" with actual line breaks
            description = description.replace("\\n", "\n");
            newsDescription.setText(description);
        } else {
            newsDescription.setText("");
        }

        likeCount.setText(String.valueOf(newsItem.getLikesCount()));

        // Format and set date/time
        if (newsItem.getTimestamp() > 0) {
            // Formatted date (e.g., "MMM dd, yyyy")
            String formattedDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(new Date(newsItem.getTimestamp()));
            newsDate.setText(formattedDate);

            // Relative time (e.g., "2h ago")
            newsTime.setText(getTimeAgo(newsItem.getTimestamp()));
        }

        // Load image using Glide
        if (newsItem.getImageUrl() != null && !newsItem.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(newsItem.getImageUrl())
                    .placeholder(R.drawable.ic_news_placeholder)
                    .error(R.drawable.ic_news_placeholder)
                    .centerCrop()
                    .into(newsImage);
        } else {
            newsImage.setImageResource(R.drawable.ic_news_placeholder);
        }
    }

    private void updateLikeStateFromFirebase() {
        if (newsItem == null || currentUser == null) return;

        Map<String, Boolean> likedBy = newsItem.getLikedBy();
        String userId = currentUser.getUid();

        // Default to false if not present
        isLiked = likedBy != null && Boolean.TRUE.equals(likedBy.get(userId));
    }

    private void updateLikeUI() {
        runOnUiThread(() -> {
            if (isLiked) {
                likeIcon.setImageResource(R.drawable.liked);
                likeIcon.setColorFilter(ContextCompat.getColor(this, R.color.white));
            } else {
                likeIcon.setImageResource(R.drawable.like);
                likeIcon.setColorFilter(ContextCompat.getColor(this, R.color.white));
            }
            likeCount.setText(String.valueOf(newsItem.getLikesCount()));
        });
    }

    private void toggleLike() {
        if (newsItem == null || newsId == null || currentUser == null) return;

        likeContainer.setEnabled(false);
        String userId = currentUser.getUid();

        DatabaseReference eventRef = FirebaseDatabase.getInstance()
                .getReference("events")
                .child(newsId);

        // Get current like state
        boolean currentlyLiked = newsItem.getLikedBy() != null &&
                newsItem.getLikedBy().getOrDefault(userId, false);

        // Create update map
        Map<String, Object> updates = new HashMap<>();
        updates.put("likedBy/" + userId, !currentlyLiked);
        updates.put("likesCount", newsItem.getLikesCount() + (currentlyLiked ? -1 : 1));

        // Single atomic update
        eventRef.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    likeContainer.setEnabled(true);
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Update failed", task.getException());
                        // Show error to user if needed
                    }
                });
    }



    private String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d";
        } else if (hours > 0) {
            return hours + "h";
        } else if (minutes > 0) {
            return minutes + "m";
        } else {
            return "Just now";
        }
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, message);
        if (!isFinishing()) {
            finish();
        }
    }


}