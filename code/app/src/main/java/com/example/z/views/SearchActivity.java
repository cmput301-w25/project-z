package com.example.z.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.z.R;
import com.example.z.mood.MoodFragment;

/**
 * SearchActivity allows users to search for content within the app.
 * It also provides navigation options to different sections of the app.
 *
 *  Outstanding Issues:
 *      - Cannot display search results yet
 */
public class SearchActivity extends AppCompatActivity {

    private EditText searchBar;
    private Button searchButton;

    /**
     * Called when the activity is created.
     * Initializes UI components, sets up navigation, and handles search functionality.
     *
     * @param savedInstanceState The saved state of the activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Find search and navigation UI elements
        searchBar = findViewById(R.id.search_bar);
        searchButton = findViewById(R.id.search_button);
        ImageButton home = findViewById(R.id.nav_home);
        ImageButton profile = findViewById(R.id.nav_profile);
        ImageButton notifications = findViewById(R.id.nav_notifications);
        ImageButton addPostButton = findViewById(R.id.nav_add);

        // Set up navigation button listeners
        home.setOnClickListener(v -> navigateTo(HomeActivity.class));
        profile.setOnClickListener(v -> navigateTo(ProfileActivity.class));
        notifications.setOnClickListener(v -> navigateTo(NotificationActivity.class));

        // Open the add post dialog when clicking the add post button
        addPostButton.setOnClickListener(v -> openAddPostDialog());

        // Handle search button click
        searchButton.setOnClickListener(v -> {
            String query = searchBar.getText().toString().trim();
            // Search logic will be implemented here
        });
    }

    /**
     * Navigates to the specified activity.
     *
     * @param targetActivity The activity class to navigate to.
     */
    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // Smooth transition effect
    }

    /**
     * Opens the MoodFragment dialog to allow the user to add a new post.
     */
    private void openAddPostDialog() {
        MoodFragment moodFragment = new MoodFragment();
        moodFragment.show(getSupportFragmentManager(), "AddMoodFragment");
    }
}
