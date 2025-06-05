package com.example.news_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private RecyclerView featuredRecycler, newsRecyclerView;
    private FeaturedNewsAdapter featuredAdapter;
    private RegularNewsAdapter regularAdapter;
    private final List<NewsItem> featuredList = new ArrayList<>();
    private final List<NewsItem> newsItemList = new ArrayList<>();
    private final List<NewsItem> allItems = new ArrayList<>();
    private final Map<String, List<NewsItem>> categoryMap = new HashMap<>();
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("events");
    private TextView tabAll, tabSports, tabAcademic, tabEvents;
    private View currentlySelectedTab;

    // Notification icon
    private ImageView notificationIcon;
    private boolean isMuted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        setupAdapters();
        setupTabListeners();
        loadNewsData();
    }

    private void initializeViews() {
        featuredRecycler = findViewById(R.id.featuredRecycler);
        newsRecyclerView = findViewById(R.id.newsRecyclerView);

        tabAll = findViewById(R.id.tabAll);
        tabSports = findViewById(R.id.tabSports);
        tabAcademic = findViewById(R.id.tabAcademic);
        tabEvents = findViewById(R.id.tabEvents);

        currentlySelectedTab = tabAll;
        tabAll.setBackgroundResource(R.drawable.bg_tab_selected);

        // Notification icon setup
        notificationIcon = findViewById(R.id.notification);
        loadMuteState(); // Set initial state

        notificationIcon.setOnClickListener(v -> {
            isMuted = !isMuted;
            updateNotificationIcon();
            saveMuteState(isMuted);
        });
    }
    private void updateNotificationIcon() {
        if (isMuted) {
            notificationIcon.setImageResource(R.drawable.mute);
            Toast.makeText(this, "Notifications Muted", Toast.LENGTH_SHORT).show();
        } else {
            notificationIcon.setImageResource(R.drawable.unmute);
            Toast.makeText(this, "Notifications Unmuted", Toast.LENGTH_SHORT).show();
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


    private void setupAdapters() {
        // Featured news (horizontal scrolling)
        featuredRecycler.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        featuredAdapter  = new FeaturedNewsAdapter(this, featuredList);
        featuredRecycler.setAdapter(featuredAdapter );

        // Regular news (vertical scrolling)
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        regularAdapter  = new RegularNewsAdapter(this, newsItemList);
        newsRecyclerView.setAdapter(regularAdapter );
    }

    private void setupTabListeners() {
        setupTabClickListener(tabAll, "All");
        setupTabClickListener(tabSports, "Sports");
        setupTabClickListener(tabAcademic, "Academic");
        setupTabClickListener(tabEvents, "Events");
    }

    private void setupTabClickListener(View tabView, String category) {
        tabView.setOnClickListener(v -> {
            if (currentlySelectedTab != null) {
                currentlySelectedTab.setBackgroundResource(R.drawable.bg_tab_unselected);
            }
            tabView.setBackgroundResource(R.drawable.bg_tab_selected);
            currentlySelectedTab = tabView;
            filterNewsByCategory(category);
        });
    }

    private void filterNewsByCategory(String category) {
        Log.d(TAG, "Filtering by category: " + category);
        newsItemList.clear();

        if ("All".equals(category)) {
            newsItemList.addAll(allItems);
        } else {
            List<NewsItem> categoryItems = categoryMap.get(category);
            if (categoryItems != null) {
                newsItemList.addAll(categoryItems);
            }
        }

        Collections.sort(newsItemList, (item1, item2) ->
                Long.compare(item2.getTimestamp(), item1.getTimestamp()));

        regularAdapter.notifyDataSetChanged();
        Log.d(TAG, "News items after filtering: " + newsItemList.size());
    }

    private void loadNewsData() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Not logged in — show message or redirect to Login
            Toast.makeText(this, "Please login to view events", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                featuredList.clear();
                newsItemList.clear();
                allItems.clear();
                categoryMap.clear();

                // Debug: Check if we're getting data
                Log.d(TAG, "Data count: " + snapshot.getChildrenCount());

                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    NewsItem newsItem = eventSnapshot.getValue(NewsItem.class);
                    if (newsItem != null) {
                        newsItem.setId(eventSnapshot.getKey()); // Make sure setId() exists
                        allItems.add(newsItem);

                        // Debug each item
                        Log.d(TAG, "Loaded item: " + newsItem.getTitle() +
                                " | Category: " + newsItem.getCategory() +
                                " | Image: " + newsItem.getImageUrl());

                        // Add to category map
                        String category = newsItem.getCategory();
                        if (category != null) {
                            if (!categoryMap.containsKey(category)) {
                                categoryMap.put(category, new ArrayList<>());
                            }
                            categoryMap.get(category).add(newsItem);
                        }
                    }
                }

                // Sort all items by timestamp (newest first)
                Collections.sort(allItems, (i1, i2) -> Long.compare(i2.getTimestamp(), i1.getTimestamp()));

                // Sort each category list too
                for (List<NewsItem> list : categoryMap.values()) {
                    Collections.sort(list, (i1, i2) -> Long.compare(i2.getTimestamp(), i1.getTimestamp()));
                }

                // Populate featured list (top 3 newest items)
                featuredList.clear();
                featuredList.addAll(allItems.subList(0, Math.min(3, allItems.size())));

                // Apply current filter
                String currentCategory = getCurrentCategory();
                filterNewsByCategory(currentCategory);

                runOnUiThread(() -> {
                    featuredAdapter.notifyDataSetChanged();
                    regularAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase error: " + error.getMessage());
                Toast.makeText(HomeActivity.this,
                        "Failed to load news: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getCurrentCategory() {
        if (currentlySelectedTab == tabSports) return "Sports";
        if (currentlySelectedTab == tabAcademic) return "Academic";
        if (currentlySelectedTab == tabEvents) return "Events";
        return "All";
    }
}