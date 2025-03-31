package com.example.z.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.z.mood.Mood;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller class that handles fetching and recommendation logic for similar moods
 * Uses Firebase Firestore to store and retrieve mood data.
 */
public class ForYouController {
    private static final String TAG = "ForYouController";
    private static final int MAX_USER_MOODS = 10;      // Maximum number of user's moods to analyze
    private static final int MAX_SIMILAR_MOODS = 20;   // Maximum number of similar moods to return
    private static int filterCounter = 0;              // Counter to alternate between type and situation

    private final String userId;
    private final FirebaseFirestore db;
    private List<Mood> userMoods;

    /**
     * Constructor for ForYouController.
     * @param userId
     *      The ID of the current user.
     */
    public ForYouController(String userId) {
        this.userId = userId;
        this.db = FirebaseFirestore.getInstance();
        this.userMoods = new ArrayList<>();
    }

    public interface SimilarMoodsCallback {
        void onSuccess(List<Mood> moods);
        void onError(Exception e);
    }

    public interface RefreshCallback {
        void onComplete();
    }

    /**
     * Updates user's mood history from Firestore.
     * @param callback
     *      Callback to be called when refresh is complete.
     */
    public void refreshUserMoods(RefreshCallback callback) {
        fetchUserMoods(() -> {
            callback.onComplete();
        });
    }

    /**
     * Retrieves similar moods based on user's mood history.
     * @param callback
     *      Callback to confirm retrieval or error.
     */
    public void getSimilarMoods(SimilarMoodsCallback callback) {
        if (userMoods.isEmpty()) {
            // First time loading - fetch user moods first
            fetchUserMoods(() -> {
                findSimilarMoods(callback);
            });
        } else {
            // User moods already loaded, just find similar moods
            findSimilarMoods(callback);
        }
    }

    /**
     * Fetches the user's recent moods from Firestore.
     * @param callback
     *      Callback to confirm retrieval or error.
     */
    private void fetchUserMoods(RefreshCallback callback) {
        Log.d(TAG, "Fetching user moods for userId: " + userId);
        db.collection("moods")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(MAX_USER_MOODS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Successfully fetched " + queryDocumentSnapshots.size() + " user moods");
                    userMoods.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Mood mood = documentToMood(document);
                        userMoods.add(mood);
                    }
                    callback.onComplete();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch user moods: " + e.getMessage(), e);
                    callback.onComplete();
                });
    }

    /**
     * Finds similar moods based on user's mood patterns.
     * @param callback
     *      Callback to confirm retrieval or error.
     */
    private void findSimilarMoods(SimilarMoodsCallback callback) {
        if (userMoods.isEmpty()) {
            Log.d(TAG, "No user moods found to base recommendations on");
            callback.onSuccess(new ArrayList<>());
            return;
        }

        // Extract user preference patterns
        Map<String, Integer> typeFrequency = new HashMap<>();
        Map<String, Integer> situationFrequency = new HashMap<>();

        for (Mood mood : userMoods) {
            // Count type frequencies
            String type = mood.getEmotionalState();
            typeFrequency.put(type, typeFrequency.getOrDefault(type, 0) + 1);

            // Count situation frequencies
            String situation = mood.getSocialSituation();
            situationFrequency.put(situation, situationFrequency.getOrDefault(situation, 0) + 1);
        }

        // Find most common type and situation
        String mostCommonType = getMostCommon(typeFrequency);
        String mostCommonSituation = getMostCommon(situationFrequency);

        Log.d(TAG, String.format("User preferences - Most common type: %s, situation: %s",
            mostCommonType, mostCommonSituation));

        // Query for similar moods from other users
        Query query = db.collection("moods")
                .whereEqualTo("private post", false)
                .orderBy("private post")
                .orderBy("timestamp", Query.Direction.DESCENDING);

        // Alternate between type and situation filters
        if (mostCommonType != null && mostCommonSituation != null) {
            if (filterCounter % 2 == 0) {
                Log.d(TAG, "Using type filter: " + mostCommonType);
                query = query.whereEqualTo("type", mostCommonType);
            } else {
                Log.d(TAG, "Using situation filter: " + mostCommonSituation);
                query = query.whereEqualTo("situation", mostCommonSituation);
            }
            filterCounter++; // Increment counter for next time
        } else if (mostCommonType != null) {
            Log.d(TAG, "Only type available, using type filter: " + mostCommonType);
            query = query.whereEqualTo("type", mostCommonType);
        } else if (mostCommonSituation != null) {
            Log.d(TAG, "Only situation available, using situation filter: " + mostCommonSituation);
            query = query.whereEqualTo("situation", mostCommonSituation);
        }

        Log.d(TAG, "Executing query with limit: " + MAX_SIMILAR_MOODS);

        // Execute query
        query.limit(MAX_SIMILAR_MOODS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Query successful. Found " + queryDocumentSnapshots.size() + " documents");
                    List<Mood> similarMoods = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Mood mood = documentToMood(document);
                        // Filter out the current user's moods in memory
                        if (!mood.getUserId().equals(userId)) {
                            similarMoods.add(mood);
                        }
                    }
                    Log.d(TAG, "After filtering user's own moods, returning " + similarMoods.size() + " moods");
                    callback.onSuccess(similarMoods);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Query failed with error: " + e.getMessage(), e);
                    callback.onError(e);
                });
    }

    /**
     * Converts a Firestore document to a Mood object.
     * @param document
     *      The Firestore document to convert.
     * @return
     *      A new Mood object containing the document data.
     */
    @NonNull
    private Mood documentToMood(DocumentSnapshot document) {
        String docId = document.getId();
        String docUserId = document.getString("userId");
        String username = document.getString("username");
        String type = document.getString("type");
        String trigger = document.getString("trigger");
        String situation = document.getString("situation");
        String description = document.getString("description");
        String emoticon = document.getString("emoji");
        String img = document.getString("img");
        // Consider moods as private by default if they don't have the "private post" field
        boolean isPrivate = !document.contains("private post") || document.getBoolean("private post");
        Date timestamp = null;

        if (document.getTimestamp("timestamp") != null) {
            timestamp = document.getTimestamp("timestamp").toDate();
        }

        Map<String, Object> location = null;
        if (document.contains("location")) {
            location = (Map<String, Object>) document.get("location");
        }
        return new Mood(docUserId, docId, username, type, trigger, situation,
                timestamp, location, description, img, emoticon, isPrivate);
    }

    /**
     * Finds the most common value in a frequency map.
     * @param frequencyMap
     *      Map containing values and their frequencies.
     * @return
     *      The most common value, or null if map is empty.
     */
    private String getMostCommon(Map<String, Integer> frequencyMap) {
        if (frequencyMap.isEmpty()) {
            return null;
        }

        String mostCommon = null;
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostCommon = entry.getKey();
            }
        }

        return mostCommon;
    }
}
