package com.example.news_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyProfileFragment extends Fragment {

    private EditText tvUsername, tvEmail, passwordText;
    private Button saveButton;
    private AuthPreferences authPreferences;
    private boolean isPasswordVisible = false;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String originalUsername, originalEmail, originalPassword;
    private ImageView profileImage;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private String imgbbApiKey = "54c5c75d66f781cf78c2bd81e0e7796e"; // Replace this with your key



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize AuthPreferences
        authPreferences = new AuthPreferences(requireContext());

        // Bind views
        ImageView btnCamera = view.findViewById(R.id.btn_camera);
        tvUsername = view.findViewById(R.id.tv_username);
        tvEmail = view.findViewById(R.id.tv_email);
        passwordText = view.findViewById(R.id.passwordText);
        saveButton = view.findViewById(R.id.saveButton);
        profileImage = view.findViewById(R.id.profile_image); // Initialize profile image view

        // Get original values
        originalUsername = authPreferences.getUsername();
        originalEmail = authPreferences.getEmail();
        originalPassword = authPreferences.getPassword();
        ProfileUtils.loadProfileImage(requireContext(), profileImage, mAuth.getCurrentUser());

        // Set initial values
        tvUsername.setText(originalUsername.isEmpty() ? "" : originalUsername);
        tvEmail.setText(originalEmail.isEmpty() ? "" : originalEmail);
        passwordText.setText(originalPassword.isEmpty() ? "" : originalPassword);

        // Password toggle functionality
        setupPasswordToggle();

        // Save button click listener
        saveButton.setOnClickListener(v -> updateUserProfile());
        btnCamera.setOnClickListener(v -> openFileChooser());
    }

    private void setupPasswordToggle() {
        passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        passwordText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2;
            if (event.getAction() == MotionEvent.ACTION_UP &&
                    passwordText.getCompoundDrawables()[DRAWABLE_END] != null &&
                    event.getRawX() >= (passwordText.getRight() -
                            passwordText.getCompoundDrawables()[DRAWABLE_END].getBounds().width())) {

                int cursorPosition = passwordText.getSelectionEnd();

                if (isPasswordVisible) {
                    passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.invisible, 0);
                } else {
                    passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    passwordText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visible, 0);
                }

                passwordText.setSelection(cursorPosition);
                isPasswordVisible = !isPasswordVisible;
                return true;
            }
            return false;
        });
    }

    private void updateUserProfile() {
        String newUsername = tvUsername.getText().toString().trim();
        String newEmail = tvEmail.getText().toString().trim();
        String newPassword = passwordText.getText().toString().trim();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasChanges = false;

        // Check for changes and update Firebase
        if (!newUsername.equals(originalUsername)) {
            databaseReference.child(user.getUid()).child("username").setValue(newUsername);
            authPreferences.saveLoginState(true, newUsername, originalEmail, authPreferences.isRememberMeEnabled());
            hasChanges = true;

            // Update username reference if username changed
            DatabaseReference usernamesRef = FirebaseDatabase.getInstance().getReference("usernames");
            usernamesRef.child(originalUsername.toLowerCase()).removeValue();
            usernamesRef.child(newUsername.toLowerCase()).setValue(user.getUid());
        }

        if (!newEmail.equals(originalEmail)) {
            // Update email in Firebase Auth first
            user.updateEmail(newEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            databaseReference.child(user.getUid()).child("email").setValue(newEmail);
                            authPreferences.saveLoginState(true, originalUsername, newEmail, authPreferences.isRememberMeEnabled());
                            Toast.makeText(getContext(), "Email updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Email update failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            hasChanges = true;
        }

        if (!newPassword.equals(originalPassword) && !newPassword.isEmpty()) {
            // Update password in Firebase Auth
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            authPreferences.savePassword(newPassword);
                            Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Password update failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            hasChanges = true;
        }

        if (!hasChanges) {
            Toast.makeText(getContext(), "No changes detected", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
        }
    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK
                && data != null && data.getData() != null) {

            selectedImageUri = data.getData();

            // Optional: Show selected image
            profileImage.setImageURI(selectedImageUri);

            // Upload to imgbb
            uploadImageToImgbb(selectedImageUri);
        }
    }
    private void uploadImageToImgbb(Uri imageUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            byte[] imageBytes = getBytes(inputStream);
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("key", imgbbApiKey)
                    .add("image", encodedImage)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload")
                    .post(formBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Upload failed: " + response.message(), Toast.LENGTH_SHORT).show());
                        return;
                    }

                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String imageUrl = jsonObject.getJSONObject("data").getString("url");

                        // Save URL to Firebase Database
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            databaseReference.child(user.getUid()).child("imageUrl").setValue(imageUrl);
                        }

                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Profile image updated!", Toast.LENGTH_SHORT).show();
                            Glide.with(requireContext())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.profile_placeholder)
                                    .into(profileImage);
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error uploading image", Toast.LENGTH_SHORT).show();
        }
    }
    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }


}