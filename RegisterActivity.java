package com.example.motion;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText firstName, lastName, email, mobileNumber, passwordEditText;
    private RadioGroup genderRadioGroup;
    private CheckBox termsCheckbox;
    private TextView termsLink;
    private Button registerButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // Sample terms and conditions
    private String[] termsList = {
            "1. Terms and conditions apply. Please check our policies.",
            "2. You must be at least 18 years of age to book a room.",
            "3. Cancellations must be made 24 hours in advance.",
            "4. No pets allowed in the hotel premises.",
            "5. Guests must provide a valid ID at check-in."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize UI components
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        email = findViewById(R.id.email);
        mobileNumber = findViewById(R.id.mobile_number);
        passwordEditText = findViewById(R.id.password);
        genderRadioGroup = findViewById(R.id.gender_radio_group);
        termsCheckbox = findViewById(R.id.terms_checkbox);
        termsLink = findViewById(R.id.terms_link);
        registerButton = findViewById(R.id.register_button);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        termsLink.setOnClickListener(v -> showTermsDialog()); // Show terms dialog

        registerButton.setOnClickListener(v -> validateAndRegister());
    }

    private void showTermsDialog() {
        // Combine all terms into a single string
        StringBuilder termsBuilder = new StringBuilder();
        for (String term : termsList) {
            termsBuilder.append(term).append("\n"); // Append each term with a newline
        }

        // Create and show the AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("Terms and Conditions")
                .setMessage(termsBuilder.toString()) // Set the full terms text
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void validateAndRegister() {
        String emailInput = email.getText().toString().trim();
        String mobile = mobileNumber.getText().toString().trim();
        String passwordInput = passwordEditText.getText().toString().trim();

        if (firstName.getText().toString().isEmpty() ||
                lastName.getText().toString().isEmpty() ||
                emailInput.isEmpty() ||
                mobile.isEmpty() ||
                passwordInput.isEmpty() ||
                genderRadioGroup.getCheckedRadioButtonId() == -1 ||
                !termsCheckbox.isChecked()) {

            Toast.makeText(this, "Please fill all fields and accept the terms.", Toast.LENGTH_SHORT).show();
        } else {
            checkUserExists(emailInput);
        }
    }

    private void checkUserExists(String email) {
        auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().getSignInMethods() != null && !task.getResult().getSignInMethods().isEmpty()) {
                            Toast.makeText(RegisterActivity.this, "User already exists, please log in.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        } else {
                            registerUser(email);
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Error checking user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("RegisterActivity", "Error checking user: ", task.getException());
                    }
                });
    }

    private void registerUser(String email) {
        String passwordInput = passwordEditText.getText().toString().trim();
        auth.createUserWithEmailAndPassword(email, passwordInput)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            sendEmailVerification(user);

                            String firstNameInput = firstName.getText().toString().trim();
                            String lastNameInput = lastName.getText().toString().trim();
                            String mobile = mobileNumber.getText().toString().trim();
                            int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
                            String gender = selectedGenderId == R.id.radio_male ? "M" : "F";

                            User userObj = new User(firstNameInput, lastNameInput, email, mobile, gender, user.getUid());
                            userObj.setProfileImage("");

                            db.collection("users").document(user.getUid())
                                    .set(userObj)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, "User registered successfully.", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, VerificationActivity.class);
                                        intent.putExtra("userId", user.getUid());
                                        intent.putExtra("email", email);
                                        intent.putExtra("firstName", firstNameInput);
                                        intent.putExtra("lastName", lastNameInput);
                                        intent.putExtra("mobile", mobile);
                                        intent.putExtra("gender", gender);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterActivity.this, "Failed to register user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e("RegisterActivity", "Firestore error: ", e);
                                    });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("RegisterActivity", "Registration error: ", task.getException());
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
