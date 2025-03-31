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
import com.example.z.filter.FilterFragment;
import com.example.z.mood.Mood;
import com.example.z.mood.MoodArrayAdapter;
import com.example.z.mood.MoodFragment;
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

        // Set up RecyclerView for displaying user moods
        recyclerView = findViewById(R.id.recyclerViewMainFeed);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MoodArrayAdapter(this, moodList);
        recyclerView.setAdapter(adapter);

        listenForMoodChanges(); // Listen for mood changes in real-time
        fetchUsername(); // Fetch username for displaying

        // Find navigation buttons
        ImageButton profile = findViewById(R.id.nav_profile);
        ImageButton notifications = findViewById(R.id.nav_notifications);
        ImageButton search = findViewById(R.id.nav_search);
        ImageButton addPostButton = findViewById(R.id.nav_add);
        ImageButton map = findViewById(R.id.btnMap);
        ImageButton filter = findViewById(R.id.btnFilter);

        // Set click listeners for navigation
        profile.setOnClickListener(v -> navigateTo(ProfileActivity.class));
        notifications.setOnClickListener(v -> navigateTo(NotificationActivity.class));
        search.setOnClickListener(v -> navigateTo(SearchActivity.class));
        map.setOnClickListener(v -> navigateTo(MapActivity.class));
        filter.setOnClickListener(v -> openFilterDialog());
        addPostButton.setOnClickListener(v -> addMoodEvent());
    }

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

    private void listenForMoodChanges() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("Firestore", "User not logged in.");
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
                                    .whereEqualTo("private post", true);


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
                                            }
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching followers", e));

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
     * Opens the MoodFragment dialog to allow the user to add a new mood post.
     */
    private void addMoodEvent() {
        MoodFragment addMood = new MoodFragment();
        addMood.setMoodAddedListener(this);
        addMood.show(getSupportFragmentManager(), "MoodFragment");
    }

    /**
     * @param newMood The newly added mood.
     */
    public void onMoodAdded(Mood newMood) {
    }

    private void openFilterDialog() {
        FilterFragment filterFragment = new FilterFragment(FilterMoods, RecentMood, SearchText, this);
        filterFragment.show(getSupportFragmentManager(), "FilterFragment");
    }

    /**
     * This is a temporary empty implementation just to test the dialog.
     */
    @Override
    public void onFilterApplied(Map<String, Boolean> FilterMoods, boolean RecentMood, String SearchText) {
        this.FilterMoods = FilterMoods;
        this.RecentMood = RecentMood;
        this.SearchText = SearchText;
        listenForMoodChanges();
    }
}


