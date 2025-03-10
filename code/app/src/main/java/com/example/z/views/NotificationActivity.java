package com.example.z.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.z.R;
import com.example.z.mood.MoodFragment;

/**
 * NotificationActivity allows users to view their notifications.
 * Users can navigate to different sections of the app and create a new mood post.
 */
public class NotificationActivity extends AppCompatActivity {

    /**
     * Called when the activity is created.
     * Initializes UI components and sets up navigation between different activities.
     *
     * @param savedInstanceState The saved state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Find navigation buttons
        ImageButton home = findViewById(R.id.nav_home);
        ImageButton profile = findViewById(R.id.nav_profile);
        ImageButton search = findViewById(R.id.nav_search);
        ImageButton addPostButton = findViewById(R.id.nav_add);

        // Set click listeners for navigation
        home.setOnClickListener(v -> navigateTo(HomeActivity.class));
        profile.setOnClickListener(v -> navigateTo(ProfileActivity.class));
        search.setOnClickListener(v -> navigateTo(SearchActivity.class));

        // Open dialog to add a new mood post
        addPostButton.setOnClickListener(v -> openAddPostDialog());
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
     * Opens the MoodFragment dialog to allow the user to add a new mood post.
     */
    private void openAddPostDialog() {
        MoodFragment moodFragment = new MoodFragment();
        moodFragment.show(getSupportFragmentManager(), "AddMoodFragment");
    }
}


