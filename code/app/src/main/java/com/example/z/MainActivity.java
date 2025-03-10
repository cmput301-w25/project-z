package com.example.z;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // EDIT HERE TO TEST

        private RecyclerView recyclerView;
        private FirebaseFirestore db;
        private List<FollowRequest> followRequests;
        private FollowRequestAdapter adapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_notification);

            recyclerView = findViewById(R.id.notificationsRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            db = FirebaseFirestore.getInstance();
            followRequests = new ArrayList<>();
            adapter = new FollowRequestAdapter(followRequests);
            recyclerView.setAdapter(adapter);

            fetchFollowRequests();
        }

        private void fetchFollowRequests() {
            db.collection("follow_requests")
                    .whereEqualTo("status", "pending")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            followRequests.clear();
                            for (DocumentSnapshot doc : task.getResult()) {
                                String id = doc.getId();
                                String followerId = doc.getString("followerId");
                                String followeeId = doc.getString("followeeId");
                                followRequests.add(new FollowRequest(id, followerId, followeeId));
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
        }
    }

// END TEST HERE

    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_page), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    */
    /*
    // code to test out notifications adds 5 placeholder cards to test...comment out code above and uncomment this
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewUserMoods);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set a basic adapter with placeholder cards
        recyclerView.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Inflate your notification card layout
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_mood_card, parent, false);
                return new RecyclerView.ViewHolder(view) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                // No need to bind actual data for placeholders
            }

            @Override
            public int getItemCount() {
                return 5; // Display 5 placeholder notification cards
            }
        });


        // Apply insets for proper padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_page), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
     */
    /*private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Enable Firestore offline persistence
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true) // Enable offline persistence
                .build();
        firestore.setFirestoreSettings(settings);

        // Delay for the splash screen (optional)
        int SPLASH_DELAY = 2000; // 2 seconds

        new Handler().postDelayed(() -> {
            // Check if the user is logged in
            if (mAuth.getCurrentUser() != null) {
                // User is logged in, redirect to MainActivity
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            } else {
                // User is not logged in, redirect to LoginActivity
                startActivity(new Intent(MainActivity.this, LogInActivity.class));
            }
            finish(); // Close the SplashActivity
        }, SPLASH_DELAY);
    }*/

    /*
    // logs you out every time you close the app (for testing)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseAuth.getInstance().signOut(); // Sign out user when app is closed
    }
    */

//}
