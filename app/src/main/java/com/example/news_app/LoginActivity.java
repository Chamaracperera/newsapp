package com.example.news_app;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;
    private CheckBox rememberMeCheckBox;
    private TextView forgotPasswordText, signupText;
    private Button loginBtn;

    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;
    private AuthPreferences authPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        authPreferences = new AuthPreferences(this);

        // Replace the direct check with this:
        if (authPreferences.shouldAutoLogin()) {
            authPreferences.attemptAutoLogin(this, new AuthPreferences.AuthCallback() {
                @Override
                public void onSuccess() {
                    navigateToHome();
                }

                @Override
                public void onFailure(String errorMessage) {  // Add String parameter
                    Toast.makeText(LoginActivity.this,
                            "Auto-login failed: " + errorMessage,
                            Toast.LENGTH_SHORT).show();
                    proceedToLoginUI();
                }
            });
        } else {
            proceedToLoginUI();
        }
    }

    private void proceedToLoginUI() {
        setContentView(R.layout.activity_login);

        // Initialize views
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        rememberMeCheckBox = findViewById(R.id.rememberMe);
        forgotPasswordText = findViewById(R.id.forgotPassword);
        loginBtn = findViewById(R.id.loginBtn);
        signupText = findViewById(R.id.signupText);

        // Restore saved username if exists
        String savedUsername = authPreferences.getUsername();
        if (!savedUsername.isEmpty()) {
            usernameEditText.setText(savedUsername);
            rememberMeCheckBox.setChecked(true);
        }

        // Toggle password visibility
        passwordEditText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordEditText.getRight() -
                        passwordEditText.getCompoundDrawables()[DRAWABLE_END].getBounds().width())) {
                    int cursorPosition = passwordEditText.getSelectionEnd();

                    if (isPasswordVisible) {
                        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.invisible, 0);
                        isPasswordVisible = false;
                    } else {
                        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visible, 0);
                        isPasswordVisible = true;
                    }
                    passwordEditText.setSelection(cursorPosition);
                    return true;
                }
            }
            return false;
        });

        // ✅ Login button click logic
        loginBtn.setOnClickListener(view -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
            usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String email = userSnapshot.child("email").getValue(String.class);

                            mAuth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // Save login state if "Remember Me" is checked
                                            if (rememberMeCheckBox.isChecked()) {
                                                authPreferences.saveLoginState(true, username, email, true); // 4th param = rememberMe
                                            } else {
                                                authPreferences.clearLoginState();
                                            }
                                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                            navigateToHome();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                            break;
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(LoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        // ✅ Forgot password click
        forgotPasswordText.setOnClickListener(v ->
                Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show());

        // ✅ "New user? Sign up" click
        signupText.setOnClickListener(v -> {
            signupText.setPaintFlags(signupText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    private void navigateToHome() {
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear if not remembered
        if (!rememberMeCheckBox.isChecked()) {
            authPreferences.clearLoginState();
        }
    }
}
