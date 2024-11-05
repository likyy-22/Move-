package com.example.motion;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button addUserButton;
    private Button removeUserButton;
    private Button listUsersButton;
    private TextView userListTextView;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI components
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        addUserButton = findViewById(R.id.add_user_button);
        removeUserButton = findViewById(R.id.remove_user_button);
        listUsersButton = findViewById(R.id.list_users_button);
        userListTextView = findViewById(R.id.user_list_text_view);

        // Set click listeners
        addUserButton.setOnClickListener(v -> addUser());
        removeUserButton.setOnClickListener(v -> removeUser());
        listUsersButton.setOnClickListener(v -> listUsers());
    }

    private void addUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user with email and password
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get the newly created user's ID
                        FirebaseUser newUser = task.getResult().getUser();
                        if (newUser != null) {
                            // Save user details in Firestore 'users' collection
                            User user = new User(
                                    email,
                                    newUser.getUid(),
                                    "First Name Placeholder", // Placeholder for actual data
                                    "Last Name Placeholder",  // Placeholder for actual data
                                    "+0000000000",            // Placeholder for mobile number
                                    "m"                        // Placeholder for gender
                            );
                            firestore.collection("users").document(newUser.getUid())
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "User added successfully.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to add user to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Failed to add user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeUser() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Email cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find and delete the user from Firestore
        firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Delete user document from Firestore
                            firestore.collection("users").document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "User removed successfully from Firestore.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to remove user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(this, "User not found in Firestore.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void listUsers() {
        // Retrieve all users from the Firestore 'users' collection
        firestore.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        StringBuilder userList = new StringBuilder();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Retrieve all user details from the document
                            String email = document.getString("email");
                            String firstName = document.getString("firstName");
                            String lastName = document.getString("lastName");
                            String mobile = document.getString("mobile");
                            String userId = document.getString("userId");
                            String gender = document.getString("gender"); // Change here to get gender as "m" or "f"

                            // Append user details to the StringBuilder
                            userList.append("Email: ").append(email).append("\n")
                                    .append("First Name: ").append(firstName).append("\n")
                                    .append("Last Name: ").append(lastName).append("\n")
                                    .append("Mobile: ").append(mobile).append("\n")
                                    .append("User ID: ").append(userId).append("\n")
                                    .append("Gender: ").append(gender).append("\n\n"); // Display gender as "m" or "f"
                        }
                        // Display the user details in the TextView
                        userListTextView.setText(userList.toString());
                    } else {
                        userListTextView.setText("No users found.");
                        Toast.makeText(this, "No users found in Firestore.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // A simple User class for storing user data in Firestore
    public static class User {
        private String email;
        private String userId;
        private String firstName;
        private String lastName;
        private String mobile;
        private String gender; // Change here to store gender as "m" or "f"

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String email, String userId, String firstName, String lastName, String mobile, String gender) {
            this.email = email;
            this.userId = userId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.mobile = mobile;
            this.gender = gender; // Change here to store gender as "m" or "f"
        }

        public String getEmail() {
            return email;
        }

        public String getUserId() {
            return userId;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getMobile() {
            return mobile;
        }

        public String getGender() {
            return gender; // Change here to return gender as "m" or "f"
        }
    }
}
