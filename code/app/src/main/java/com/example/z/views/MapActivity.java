package com.example.z.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.z.R;
import com.example.z.mood.Mood;
import com.example.z.mood.MoodFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * MapActivity is responsible for displaying a map with user events marked on it.
 * Users can navigate between different sections of the app and add a new mood post.
 *  Outstanding Issues:
 *      - Cannot display map with mood events yet
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    /**
     * Called when the activity is created.
     * Initializes UI components and sets up navigation between different activities.
     *
     * @param savedInstanceState The saved state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

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

        // Initialize the Map Fragment using getSupportFragmentManager()
        FragmentManager fragmentManager = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
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


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        loadMoodEvents();
    }

    private void loadMoodEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("moods").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Mood mood = doc.toObject(Mood.class);

                if (mood != null && mood.getLocation() != null) {
                    Double lat = (Double) mood.getLocation().get("latitude");
                    Double lng = (Double) mood.getLocation().get("longitude");

                    if (lat != null && lng != null) {
                        LatLng location = new LatLng(lat, lng);
                        mMap.addMarker(new MarkerOptions()
                                .position(location)
                                .title(mood.getUsername() + " is feeling " + mood.getEmotionalState())
                                .snippet(mood.getDescription()));
                    }
                }
            }
        }).addOnFailureListener(e -> Log.e("MapActivity", "Error loading mood events", e));
    }

}
