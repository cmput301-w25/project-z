package com.example.z;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class AccessAppActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText usernameInput;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_app);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        usernameInput = findViewById(R.id.username_input);

        // Set up login button
        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide email field for login
                emailInput.setVisibility(View.GONE);
                loginUser();
            }
        });

        // Set up signup button
        Button signupButton = findViewById(R.id.signup_button);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show email field for signup
                emailInput.setVisibility(View.VISIBLE);
                signupUser();
            }
        });
    }

    private void loginUser() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter a username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Proceed with login logic
        dbLogin(username, password);
    }

    private void dbLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login successful
                            Log.d("Login", "Login successful for email: " + email);
                            grant_access();
                        } else {
                            // Login failed
                            Log.e("Login", "Login failed: " + task.getException().getMessage());
                            Toast.makeText(AccessAppActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signupUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Signup successful
                            Log.d("Signup", "Signup successful for email: " + email);

                            // Save additional user data to Firestore
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveUserToFirestore(user.getUid(), email, username);
                            }

                            // Notify user and grant access
                            Toast.makeText(AccessAppActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                            grant_access();
                        } else {
                            // Signup failed
                            Log.e("Signup", "Signup failed: " + task.getException().getMessage());
                            Toast.makeText(AccessAppActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserToFirestore(String userId, String email, String username) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("username", username);

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "User data saved to Firestore");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Failed to save user data: " + e.getMessage());
                    }
                });
    }

    private void grant_access() {
        Intent intent = new Intent(this, FollowingActivity.class);
        startActivity(intent);
        finish();
    }
}

