package com.example.news_app;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileUtils {
    public static void loadProfileImage(Context context, ImageView profileImage, FirebaseUser currentUser) {
        if (currentUser == null) {
            profileImage.setImageResource(R.drawable.profile_placeholder);
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String imageUrl = snapshot.child("imageUrl").exists()
                        ? snapshot.child("imageUrl").getValue(String.class)
                        : null;

                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.profile_placeholder)
                        .error(R.drawable.profile_placeholder)
                        .into(profileImage);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                profileImage.setImageResource(R.drawable.profile_placeholder);
            }
        });
    }
}