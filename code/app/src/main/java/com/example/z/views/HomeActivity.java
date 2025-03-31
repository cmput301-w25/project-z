package com.example.z.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.z.R;
import com.example.z.filter.FilterFragment;
import com.example.z.mood.Mood;
import com.example.z.mood.MoodArrayAdapter;
import com.example.z.mood.MoodFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * HomeActivity serves as the main feed where users can navigate to other pages
 * such as Profile, Notifications, and Search, as well as add new posts and when implemented view other users posts.
 *
 *  Outstanding Issues:
 *      - Cannot see other users moods yet
 */
public class HomeActivity extends AppCompatActivity implements MoodFragment.OnMoodAddedListener, FilterFragment.FilterListener{

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private MoodArrayAdapter adapter;
    private List<Mood> moodList = new ArrayList<>();
    private ListenerRegistration moodListener;
    private String username;
    private Map<String, Boolean> FilterMoods = new HashMap<>();
    private boolean RecentMood;
    private String SearchText = "";
    private TabLayout tabLayout;
    private TabLayout.OnTabSelectedListener tabListener;
    private TextView emptyStateText;

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

        db = FirebaseFirestore.getInstance();

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewMainFeed);
        emptyStateText = findViewById(R.id.empty_state_text);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MoodArrayAdapter(this, moodList);
        recyclerView.setAdapter(adapter);

        // Find navigation buttons
        ImageButton profile = findViewById(R.id.nav_profile);
        ImageButton notifications = findViewById(R.id.nav_notifications);
        ImageButton search = findViewById(R.id.nav_search);
        ImageButton addPostButton = findViewById(R.id.nav_add);
        ImageButton map = findViewById(R.id.btnMap);
        ImageButton filter = findViewById(R.id.btnFilter);
        tabLayout = findViewById(R.id.tab_layout);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            listenForMoodChanges();
            swipeRefreshLayout.setRefreshing(false);
        });

        tabListener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) { // "For You" tab
                    // Navigate to ForYouActivity
                    Intent intent = new Intent(HomeActivity.this, ForYouActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // Smooth transition effect
                    finish();
                } else if (tab.getPosition() == 1) { // "Following" tab
                    // Stay on current activity and refresh
                    listenForMoodChanges();
                    fetchUsername();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    recyclerView.smoothScrollToPosition(0);
                }
            }
        };

        tabLayout.addOnTabSelectedListener(tabListener);
        
        // Set initial tab to Following
        tabLayout.getTabAt(1).select();
        
        // Load initial data
        listenForMoodChanges();
        fetchUsername();

        // Set click listeners for navigation
        profile.setOnClickListener(v -> navigateTo(ProfileActivity.class));
        notifications.setOnClickListener(v -> navigateTo(NotificationActivity.class));
        search.setOnClickListener(v -> navigateTo(SearchActivity.class));
        map.setOnClickListener(v -> navigateTo(MapActivity.class));
        filter.setOnClickListener(v -> openFilterDialog());
        addPostButton.setOnClickListener(v -> addMoodEvent());
    }

    /**
     * Fetches the current user's username from Firestore and updates the welcome message.
     * If the user is not logged in or the username is not found, logs an error.
     */
    private void fetchUsername() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("Firestore", "User not logged in.");
            return;
        }

        String userId = user.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("username")) {
                        username = documentSnapshot.getString("username");

                        // Update UI with username
                        TextView usernamePlaceholder = findViewById(R.id.tvWelcome);
                        usernamePlaceholder.setText(String.format("Welcome, %s! How are you feeling today?", username));
                    } else {
                        Log.e("Firestore", "Username not found for userId: " + userId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching username", e));
    }

    /**
     * Listens for mood changes from followed users and updates the feed accordingly.
     */
    private void listenForMoodChanges() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("Firestore", "User not logged in.");
            showEmptyState("Please log in to see your feed");
            return;
        }

        String userId = user.getUid();
        List<String> following = new ArrayList<>();

        List<String> moods_selected = new ArrayList<>();
        for (Map.Entry<String, Boolean> selected : FilterMoods.entrySet()) {
            if (selected.getValue()) {
                moods_selected.add(selected.getKey());
            }
        }
        db.collection("followers")
                .whereEqualTo("followerId", userId).get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String followedId = doc.getString("followedId");
                        following.add(followedId);
                    }
                    if (!following.isEmpty()) {
                        List<Mood> moods_list = new ArrayList<>();
                        int[] iteration = {0};
                        for (String followerId: following) {
                            Query query = db.collection("moods")
                                    .whereEqualTo("userId", followerId)
                                    .whereEqualTo("private post", false);

                            if (!moods_selected.isEmpty()) {
                                query = query.whereIn("type", moods_selected);
                            }

                            if (RecentMood) {
                                Date Recent = new Date(new Date().getTime() - (7 * 24 * 60 * 60 * 1000));
                                query = query.whereGreaterThan("timestamp", Recent);
                            }

                            moodListener = query.orderBy("timestamp", Query.Direction.DESCENDING)
                                    .limit(3)
                                    .addSnapshotListener((snapshots, error) -> {
                                        if (error != null) {
                                            Log.e("Firestore", "Error listening for mood changes", error);
                                            showEmptyState("Error loading feed: " + error.getMessage());
                                            return;
                                        }

                                        if (snapshots != null) {
                                            Log.d("Firestore", "Mood documents found: " + snapshots.size());

                                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                                Mood mood = doc.toObject(Mood.class);
                                                if (mood != null) {
                                                    Log.d("Firestore", "Mood found: " + mood.getDescription());
                                                    if (!SearchText.isEmpty()) {
                                                        if (mood.getDescription().contains(SearchText)) {
                                                            mood.setDocumentId(doc.getId());
                                                            moods_list.add(mood);
                                                        }
                                                    }else{
                                                        mood.setDocumentId(doc.getId());
                                                        moods_list.add(mood);
                                                    }
                                                }
                                            }
                                            iteration[0]++;
                                            if (iteration[0] == following.size()) {
                                                moodList.clear();
                                                moods_list.sort((m1, m2) -> m2.getCreatedAt().compareTo(m1.getCreatedAt()));
                                                moodList.addAll(moods_list);
                                                adapter.notifyDataSetChanged();
                                                
                                                if (moodList.isEmpty()) {
                                                    showEmptyState("No moods found. Try following more users!");
                                                } else {
                                                    hideEmptyState();
                                                }
                                            }
                                        }
                                    });
                        }
                    } else {
                        showEmptyState("No moods found. Try following some users!");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching followers", e);
                    showEmptyState("Error loading feed: " + e.getMessage());
                });
    }

    /**
     * Shows empty state message when no moods available
     */
    private void showEmptyState(String message) {
        emptyStateText.setText(message);
        emptyStateText.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    /**
     * Hides empty state message and shows RecyclerView
     */
    private void hideEmptyState() {
        emptyStateText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Navigates to a specified activity with a smooth transition animation.
     * 
     * @param targetActivity
     *      The destination activity class
     */
    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // Smooth transition effect
    }

    /**
     * Opens the MoodFragment dialog to allow the user to add a new mood post.
     */
    private void addMoodEvent() {
        MoodFragment addMood = new MoodFragment();
        addMood.setMoodAddedListener(this);
        addMood.show(getSupportFragmentManager(), "MoodFragment");
    }

    /**
     * Callback method called when a new mood is added through the MoodFragment.
     * Currently empty as the feed is automatically updated through the Firestore listener.
     * 
     * @param newMood
     *      The newly added mood object
     */
    public void onMoodAdded(Mood newMood) {
    }

    /**
     * Opens the FilterFragment dialog to allow the user to filter their feed.
     */
    private void openFilterDialog() {
        FilterFragment filterFragment = new FilterFragment(FilterMoods, RecentMood, SearchText, this);
        filterFragment.show(getSupportFragmentManager(), "FilterFragment");
    }

    /**
     * Callback method called when filters are applied through the FilterFragment.
     * Updates the current filter state and refreshes the feed with the new filters.
     * 
     * @param FilterMoods
     *      Map of mood types and their selected state
     * @param RecentMood
     *      Whether to show only recent moods (within last 7 days)
     * @param SearchText
     *      Text to filter moods by description
     */
    @Override
    public void onFilterApplied(Map<String, Boolean> FilterMoods, boolean RecentMood, String SearchText) {
        this.FilterMoods = FilterMoods;
        this.RecentMood = RecentMood;
        this.SearchText = SearchText;
        listenForMoodChanges();
    }

    /**
     * Clean up tab listener when activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tabLayout != null) {
            tabLayout.removeOnTabSelectedListener(tabListener);
        }
    }


}


