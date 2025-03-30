package com.example.z.views;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.z.R;
import com.example.z.data.DatabaseManager;
import com.example.z.mood.Mood;
import com.example.z.mood.MoodArrayAdapter;
import com.example.z.mood.MoodFragment;
import com.example.z.user.UserController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class PublicProfileActivity extends AppCompatActivity implements MoodFragment.OnMoodAddedListener{
    private FirebaseFirestore db;
    private DatabaseManager dbManager;
    private RecyclerView recyclerView;
    private MoodArrayAdapter adapter;
    private List<Mood> moodList = new ArrayList<>();
    private ListenerRegistration moodListener;
    private String selectedUserId;
    private String selectedUsername;
    private Button followButton;
    private UserController userController;
    private String currentUserId;
    private String followStatus;

    /**
     * Called when the activity is created.
     * Initializes UI components, sets up navigation, fetches username, and listens for mood updates.
     *
     * @param savedInstanceState The saved state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_profile);

        db = FirebaseFirestore.getInstance();
        dbManager = new DatabaseManager();

        userController = new UserController(dbManager);  // Initialize UserController

        // Retrieve user ID and username from intent
        Intent intent = getIntent();
        selectedUserId = intent.getStringExtra("userId");
        selectedUsername = intent.getStringExtra("username");

        if (selectedUserId == null) {
            Log.e("PublicProfileActivity", "No user ID provided!");
            finish();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("Firestore", "User not logged in.");
            return;
        }

        currentUserId = user.getUid();

        // Set up RecyclerView for displaying user moods
        recyclerView = findViewById(R.id.recyclerViewUserMoods);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MoodArrayAdapter(this, moodList);
        recyclerView.setAdapter(adapter);


        fetchUsername(); // Fetch username for displaying

        // Initialize Follow Button
        followButton = findViewById(R.id.followButton);
        followStatus = "notFollowing";
        updateFollowButton(currentUserId, selectedUserId);

        listenForMoodChanges(); // Listen for mood changes in real-time

        // Navigation buttons
        ImageButton createMood = findViewById(R.id.nav_add);
        ImageButton home = findViewById(R.id.nav_home);
        ImageButton notifications = findViewById(R.id.nav_notifications);
        ImageButton search = findViewById(R.id.nav_search);
        //ImageButton map = findViewById(R.id.btnMapMoods);

        // Set click listeners for navigation
        createMood.setOnClickListener(v -> addMoodEvent());
        home.setOnClickListener(v -> switchActivity(HomeActivity.class));
        search.setOnClickListener(v -> switchActivity(SearchActivity.class));
        notifications.setOnClickListener(v -> switchActivity(NotificationActivity.class));
        //map.setOnClickListener(v -> switchActivity(MapActivity.class));
    }

    /**
     * Fetches the username from Firestore and updates the UI.
     * If the username is not found, logs an error.
     */
    private void fetchUsername() {
        db.collection("users").document(selectedUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("username")) {
                        selectedUsername = documentSnapshot.getString("username");

                        // Update UI with username
                        TextView usernamePlaceholder = findViewById(R.id.username);
                        usernamePlaceholder.setText(String.format("%s", selectedUsername));
                    } else {
                        Log.e("Firestore", "Username not found for userId: " + selectedUserId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching username", e));
    }

    /**
     * Listens for real-time mood changes for the user's profile.
     * Updates the RecyclerView whenever there is a change in Firestore.
     */
    private void listenForMoodChanges() {
        moodListener = db.collection("moods")
                .whereEqualTo("userId", selectedUserId) // Filter moods by the current user
                .whereEqualTo("private post", false) // Only fetch public moods
                .orderBy("private post")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Order by most recent
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Error listening for mood changes", error);
                        return;
                    }

                    if (snapshots != null) {
                        moodList.clear();

                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Mood mood = doc.toObject(Mood.class);
                            if (mood != null) {
                                mood.setDocumentId(doc.getId());
                                moodList.add(mood);
                            }
                        }
                        adapter.notifyDataSetChanged(); // Refresh RecyclerView
                    }
                });
//        if ("following".equals(followStatus)) {
//            Log.d("PublicProfileActivity", "User is following, fetching all moods (public and private).");
//            moodListener = db.collection("moods")
//                    .whereEqualTo("userId", selectedUserId) // Filter moods by the current user
//                    .orderBy("timestamp", Query.Direction.DESCENDING) // Order by most recent
//                    .addSnapshotListener((snapshots, error) -> {
//                        if (error != null) {
//                            Log.e("Firestore", "Error listening for mood changes", error);
//                            return;
//                        }
//
//                        if (snapshots != null) {
//                            moodList.clear();
//
//                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
//                                Mood mood = doc.toObject(Mood.class);
//                                if (mood != null) {
//                                    mood.setDocumentId(doc.getId());
//                                    moodList.add(mood);
//                                }
//                            }
//                            adapter.notifyDataSetChanged(); // Refresh RecyclerView
//                        }
//                    });
//        } else {
//            Log.d("PublicProfileActivity", "User is NOT following, fetching only public moods.");
//            moodListener = db.collection("moods")
//                    .whereEqualTo("userId", selectedUserId) // Filter moods by the selected user
//                    .whereEqualTo("`private post`", false) // Only fetch public moods
//                    .orderBy("timestamp", Query.Direction.DESCENDING) // Order by most recent
//                    .addSnapshotListener((snapshots, error) -> {
//                        if (error != null) {
//                            Log.e("Firestore", "Error listening for mood changes", error);
//                            return;
//                        }
//
//                        if (snapshots != null) {
//                            moodList.clear();
//
//                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
//                                Mood mood = doc.toObject(Mood.class);
//                                if (mood != null) {
//                                    mood.setDocumentId(doc.getId());
//                                    moodList.add(mood);
//                                }
//                            }
//                            adapter.notifyDataSetChanged(); // Refresh RecyclerView
//                        }
//                    });
//        }
    }

    private void updateFollowButton(String currentUserId, String selectedUserId) {
        DatabaseManager dbManager = new DatabaseManager();
        Button followButton = findViewById(R.id.followButton);

        // Listen for real-time updates
        dbManager.listenForFollowStatusChanges(currentUserId, selectedUserId, status -> {
            if ("accepted".equals(status)) {
                followStatus = "folllowing";
                followButton.setText("Following");
                followButton.setEnabled(false);
            } else if ("pending".equals(status)) {
                followButton.setText("Request Pending");
                followButton.setEnabled(false);
            } else {
                followButton.setText("Follow");
                followButton.setEnabled(true);
                followButton.setOnClickListener(v -> {
                    dbManager.requestToFollow(currentUserId, selectedUserId);
                    followButton.setText("Request Pending");
                    followButton.setEnabled(false);
                });
            }
        });
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
     * Called when a new mood is added.
     * Updates the RecyclerView with the new mood at the top.
     *
     * @param newMood The newly added mood.
     */
    public void onMoodAdded(Mood newMood) {
        moodList.add(0, newMood); // Add new mood to the top of the list
        adapter.notifyItemInserted(0); // Refresh RecyclerView
    }

    /**
     * Switches the current activity to another activity.
     *
     * @param targetActivity The activity class to navigate to.
     */
    private void switchActivity(Class<?> targetActivity) {
        Intent intent = new Intent(PublicProfileActivity.this, targetActivity);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // Smooth transition
        finish(); // Prevents returning to the previous activity
    }
}

