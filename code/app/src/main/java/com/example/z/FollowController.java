package com.example.z;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;

import java.util.Map;

/**
 * Controller class for handling follow requests and follow relationships in Firebase.
 */
public class FollowController {
    private FirebaseFirestore db;

    /**
     * Constructor initializes Firebase Firestore instance.
     */
    public FollowController() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Sends a follow request from one user to another.
     *
     * @param followerId The ID of the user sending the request.
     * @param followeeId The ID of the user receiving the request.
     */
    public void sendFollowRequest(String followerId, String followeeId) {
        Map<String, Object> request = new HashMap<>();
        request.put("followerId", followerId);
        request.put("followeeId", followeeId);
        request.put("timestamp", System.currentTimeMillis());

        db.collection("follow_requests")
                .add(request)
                .addOnSuccessListener(documentReference -> System.out.println("Follow request sent!"))
                .addOnFailureListener(e -> System.err.println("Error sending request: " + e.getMessage()));
    }

    /**
     * Accepts a follow request, removing it from pending requests and adding the relationship
     * to both "followers" and "following" collections.
     *
     * @param followerId The ID of the user who requested to follow.
     * @param followeeId The ID of the user accepting the request.
     */
    public void acceptFollowRequest(String followerId, String followeeId) {
        Query requestQuery = db.collection("follow_requests")
                .whereEqualTo("followerId", followerId)
                .whereEqualTo("followeeId", followeeId);

        requestQuery.get().addOnSuccessListener(querySnapshot -> {
            querySnapshot.forEach(document -> document.getReference().delete());

            // Create a new Following object
            Following following = new Following(followerId, followeeId, System.currentTimeMillis());

            // Store the relationship in the followers and following collections
            db.collection("followers").add(following);
            db.collection("following").add(following);
        });
    }

    /**
     * Removes a follow request, either because the request was declined or canceled.
     *
     * @param followerId The ID of the user who sent the request.
     * @param followeeId The ID of the user who received the request.
     */
    public void removeFollowRequest(String followerId, String followeeId) {
        Query requestQuery = db.collection("follow_requests")
                .whereEqualTo("followerId", followerId)
                .whereEqualTo("followeeId", followeeId);

        requestQuery.get().addOnSuccessListener(querySnapshot -> {
            querySnapshot.forEach(document -> document.getReference().delete());
        });
    }
}

