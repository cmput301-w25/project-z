package com.example.z;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    private FirebaseFirestore db;
    private CollectionReference usersRef;
    private CollectionReference moodsRef;

    public DatabaseManager() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        moodsRef = db.collection("moods"); // Reference to moods collection
    }

    public void saveMood(String userId, String moodType, String description) {
        // Create a mood object
        Map<String, Object> mood = new HashMap<>();
        mood.put("userId", userId);
        mood.put("type", moodType);
        mood.put("description", description);
        mood.put("timestamp", System.currentTimeMillis());

        // Save to Firestore
        moodsRef.add(mood)
                .addOnSuccessListener(documentReference ->
                        System.out.println("Mood saved with ID: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        System.err.println("Error saving mood: " + e));
    }

    public boolean validateUser(String username, String password) {
        return userDatabase.containsKey(username) && userDatabase.get(username).equals(password);
    }

    public boolean createUser(String username, String password) {
        if (userDatabase.containsKey(username)) {
            return false; // Username already exists
        }
        userDatabase.put(username, password);
        return true;
    }

    public void getInstance() {
        db = FirebaseFirestore.getInstance();
        movieRef = db.collection("movies");
    }
}

