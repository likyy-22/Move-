package com.example.motion;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private TextView userEmailTextView;
    private ImageView profilePictureImageView;
    private Button editPictureButton;
    private Button logoutButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // Constants for image selection
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize views
        userNameTextView = findViewById(R.id.user_name);
        userEmailTextView = findViewById(R.id.user_email);
        profilePictureImageView = findViewById(R.id.profile_picture);
        editPictureButton = findViewById(R.id.edit_picture_button);
        logoutButton = findViewById(R.id.logout_button);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get the current user
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            loadUserProfile(userId);
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        // Edit Picture
        editPictureButton.setOnClickListener(v -> requestStoragePermission());

        // Logout
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(UserProfileActivity.this, MainActivity.class));
            finish();
        });
    }

    private void loadUserProfile(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String firstName = document.getString("firstName");
                            String lastName = document.getString("lastName");
                            String userEmail = document.getString("email");

                            String fullName = firstName + " " + lastName;
                            userNameTextView.setText(fullName);
                            userEmailTextView.setText(userEmail);

                            String profileImagePath = document.getString("profileImage");
                            if (profileImagePath != null && !profileImagePath.isEmpty()) {
                                Glide.with(this).load(new File(profileImagePath)).into(profilePictureImageView);
                            } else {
                                profilePictureImageView.setImageResource(R.drawable.profile);
                            }
                        }
                    } else {
                        Log.e("UserProfile", "Failed to load user data");
                    }
                });
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            } else {
                openGallery();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            profilePictureImageView.setImageURI(imageUri);
            saveImageLocally(imageUri);
        }
    }

    private void saveImageLocally(Uri imageUri) {
        if (imageUri != null) {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                try {
                    // Get the input stream from the URI
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    // Create a directory to save the image
                    File directory = new File(getFilesDir(), "profile_images");
                    if (!directory.exists()) {
                        directory.mkdirs(); // Create the directory if it doesn't exist
                    }

                    // Create a file in the directory
                    File file = new File(directory, "profile_" + userId + ".jpg");
                    // Write the input stream to the file
                    FileOutputStream outputStream = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.close();
                    inputStream.close();

                    // Save the file path to Firestore
                    storeProfileImagePathInFirestore(file.getAbsolutePath());

                    // Optionally load the image into the ImageView
                    Glide.with(this).load(file).into(profilePictureImageView);

                } catch (IOException e) {
                    Log.e("UserProfile", "Error saving image", e);
                }
            }
        }
    }

    private void storeProfileImagePathInFirestore(String filePath) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId)
                    .update("profileImage", filePath)
                    .addOnSuccessListener(aVoid -> Log.d("UserProfile", "Profile picture path saved"))
                    .addOnFailureListener(e -> Log.e("UserProfile", "Error saving path to Firestore", e));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Log.e("UserProfile", "Permission denied");
            }
        }
    }
}
