package com.example.z;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * SignUpController handles the logic for user registration.
 * It interacts with Firebase Authentication and Firestore to create new user accounts
 * and save user profiles.
 *
 * Outstanding Issues:
 * - No retry mechanism for Firestore or Firebase Authentication failures.
 */
public class SignUpController {
    private Context context;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    /**
     * Constructor for SignUpController.
     *
     * @param context The context of the calling activity.
     */
    public SignUpController(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseFirestore.setLoggingEnabled(true); // Enable Firestore logging
    }

    /**
     * Registers a new user with the provided email, username, and password.
     * @param email
     *      The user's email address.
     * @param username
     *      The desired username.
     * @param password
     *      The user's password.
     */
    public void signUpUser(String email, String username, String password, accessCallback callback) {
        // Check if username is unique
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().isEmpty()) {
                        // Username is unique, proceed with Firebase Authentication
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(authTask -> {
                                    if (authTask.isSuccessful()) {
                                        // Save user profile to Firestore
                                        saveUserProfile(email, username);
                                        callback.onAccessResult(true, "SignUp successful!");
                                    } else {
                                        // Get the detailed error message
                                        String errorMessage = authTask.getException() != null ? authTask.getException().getMessage() : "Unknown error";

                                        // Log and show the exact Firebase error
                                        Log.e("FirebaseAuthError", errorMessage);
                                        Toast.makeText(context, "Firebase Authentication failed: " + errorMessage, Toast.LENGTH_LONG).show();

                                    }
                                });
                    } else {
                        // Provide feedback when username already exists
                        callback.onAccessResult(false, "SignUp failed");
                    }
                } else {
                    // Provide feedback
                    Toast.makeText(context, "Error checking username", Toast.LENGTH_SHORT).show();
                }
            });
    }

    /**
     * Saves the user's profile to Firestore.
     * @param email
     *      The user's email address.
     * @param username
     *      The user's username.
     */
    private void saveUserProfile(String email, String username) {

        if (mAuth.getCurrentUser() == null) {
            // Provide feedback
            Toast.makeText(context, "User not authenticated. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Create a User object
        User user = new User(email, username);

        // Save to Firestore
        db.collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener(aVoid -> {
                // Provide feedback
                Toast.makeText(context, "Profile saved successfully!", Toast.LENGTH_SHORT).show();

                // redirect to profile activity
                Intent intent = new Intent(context, ProfileActivity.class);
                context.startActivity(intent);
                ((SignUpActivity) context).finish(); // Close the SignUpActivity
            })
            .addOnFailureListener(e -> {
                // Provide feedback
                Toast.makeText(context, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
