package com.example.z.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.z.R;
import com.example.z.mood.Mood;
import com.example.z.mood.MoodArrayAdapter;
import com.example.z.mood.MoodFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

/**
 * ProfileActivity displays the user's profile and their posted moods.
 * Users can add new moods, navigate between different sections, and view their mood history.
 *
 *  Outstanding Issues:
 *      - Cannot add personal info yet
 */
public class ProfileActivity extends AppCompatActivity implements MoodFragment.OnMoodAddedListener {
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private MoodArrayAdapter adapter;
    private List<Mood> moodList = new ArrayList<>();
    private ListenerRegistration moodListener;
    private String username;

    /**
     * Called when the activity is created.
     * Initializes UI components, sets up navigation, fetches username, and listens for mood updates.
     *
     * @param savedInstanceState The saved state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();

        // Set up RecyclerView for displaying user moods
        recyclerView = findViewById(R.id.recyclerViewUserMoods);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MoodArrayAdapter(this, moodList);
        recyclerView.setAdapter(adapter);

        listenForMoodChanges(); // Listen for mood changes in real-time
        fetchUsername(); // Fetch username for displaying

        // Navigation buttons
        ImageButton createMood = findViewById(R.id.nav_add);
        ImageButton home = findViewById(R.id.nav_home);
        ImageButton notifications = findViewById(R.id.nav_notifications);
        ImageButton search = findViewById(R.id.nav_search);
        ImageButton map = findViewById(R.id.btnMapMoods);

        // Set click listeners for navigation
        createMood.setOnClickListener(v -> addMoodEvent());
        home.setOnClickListener(v -> switchActivity(HomeActivity.class));
        search.setOnClickListener(v -> switchActivity(SearchActivity.class));
        notifications.setOnClickListener(v -> switchActivity(NotificationActivity.class));
        map.setOnClickListener(v -> switchActivity(MapActivity.class));
    }

    /**
     * Fetches the username from Firestore and updates the UI.
     * If the username is not found, logs an error.
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
                        TextView usernamePlaceholder = findViewById(R.id.username);
                        usernamePlaceholder.setText(String.format("Welcome, %s!", username));
                    } else {
                        Log.e("Firestore", "Username not found for userId: " + userId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching username", e));
    }

    /**
     * Listens for real-time mood changes for the logged-in user.
     * Updates the RecyclerView whenever there is a change in Firestore.
     */
    private void listenForMoodChanges() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("Firestore", "User not logged in.");
            return;
        }

        String userId = user.getUid();

        moodListener = db.collection("moods")
                .whereEqualTo("userId", userId) // Filter moods by the current user
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
        Intent intent = new Intent(ProfileActivity.this, targetActivity);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // Smooth transition
        finish(); // Prevents returning to the previous activity
    }
}
