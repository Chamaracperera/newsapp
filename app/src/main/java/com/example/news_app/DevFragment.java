package com.example.news_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DevFragment extends Fragment {

    private ImageView btnBack, btnClose, devImage;
    private TextView tvInfo, tvStatus, tvVersion, tvDevelopedDate;

    private DatabaseReference devInfoRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dev, container, false);

        // Initialize views

        devImage = view.findViewById(R.id.dev_image);

        tvInfo = view.findViewById(R.id.tv_info);
        tvStatus = view.findViewById(R.id.tv_status);
        tvVersion = view.findViewById(R.id.tv_version);
        tvDevelopedDate = view.findViewById(R.id.tv_developed_date);


        // Firebase Database Reference
        devInfoRef = FirebaseDatabase.getInstance().getReference("developerInfo");

        // Load data
        loadDeveloperInfo();

        return view;
    }

    private void loadDeveloperInfo() {
        devInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String info = snapshot.child("info").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    String version = snapshot.child("version").getValue(String.class);
                    String developedDate = snapshot.child("developedDate").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    // Handle \n for new lines in info
                    if (info != null) {
                        tvInfo.setText(info.replace("\\n", "\n"));
                    }

                    tvStatus.setText(status);
                    tvVersion.setText("Version : " + version);
                    tvDevelopedDate.setText("Developed: " + developedDate);

                    Glide.with(requireContext())
                            .load(profileImageUrl)
                            .placeholder(R.drawable.dev)
                            .into(devImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });
    }
}
