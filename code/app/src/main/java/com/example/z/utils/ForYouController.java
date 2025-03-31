package com.example.z.utils;

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
    private static final int MAX_USER_MOODS = 10;      // Maximum number of user's moods to analyze
    private static final int MAX_SIMILAR_MOODS = 20;   // Maximum number of similar moods to return

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
        db.collection("moods")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(MAX_USER_MOODS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userMoods.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Mood mood = documentToMood(document);
                        userMoods.add(mood);
                    }
                    callback.onComplete();
                })
                .addOnFailureListener(e -> {
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
            // No user moods to base recommendations on
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

        // Query for similar moods from other users
        Query query = db.collection("moods")
                .whereNotEqualTo("userId", userId);

        // Apply filters based on user preferences
        if (mostCommonType != null && mostCommonSituation != null) {
            // Give equal chance to match by type or situation
            if (Math.random() < 0.5) {
                query = query.whereEqualTo("type", mostCommonType);
            } else {
                query = query.whereEqualTo("situation", mostCommonSituation);
            }
        } else if (mostCommonType != null) {
            query = query.whereEqualTo("type", mostCommonType);
        } else if (mostCommonSituation != null) {
            query = query.whereEqualTo("situation", mostCommonSituation);
        }

        // Execute query
        query.orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(MAX_SIMILAR_MOODS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Mood> similarMoods = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Mood mood = documentToMood(document);
                        similarMoods.add(mood);
                    }
                    callback.onSuccess(similarMoods);
                })
                .addOnFailureListener(e -> {
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
        boolean isPrivate = document.getBoolean("private post") != null ? document.getBoolean("private post") : false;
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
