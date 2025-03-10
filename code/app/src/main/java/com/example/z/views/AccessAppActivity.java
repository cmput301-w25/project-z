package com.example.z.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.z.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Used initially for testing, now replaced by LogInActivity and SignUpActivity.
 * Not used for app functionality but was required for initial testing.
 * Handles user authentication, including sign-up and login.
 * Users can register with an email, username, and password, or log in with existing credentials.
 *
 *  Outstanding Issues
 *      - None
 */
public class AccessAppActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText usernameInput;
    private FirebaseFirestore db;

    /**
     * Called when the activity is created.
     * Initializes Firebase authentication and UI elements.
     *
     * @param savedInstanceState The saved state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailInput = findViewById(R.id.etEmail);
        passwordInput = findViewById(R.id.etPassword);
        usernameInput = findViewById(R.id.etUsername);

        // Set up the sign-up button
        Button signupButton = findViewById(R.id.btnSignup);
        signupButton.setOnClickListener(v -> {
            emailInput.setVisibility(View.VISIBLE); // Ensure email field is visible for signup
            signupUser();
        });
    }

    /**
     * Logs in a user with their username and password.
     * Ensures that the fields are not empty before proceeding.
     */
    private void loginUser() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter a username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        dbLogin(username, password);
    }

    /**
     * Performs Firebase authentication login using email and password.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     */
    private void dbLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("Login", "Login successful for email: " + email);
                        grant_access();
                    } else {
                        Log.e("Login", "Login failed: " + task.getException().getMessage());
                        Toast.makeText(AccessAppActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Registers a new user with Firebase Authentication and Firestore.
     * Ensures all fields are filled before proceeding.
     */
    private void signupUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("Signup", "Signup successful for email: " + email);
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), email, username);
                        }
                        Toast.makeText(AccessAppActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                        grant_access();
                    } else {
                        Log.e("Signup", "Signup failed: " + task.getException().getMessage());
                        Toast.makeText(AccessAppActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Saves the new user's information (email and username) to Firestore.
     *
     * @param userId   The unique ID of the Firebase user.
     * @param email    The user's email address.
     * @param username The user's chosen username.
     */
    private void saveUserToFirestore(String userId, String email, String username) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("username", username);

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "User data saved to Firestore"))
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to save user data: " + e.getMessage()));
    }

    /**
     * Redirects the user to the FollowingActivity after successful login or sign-up.
     */
    private void grant_access() {
        Intent intent = new Intent(this, FollowingActivity.class);
        startActivity(intent);
        finish();
    }
}


