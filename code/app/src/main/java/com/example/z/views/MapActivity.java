package com.example.z.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;


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
import com.google.android.gms.maps.CameraUpdateFactory;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import android.location.Location;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;



/**
 * MapActivity is responsible for displaying a map with user events marked on it.
 * Users can navigate between different sections of the app and add a new mood post.
 *  Outstanding Issues:
 *      - Cannot display map with mood events yet
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentUserLocation;

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
        ImageButton filterBtn = findViewById(R.id.btnFilter2);


        // Set click listeners for navigation
        home.setOnClickListener(v -> navigateTo(HomeActivity.class));
        profile.setOnClickListener(v -> navigateTo(ProfileActivity.class));
        notifications.setOnClickListener(v -> navigateTo(NotificationActivity.class));
        search.setOnClickListener(v -> navigateTo(SearchActivity.class));
        filterBtn.setOnClickListener(v -> filterDialog());

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
        LatLng display = new LatLng(53.5461, -113.4938);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(display, 5));
        loadMoodEvents();
    }

    private void filterDialog() {
        String[] options = {"My Moods", "Following Moods", "Following Moods within 5km"};

        new AlertDialog.Builder(this)
                .setTitle("Filter Moods")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        loadMoodEvents();
                    } else if (which == 1) {
                        loadRecentMood(false);
                    } else if (which == 2) {
                        requestUserLocation();
                    }
                })
                .show();
    }



    private void loadMoodEvents() {
        mMap.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("moods")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Mood mood = doc.toObject(Mood.class);

                        if (mood != null && mood.getLocation() != null) {
                            Double lat = (Double) mood.getLocation().get("latitude");
                            Double lng = (Double) mood.getLocation().get("longitude");

                            if (lat != null && lng != null) {
                                LatLng location = new LatLng(lat, lng);
                                mMap.addMarker(new MarkerOptions()
                                        .position(location)
                                        .title(mood.getEmotionalState()));
                                }
                            }
                        }
                }).addOnFailureListener(e -> Log.e("MapActivity", "Error loading mood events", e));
    }


    private void loadRecentMood(boolean within5km) {
        mMap.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("followers")
                .whereEqualTo("followerId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String followeeId = doc.getString("followedId");

                        if (followeeId != null) {
                            db.collection("moods")
                                    .whereEqualTo("userId", followeeId)
                                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener(moodSnapshots -> {
                                        for (DocumentSnapshot moodDoc : moodSnapshots) {
                                            Mood mood = moodDoc.toObject(Mood.class);

                                            if (mood != null && mood.getLocation() != null) {
                                                Double lat = (Double) mood.getLocation().get("latitude");
                                                Double lng = (Double) mood.getLocation().get("longitude");

                                                if (lat != null && lng != null) {
                                                    LatLng location = new LatLng(lat, lng);

                                                    if (within5km && !isWithin5km(location)) return;

                                                    mMap.addMarker(new MarkerOptions()
                                                            .position(location)
                                                            .title(mood.getEmotionalState())
                                                            .snippet("@" + mood.getUsername()));
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                }).addOnFailureListener(e -> Log.e("MapActivity", "Error loading recent moods", e));
    }

    private boolean isWithin5km(LatLng moodLocation) {
        float[] result = new float[1];
        Location.distanceBetween(
                currentUserLocation.latitude, currentUserLocation.longitude,
                moodLocation.latitude, moodLocation.longitude,
                result
            );

        return result[0] <= 5000;
        }


    private void requestUserLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            Log.d("MapActivity", "Location attached: " + currentUserLocation);
                            mMap.clear();
                            loadRecentMood(true);
                        } else {
                            Log.w("MapActivity", "Location not available. Skipping 5km filter.");
                            }
                        });
            }
        }


}
