package com.example.z.user;

import android.content.Intent;
import android.widget.Toast;
import android.content.Context;

import com.example.z.views.LogInActivity;
import com.example.z.views.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Handles user login functionality using Firebase Authentication.
 */
public class LogInController {
    private Context context;
    private FirebaseAuth mAuth;

    /**
     * Constructs a new LogInController.
     *
     * @param context The context from which this controller is being used.
     */
    public LogInController(Context context) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Attempts to log in a user with the provided email and password.
     * If authentication is successful, the user is redirected to the ProfileActivity.
     *
     * @param email    The email address of the user.
     * @param password The password entered by the user.
     */
    public void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show();

                        // Navigate to ProfileActivity upon successful login
                        context.startActivity(new Intent(context, ProfileActivity.class));

                        // Close the LogInActivity to prevent back navigation
                        ((LogInActivity) context).finish();
                    } else {
                        // Display an error message if authentication fails
                        Toast.makeText(context, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

