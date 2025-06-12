package com.example.news_app;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;
    private CheckBox rememberMeCheckBox;
    private TextView forgotPasswordText, signupText;
    private Button loginBtn;

    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;
    private AuthPreferences authPreferences;


    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        makeStatusBarTransparent();

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
        // Add this to your LoginActivity's onCreate() method
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }
    protected void makeStatusBarTransparent() {
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
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

        ImageView googleSignIn = findViewById(R.id.googleSignIn);
        ImageView facebookSignIn = findViewById(R.id.facebookSignIn);
        ImageView linkedinSignIn = findViewById(R.id.linkedinSignIn);

        facebookSignIn.setOnClickListener(v ->
                Toast.makeText(this, "Facebook login is not available right now.", Toast.LENGTH_SHORT).show());

        linkedinSignIn.setOnClickListener(v ->
                Toast.makeText(this, "LinkedIn login is not available right now.", Toast.LENGTH_SHORT).show());


        googleSignIn.setOnClickListener(v -> signInWithGoogle());


        // Restore saved username if exists
        String savedUsername = authPreferences.getUsername();
        if (!savedUsername.isEmpty()) {
            usernameEditText.setText(savedUsername);
            rememberMeCheckBox.setChecked(true);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


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

            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
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
                                                authPreferences.saveLoginState(true, username, email, true);
                                                authPreferences.savePassword(password);
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
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Save user data to Realtime Database if needed
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                        User newUser = new User(user.getDisplayName(), user.getEmail());
                        databaseReference.child(user.getUid()).setValue(newUser)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this,
                                                "Google sign-in successful!",
                                                Toast.LENGTH_SHORT).show();
                                        navigateToHome();
                                    } else {
                                        Toast.makeText(LoginActivity.this,
                                                "Failed to save user data",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }



}
