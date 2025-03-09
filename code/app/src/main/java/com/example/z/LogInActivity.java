package com.example.z;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * LogInActivity handles user authentication.
 * It provides a UI for users to input their email and password.
 * It delegates the login logic to the LogInController.
 *
 * Outstanding Issues:
 * - No "Forgot Password" functionality.
 * - No input validation for email format.
 */
public class LogInActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private LogInController logInController;

    private Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);

        logInController = new LogInController(this);

        // Set up login button click listener
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validate inputs
            if (email.isEmpty() || password.isEmpty()) {
                // Provide feedback
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Delegate login logic to LoginController
            logInController.loginUser(email, password);
        });

        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

    }
}
