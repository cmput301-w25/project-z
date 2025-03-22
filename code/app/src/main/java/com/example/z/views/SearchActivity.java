package com.example.z.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.z.R;
import com.example.z.data.DatabaseManager;
import com.example.z.mood.MoodFragment;
import com.example.z.user.SearchUserController;
import com.example.z.user.User;
import com.example.z.user.UserArrayAdapter;
import com.example.z.utils.OnUserSearchCompleteListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * SearchActivity allows users to search for content within the app.
 * It also provides navigation options to different sections of the app.
 *
 *  Outstanding Issues:
 *      - Cannot display search results yet
 */
public class SearchActivity extends AppCompatActivity implements OnUserSearchCompleteListener{

    private EditText searchBar;
    private Button searchButton;
    private RecyclerView recyclerView;
    private UserArrayAdapter userArrayAdapter;
    private SearchUserController searchUserController;
    private String currentUserId;

    /**
     * Called when the activity is created.
     * Initializes UI components, sets up navigation, and handles search functionality.
     *
     * @param savedInstanceState The saved state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Get the current user's ID
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize UI components
        searchBar = findViewById(R.id.search_bar);
        searchButton = findViewById(R.id.search_button);
        recyclerView = findViewById(R.id.recyclerView_search);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize SearchController
        DatabaseManager dbManager = new DatabaseManager();
        searchUserController = new SearchUserController(dbManager, this);

        userArrayAdapter = new UserArrayAdapter(searchUserController, currentUserId);
        recyclerView.setAdapter(userArrayAdapter);

        // Handle search button click
        searchButton.setOnClickListener(v -> {
            String query = searchBar.getText().toString().trim();
            searchUserController.searchUsers(query);
        });

        // Find navigation buttons
        ImageButton home = findViewById(R.id.nav_home);
        ImageButton profile = findViewById(R.id.nav_profile);
        ImageButton notifications = findViewById(R.id.nav_notifications);
        ImageButton search = findViewById(R.id.nav_search);
        ImageButton addPostButton = findViewById(R.id.nav_add);

        // Set click listeners for navigation
        home.setOnClickListener(v -> navigateTo(HomeActivity.class));
        profile.setOnClickListener(v -> navigateTo(ProfileActivity.class));
        notifications.setOnClickListener(v -> navigateTo(NotificationActivity.class));
        search.setOnClickListener(v -> navigateTo(SearchActivity.class));

        // Open dialog to add a new mood post
        addPostButton.setOnClickListener(v -> openAddPostDialog());
    }

    @Override
    public void onSuccess(List<User> users) {
        userArrayAdapter.updateUserList(users);
    }

    @Override
    public void onFailure(Exception e) {
        Toast.makeText(this, "Error fetching users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
