package com.example.z;

import android.content.Intent;
import android.os.Bundle;
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
            if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                // Provide feedback
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Delegate sign-up logic to SignUpController
            signUpController.signUpUser(email, username, password);
        });

        // Set up login text click listener
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
            startActivity(intent);
        });
    }
}
