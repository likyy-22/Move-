package com.example.motion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerLink;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI components
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        registerLink = findViewById(R.id.register_link);
        progressBar = findViewById(R.id.progress_bar);
        ImageView gifImageView = findViewById(R.id.gif_image);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Load GIF using Glide
        Glide.with(this)
                .asGif()
                .load(R.drawable.login)  // Ensure your gif is named login.gif in res/drawable
                .into(gifImageView);

        // Register link click listener
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Login button click listener
        loginButton.setOnClickListener(v -> loginUser());
    }

    // Method to handle login
    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Input validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPassword(password)) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress bar and disable login button
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        // Sign in the user
        signInUser(email, password);
    }

    // Method to check email format
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Method to check password length
    private boolean isValidPassword(String password) {
        return password.length() >= 6;
    }

    // Method to sign in using Firebase Auth
    private void signInUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    // Hide progress bar and enable login button
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        // Navigate to the next activity
                        Intent intent = new Intent(LoginActivity.this, NextActivity.class);
                        startActivity(intent);
                        finish(); // Finish LoginActivity
                    } else {
                        String errorMessage = "Login failed";
                        if (task.getException() != null) {
                            errorMessage = getErrorMessage(task.getException());
                        }
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to provide custom error messages
    private String getErrorMessage(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            return "No account found with this email.";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return "Incorrect password.";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return "Invalid email format.";
        }
        return "Login failed. Please try again.";
    }
}
