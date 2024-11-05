package com.example.motion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerificationActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button checkVerificationButton;
    private ImageView staticImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        auth = FirebaseAuth.getInstance();
        checkVerificationButton = findViewById(R.id.check_verification_button);
        staticImageView = findViewById(R.id.static_image_view);

        // Load the GIF into the ImageView using Glide
        Glide.with(this)
                .asGif()
                .load(R.drawable.email) // Replace with your GIF file in res/drawable
                .into(staticImageView);

        checkVerificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkEmailVerification();
            }
        });
    }

    private void checkEmailVerification() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (user.isEmailVerified()) {
                        // Proceed to the next activity
                        startActivity(new Intent(VerificationActivity.this, NextActivity.class)); // Change to your next activity
                        finish();
                    } else {
                        Toast.makeText(VerificationActivity.this, "Email not verified. Please check your email.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(VerificationActivity.this, "Error checking verification status.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
