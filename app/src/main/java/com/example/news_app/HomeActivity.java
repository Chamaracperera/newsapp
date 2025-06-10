package com.example.news_app;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends BaseActivity implements
        FeaturedNewsAdapter.OnNewsClickListener,
        RegularNewsAdapter.OnNewsClickListener {

    private static final String TAG = "HomeActivity";
    private ViewPager2 featuredViewPager;
    private DotsIndicator dotsIndicator;
    private RecyclerView newsRecyclerView;
    private FeaturedNewsAdapter featuredAdapter;

    private RegularNewsAdapter regularAdapter;
    private final List<NewsItem> featuredList = new ArrayList<>();
    private final List<NewsItem> newsItemList = new ArrayList<>();
    private final List<NewsItem> allItems = new ArrayList<>();
    private final Map<String, List<NewsItem>> categoryMap = new HashMap<>();
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("events");
    private TextView tabAll, tabSports, tabAcademic, tabEvents;
    private View currentlySelectedTab;

    private Handler sliderHandler = new Handler();
    private Runnable sliderRunnable = () -> {
        int current = featuredViewPager.getCurrentItem();
        featuredViewPager.setCurrentItem(
                current == featuredAdapter.getItemCount()-1 ? 0 : current+1
        );
    };
    private ProgressBar progressBar;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupToolbar(true,false); // From BaseActivity
        initializeViews();
        setupTabListeners();
        loadNewsData();
    }

    @Override
    protected void handleMenuClick() {
        startActivity(new Intent(this, DrawerActivity.class));
        // Optional: Add slide animation
        overridePendingTransition(R.animator.slide_in_left, 0);
    }


    private void initializeViews() {
        featuredViewPager = findViewById(R.id.featuredViewPager);
        dotsIndicator = findViewById(R.id.dotsIndicator);
        newsRecyclerView = findViewById(R.id.newsRecyclerView);
        progressBar = findViewById(R.id.progressBar);


        tabAll = findViewById(R.id.tabAll);
        tabSports = findViewById(R.id.tabSports);
        tabAcademic = findViewById(R.id.tabAcademic);
        tabEvents = findViewById(R.id.tabEvents);

        currentlySelectedTab = tabAll;
        tabAll.setSelected(true);
        tabAll.setAlpha(1.0f);
        tabAll.setPaintFlags(tabAll.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        tabSports.setSelected(false);
        tabSports.setAlpha(0.5f);
        tabSports.setPaintFlags(tabSports.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

        tabAcademic.setSelected(false);
        tabAcademic.setAlpha(0.5f);
        tabAcademic.setPaintFlags(tabAcademic.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

        tabEvents.setSelected(false);
        tabEvents.setAlpha(0.5f);
        tabEvents.setPaintFlags(tabEvents.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));



    }


    private void setupAdapters() {
        // Featured news adapter with click listener
        featuredAdapter = new FeaturedNewsAdapter(this, featuredList, this);
        featuredViewPager.setAdapter(featuredAdapter);
        featuredViewPager.setOffscreenPageLimit(3);
        featuredViewPager.setPageTransformer(new ZoomOutPageTransformer());
        dotsIndicator.setViewPager2(featuredViewPager);

        featuredViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });

        // Regular news adapter with click listener
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        regularAdapter = new RegularNewsAdapter(this, newsItemList, this);
        newsRecyclerView.setAdapter(regularAdapter);
    }


    private void setupTabListeners() {
        setupTabClickListener(tabAll, "All");
        setupTabClickListener(tabSports, "Sports");
        setupTabClickListener(tabAcademic, "Academic");
        setupTabClickListener(tabEvents, "Events");
    }

    private void setupTabClickListener(TextView tabView, String category) {
        tabView.setOnClickListener(v -> {
            if (currentlySelectedTab != null) {
                currentlySelectedTab.setSelected(false);
                ((TextView) currentlySelectedTab).setAlpha(0.5f);
                ((TextView) currentlySelectedTab).setPaintFlags(
                        ((TextView) currentlySelectedTab).getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG)
                );
            }
            tabView.setSelected(true);
            tabView.setAlpha(1.0f);
            tabView.setPaintFlags(tabView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
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
        progressBar.setVisibility(View.VISIBLE);  // Show loading spinner

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

                runOnUiThread(() -> {
                    setupAdapters(); // ✅ Moved here
                    String currentCategory = getCurrentCategory();
                    filterNewsByCategory(currentCategory);

                    featuredAdapter.notifyDataSetChanged();
                    regularAdapter.notifyDataSetChanged();

                    progressBar.setVisibility(View.GONE);  // Hide loading spinner
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase error: " + error.getMessage());
                Toast.makeText(HomeActivity.this,
                        "Failed to load news: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private String getCurrentCategory() {
        if (currentlySelectedTab == tabSports) return "Sports";
        if (currentlySelectedTab == tabAcademic) return "Academic";
        if (currentlySelectedTab == tabEvents) return "Events";
        return "All";
    }

    @Override
    public void onNewsClick(NewsItem newsItem) {
        openNewsDetail(newsItem);
    }

    private void openNewsDetail(NewsItem newsItem) {
        Intent intent = new Intent(this, NewsActivity.class);
        intent.putExtra("news_item", newsItem);
        startActivity(intent);
        overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
    }
}