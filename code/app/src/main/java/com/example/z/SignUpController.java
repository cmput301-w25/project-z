package com.example.z;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpController {
    private Context context;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public SignUpController(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseFirestore.setLoggingEnabled(true); // Enable Firestore logging
    }

    public void signUpUser(String email, String username, String password) {
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
                                    } else {
                                        Toast.makeText(context, "Firestore Authentication failed. ", Toast.LENGTH_SHORT).show();
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

    private void saveUserProfile(String email, String username) {

        if (mAuth.getCurrentUser() == null) {
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
                // Show success Toast
                Toast.makeText(context, "Profile saved successfully!", Toast.LENGTH_SHORT).show();

                // redirect to profile activity
                Intent intent = new Intent(context, ProfileActivity.class);
                context.startActivity(intent);
                ((SignUpActivity) context).finish(); // Close the SignUpActivity
            })
            .addOnFailureListener(e -> {
                // Show failure Toast
                Toast.makeText(context, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
