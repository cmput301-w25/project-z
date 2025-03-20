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

public class ForYouController {
    private static final int MAX_USER_MOODS = 10;
    private static final int MAX_SIMILAR_MOODS = 20;

    private final String userId;
    private final FirebaseFirestore db;
    private List<Mood> userMoods;

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

    public void refreshUserMoods(RefreshCallback callback) {
        fetchUserMoods(() -> {
            callback.onComplete();
        });
    }

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
                    // Handle error
                    callback.onComplete();
                });
    }

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

    @NonNull
    private Mood documentToMood(DocumentSnapshot document) {
        String docId = document.getId();
        String docUserId = document.getString("userId");
        String username = document.getString("username");
        String type = document.getString("type");
        String trigger = document.getString("trigger");
        String situation = document.getString("situation");
        String description = document.getString("description");
        Date timestamp = null;

        if (document.getTimestamp("timestamp") != null) {
            timestamp = document.getTimestamp("timestamp").toDate();
        }

        Map<String, Object> location = null;
        if (document.contains("location")) {
            location = (Map<String, Object>) document.get("location");
        }

        return new Mood(docUserId, docId, username, type, trigger, situation,
                timestamp, location, description);
    }

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
