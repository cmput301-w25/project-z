package com.example.z;

import android.content.Intent;
import android.widget.Toast;
import android.content.Context;
import com.google.firebase.auth.FirebaseAuth;

/**
 * LogInController handles the logic for user authentication.
 * It interacts with Firebase Authentication to log in users.
 *
 * Outstanding Issues:
 * - No error handling for network issues.
 */
public class LogInController {
    private Context context;
    private FirebaseAuth mAuth;

    /**
     * Constructor for LogInController.
     * @param context
     *      The context of the calling activity.
     */
    public LogInController(Context context) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Authenticates a user with the provided email and password.
     * @param email
     *      The user's email address.
     * @param password
     *      The user's password.
     */
    public void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Provide feedback
                    Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show();
                    context.startActivity(new Intent(context, ProfileActivity.class));
                    ((LogInActivity) context).finish(); // Close the LogInActivity
                } else {
                    // Provide feedback
                    Toast.makeText(context, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
}
