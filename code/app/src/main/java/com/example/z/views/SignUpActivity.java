package com.example.z.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.z.R;
import com.example.z.user.SignUpController;

/**
 * SignUpActivity handles user registration.
 * Users can enter their email, username, and password to create an account.
 * If the user already has an account, they can navigate to the login page.
 */
public class SignUpActivity extends AppCompatActivity {

    private EditText etEmail, etUsername, etPassword;
    private Button btnSignup;
    private TextView tvLogin;
    private SignUpController signUpController;

    /**
     * Called when the activity is created.
     * Initializes UI elements and sets up event listeners for sign-up and login navigation.
     *
     * @param savedInstanceState The saved state of the activity.
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
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Delegate sign-up logic to SignUpController
            signUpController.signUpUser(email, username, password);
        });

        /**
         * Navigates the user to the login page when they click "Already have an account?".
         */
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
            startActivity(intent);
        });
    }
}

