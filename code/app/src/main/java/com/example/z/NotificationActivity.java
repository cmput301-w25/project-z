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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private List<Object> users;
    private RecyclerView recyclerView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.notificationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setContentView(R.layout.activity_notification);

        db = FirebaseFirestore.getInstance();

        // Set a basic adapter with placeholder cards
        recyclerView.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Inflate your notification card layout
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.notification_card, parent, false);
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
}


/**
 * Activity to display follow requests in a RecyclerView and allow users to accept them.
 */
public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FollowRequestAdapter adapter;
    private List<FollowRequest> requestList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.notificationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestList = new ArrayList<>();
        adapter = new FollowRequestAdapter(requestList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadFollowRequests();
    }

    /**
     * Loads pending follow requests from Firestore and updates the RecyclerView.
     */
    private void loadFollowRequests() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            db.collection("follow_requests")
                    .whereEqualTo("followeeId", currentUser.getUid())
                    .whereEqualTo("status", "pending")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        requestList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            FollowRequest request = document.toObject(FollowRequest.class);
                            requestList.add(request);
                        }
                        adapter.notifyDataSetChanged();
                    });
        }
    }
}

/**
 * Adapter to display follow requests in a RecyclerView.
 */
class FollowRequestAdapter extends RecyclerView.Adapter<FollowRequestAdapter.ViewHolder> {
    private List<FollowRequest> requestList;
    private FirebaseFirestore db;

    public FollowRequestAdapter(List<FollowRequest> requestList) {
        this.requestList = requestList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FollowRequest request = requestList.get(position);
        holder.usernameTextView.setText("User ID: " + request.getFollowerId());

        holder.acceptButton.setOnClickListener(v -> {
            acceptFollowRequest(request.getFollowerId(), request.getFolloweeId());
            requestList.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        Button acceptButton;

        public ViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.txtUsername);
            acceptButton = itemView.findViewById(R.id.btnAcceptFollow);
        }
    }

    /**
     * Accepts a follow request by updating Firestore.
     * @param followerId The ID of the user who sent the request.
     * @param followeeId The ID of the user accepting the request.
     */
    private void acceptFollowRequest(String followerId, String followeeId) {
        db.collection("follow_requests")
                .whereEqualTo("followerId", followerId)
                .whereEqualTo("followeeId", followeeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        db.collection("follow_requests").document(document.getId())
                                .update("status", "accepted");
                    }
                });
    }
}

