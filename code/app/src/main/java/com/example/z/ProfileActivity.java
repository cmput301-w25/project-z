package com.example.z;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ProfileActivity extends AppCompatActivity {
    private User user;
    private Mood mood;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();

        // get moods belonging to user
        getMoods("exampleUserId");

        // Display main buttons
        Button createMood = findViewById(R.id.create_mood_button);
        Button following= findViewById(R.id.create_timeline_button);
        Button profile = findViewById(R.id.profile_button);
        Button notifications = findViewById(R.id.notifications_button);
        Button search = findViewById(R.id.search_button);
    }

    private void getMoods(String userId) {
        db.collection("moods")
                .whereEqualTo("userId", userId) // Fetch moods for this user
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d("Firestore", "Mood: " + document.getData());
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Error getting moods", e));
    }

    following.setOnClickListener(v -> {
        Intent intent = new Intent(ProfileActivity.this, FollowingActivity.class);
        startActivity(intent);
    });

    profile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

    notifications.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

    search.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SearchActivity.class);
            startActivity(intent);
        });
}