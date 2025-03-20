package com.example.z.user;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import android.content.Context;

import com.example.z.utils.AccessCallBack;
import com.example.z.views.LogInActivity;
import com.example.z.views.ProfileActivity;
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
    public void loginUser(String email, String password, AccessCallBack callback) {
        Log.d("LoginController", "Starting login attempt");
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("LoginController", "Firebase auth successful");
                        Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show();
                        callback.onAccessResult(true, "Login successful!");
                    } else {
                        Log.d("LoginController", "Firebase auth failed: " + task.getException().getMessage());
                        Toast.makeText(context, "Wrong email and/or password!", Toast.LENGTH_SHORT).show();
                        callback.onAccessResult(false, "Login failed!");
                    }
                });
    }
}

