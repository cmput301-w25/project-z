package com.example.z;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.z.views.LogInActivity;
import com.example.z.views.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * MainActivity serves as the splash screen and entry point of the app.
 * It handles Firebase authentication, Firestore offline persistence,
 * and redirects the user to the appropriate screen (ProfileActivity or LogInActivity).
 */
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    /**
     * Called when the activity is first created.
     * Initializes Firebase authentication and Firestore settings,
     * handles automatic logout if required, and redirects the user
     * after a short splash delay.
     *
     * @param savedInstanceState The saved state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Enable Firestore offline persistence
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true) // Enable offline persistence
                .build();
        firestore.setFirestoreSettings(settings);

        // Check if the user should be logged out
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean shouldLogout = prefs.getBoolean("shouldLogout", false);

        if (shouldLogout) {
            mAuth.signOut(); // Force logout
            prefs.edit().putBoolean("shouldLogout", false).apply(); // Reset flag
        }

        // Delay for the splash screen (optional)
        int SPLASH_DELAY = 2000; // 2 seconds

        new Handler().postDelayed(() -> {
            // Check if the user is logged in
            if (mAuth.getCurrentUser() != null) {
                // User is logged in, redirect to ProfileActivity
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            } else {
                // User is not logged in, redirect to LogInActivity
                startActivity(new Intent(MainActivity.this, LogInActivity.class));
            }
            finish(); // Close the SplashActivity
        }, SPLASH_DELAY);
    }

    /**
     * Called when the activity is stopped.
     * This can be used to automatically log out the user when the app is closed.
     *
     * Note: This method is currently commented out.
     */
    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("shouldLogout", true).apply();
    }
    public FirebaseAuth getAuth() {
        return mAuth;
    }
}

