package com.example.z.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.z.R;
import com.example.z.mood.MoodFragment;

/**
 * HomeActivity serves as the main feed where users can navigate to other pages
 * such as Profile, Notifications, and Search, as well as add new posts and when implemented view other users posts.
 */
public class HomeActivity extends AppCompatActivity {

    /**
     * Called when the activity is created.
     * Initializes navigation buttons and sets up click listeners for page switching.
     *
     * @param savedInstanceState The saved state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_feed);

        // Find navigation buttons
        ImageButton profile = findViewById(R.id.nav_profile);
        ImageButton notifications = findViewById(R.id.nav_notifications);
        ImageButton search = findViewById(R.id.nav_search);
        ImageButton addPostButton = findViewById(R.id.nav_add);
        ImageButton map = findViewById(R.id.btnMap);

        // Set click listeners for navigation
        profile.setOnClickListener(v -> navigateTo(ProfileActivity.class));
        notifications.setOnClickListener(v -> navigateTo(NotificationActivity.class));
        search.setOnClickListener(v -> navigateTo(SearchActivity.class));
        map.setOnClickListener(v -> navigateTo(MapActivity.class));

        addPostButton.setOnClickListener(v -> openAddPostDialog());
    }

    /**
     * Navigates to the specified activity.
     *
     * @param targetActivity The destination activity class.
     */
    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // Smooth transition effect
    }

    /**
     * Opens the MoodFragment dialog for users to add a new post.
     */
    private void openAddPostDialog() {
        MoodFragment moodFragment = new MoodFragment();
        moodFragment.show(getSupportFragmentManager(), "AddMoodFragment");
    }
}


