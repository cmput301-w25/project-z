package com.example.z;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {
    private EditText etEmail, etUsername, etPassword;
    private Button btnSignup;

    private TextView tvLogin;
    private SignUpController signUpController;

    /**
     * SignUpActivity is responsible for handling user registration.
     * It provides a UI for users to input their email, username, and password.
     * It delegates the sign-up logic to the SignUpController.
     *
     * Outstanding Issues:
     * - No input validation for email format.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Link UI elements
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSignup = findViewById(R.id.btnSignup);
        tvLogin = findViewById(R.id.tvLogin);

        // Initialize SignUpController
        signUpController = new SignUpController(this);

        // Set up sign-up button click listener
        btnSignup.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validate inputs
            if (email.isEmpty()) {
                etEmail.setError("Email cannot be empty.");
            }
            if (username.isEmpty()) {
                etUsername.setError("Username cannot be empty.");
            }
            if (password.isEmpty()) {
                etPassword.setError("Password cannot be empty.");
            }
            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                return;
            }

            // Delegate sign-up logic to SignUpController
            signUpController.signUpUser(email, username, password, (isSuccess, message) -> {
                if (isSuccess) {
                    // Signup Successful
                    startActivity(new Intent(SignUpActivity.this, ProfileActivity.class));
                    finish(); // Close the LogInActivity
                } else {
                    // Username already exists
                    Log.d("SignUpActivity", "Sign-up failed: " + message);
                    etUsername.setError("Username already exists");
                }
            });
        });

        // Set up login text click listener
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
            startActivity(intent);
        });
    }
}
