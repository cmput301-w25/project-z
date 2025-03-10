package com.example.z.user;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.z.views.ProfileActivity;
import com.example.z.views.SignUpActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Handles user sign-up logic, including authentication and Firestore user profile storage.
 */
public class SignUpController {
    private Context context;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    /**
     * Constructs a new SignUpController.
     *
     * @param context The context from which this controller is being used.
     */
    public SignUpController(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseFirestore.setLoggingEnabled(true); // Enable Firestore logging
    }

    /**
     * Attempts to sign up a new user with the provided credentials.
     * Checks if the username is unique before proceeding with Firebase authentication.
     *
     * @param email    The email address of the user.
     * @param username The desired username of the user.
     * @param password The password chosen by the user.
     */
    public void signUpUser(String email, String username, String password) {
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
                                            saveUserProfile(email, username);
                                        } else {
                                            Toast.makeText(context, "Firestore Authentication failed.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(context, "Username already exists", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Error checking username", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Saves the authenticated user's profile information to Firestore.
     *
     * @param email    The email address of the user.
     * @param username The username of the user.
     */
    private void saveUserProfile(String email, String username) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(context, "User not authenticated. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        User user = new User(email, username);

        db.collection("users")
                .document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Profile saved successfully!", Toast.LENGTH_SHORT).show();

                    // Redirect to ProfileActivity
                    Intent intent = new Intent(context, ProfileActivity.class);
                    context.startActivity(intent);
                    ((SignUpActivity) context).finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

