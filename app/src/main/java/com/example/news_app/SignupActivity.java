package com.example.news_app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button signupButton;
    private TextView loginText;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        // Initialize views
        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        signupButton = findViewById(R.id.signupBtn);
        loginText = findViewById(R.id.loginText);

        // Password visibility toggle for "Password" field
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

// Password visibility toggle for "Confirm Password" field
        confirmPasswordEditText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (confirmPasswordEditText.getRight() -
                        confirmPasswordEditText.getCompoundDrawables()[DRAWABLE_END].getBounds().width())) {
                    int cursorPosition = confirmPasswordEditText.getSelectionEnd();

                    if (isConfirmPasswordVisible) {
                        confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        confirmPasswordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.invisible, 0);
                        isConfirmPasswordVisible = false;
                    } else {
                        confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        confirmPasswordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visible, 0);
                        isConfirmPasswordVisible = true;
                    }
                    confirmPasswordEditText.setSelection(cursorPosition);
                    return true;
                }
            }
            return false;
        });


        // Set click listeners
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSignup();
            }
        });

        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Navigate to LoginActivity
                Toast.makeText(SignupActivity.this, "Go to LoginActivity", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSignup() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement actual signup logic (e.g., send to server)
        Toast.makeText(this, "Signup successful (not implemented)", Toast.LENGTH_SHORT).show();
    }
}
