package com.example.z.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.z.R;
import com.example.z.user.LogInController;

/**
 * LogInActivity handles user authentication.
 * It provides a UI for users to input their email and password.
 * It delegates the login logic to the LogInController.
 *
 *  Outstanding Issues:
 *      - Maybe more input validation and feedback required for minor edge cases
 */
public class LogInActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private LogInController logInController;

    private Button btnSignUp;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);

        context = this;
        logInController = new LogInController(this);

        // Set up login button click listener
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validate inputs
            if (email.isEmpty()) {
                // Provide feedback
                etEmail.setError("Email cannot be empty.");
            }
            // Validate inputs
            if (password.isEmpty()) {
                // Provide feedback
                etPassword.setError("Password cannot be empty.");
            }

            if (password.isEmpty() || email.isEmpty()) {
                return;
            }

            logInController.loginUser(email, password, (isSuccess, message) -> {
                if (isSuccess) {
                    // Login successful
                    startActivity(new Intent(LogInActivity.this, ProfileActivity.class));
                    finish(); // Close the LogInActivity
                }
                else {
                    etPassword.setError("Wrong password and/or email.");
                    etEmail.setError("Wrong password and/or email.");
                }
            });

        });

        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

    }
}


