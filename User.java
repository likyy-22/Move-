package com.example.motion;

public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String gender;
    private String userId;
    private String profileImage;

    // Default constructor required for Firestore
    public User() {
    }

    // Constructor for creating user with all fields
    public User(String firstName, String lastName, String email, String mobile, String gender, String userId, String profileImage) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobile = mobile;
        this.gender = gender;
        this.userId = userId;
        this.profileImage = profileImage;
    }

    // Optional constructor without profile image
    public User(String firstName, String lastName, String email, String mobile, String gender, String userId) {
        this(firstName, lastName, email, mobile, gender, userId, ""); // Default profile image as empty string
    }

    // Getters and setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
