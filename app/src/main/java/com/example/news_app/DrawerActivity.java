package com.example.news_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class DrawerActivity extends BaseActivity {

    private static final String TAG = "DrawerActivity";
    private ImageView btnBack, btnClose, profileImage;
    private LinearLayout menuHome, menuProfile, menuInfo, btnSignout;
    private TextView profileName;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private AuthPreferences authPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        setupToolbar(false,true);
        initializeViews();

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        // Initialize AuthPreferences
        authPreferences = new AuthPreferences(this);

        fetchUserData();
        setupListeners();
    }

    private void fetchUserData() {
        if (currentUser == null) {
            profileName.setText("Guest");
            profileImage.setImageResource(R.drawable.profile_placeholder);
            return;
        }

        // First try to load username from AuthPreferences
        String savedUsername = authPreferences.getUsername();
        if (savedUsername != null && !savedUsername.isEmpty()) {
            profileName.setText(savedUsername);
        } else {
            profileName.setText("Loading...");
        }

        String uid = currentUser.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Try to get username from multiple possible fields
                    String username = snapshot.child("username").getValue(String.class);
                    String name = snapshot.child("name").getValue(String.class);
                    String displayName = snapshot.child("displayName").getValue(String.class);

                    // Priority: username > name > displayName > email
                    if (username != null && !username.isEmpty()) {
                        profileName.setText(username);
                    } else if (name != null && !name.isEmpty()) {
                        profileName.setText(name);
                    } else if (displayName != null && !displayName.isEmpty()) {
                        profileName.setText(displayName);
                    } else if (currentUser.getEmail() != null) {
                        profileName.setText(currentUser.getEmail());
                        Log.w(TAG, "No username found in database, using email");
                    } else {
                        profileName.setText("Guest");
                        Log.w(TAG, "No username or email found");
                    }

                    // Load profile image using ProfileUtils
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                    ProfileUtils.loadProfileImage(DrawerActivity.this, profileImage, currentUser);

                } else {
                    // User data not found
                    if (currentUser.getEmail() != null) {
                        profileName.setText(currentUser.getEmail());
                    } else {
                        profileName.setText("Guest");
                    }
                    Log.w(TAG, "User data not found in database");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // On error fallback to currentUser email or "Guest"
                if (currentUser.getEmail() != null) {
                    profileName.setText(currentUser.getEmail());
                } else {
                    profileName.setText("Guest");
                }
                profileImage.setImageResource(R.drawable.profile_placeholder);
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        btnClose = findViewById(R.id.btn_close);
        menuHome = findViewById(R.id.menu_home);
        menuProfile = findViewById(R.id.menu_profile);
        menuInfo = findViewById(R.id.menu_info);
        btnSignout = findViewById(R.id.btn_signout);
        profileName = findViewById(R.id.profile_name);
        profileImage = findViewById(R.id.profile_image);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
                overridePendingTransition(0, R.animator.slide_out_left);
            }
        });

        btnClose.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, R.animator.slide_out_left);
        });

        menuHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        menuProfile.setOnClickListener(v -> {
            MyProfileFragment fragment = new MyProfileFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left,
                            R.animator.slide_in_left, R.animator.slide_out_right)
                    .replace(R.id.drawer_content_frame, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        menuInfo.setOnClickListener(v -> {
            DevFragment fragment = new DevFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left,
                            R.animator.slide_in_left, R.animator.slide_out_right)
                    .replace(R.id.drawer_content_frame, fragment)
                    .addToBackStack(null)
                    .commit();
        });


        btnSignout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            authPreferences.clearLoginState();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finishAffinity(); // Clear all activities
        });
    }

}
