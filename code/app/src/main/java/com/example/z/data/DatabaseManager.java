package com.example.z.data;

import static android.app.PendingIntent.getActivity;

import com.example.z.user.User;
import com.example.z.utils.OnFollowStatusListener;
import com.example.z.utils.OnUserSearchCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages interactions with Firestore, including saving and editing mood entries.
 *
 *  Outstanding Issues:
 *      - None
 */
public class DatabaseManager {
    private FirebaseFirestore db;
    private CollectionReference usersRef;
    private CollectionReference moodsRef;
    private CollectionReference followersRef;

    /**
     * Initializes the Firestore database and references the "users" and "moods" collections.
     */
    public DatabaseManager() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        moodsRef = db.collection("moods");
        followersRef = db.collection("followers");
    }

    /**
     * Saves a new mood entry to Firestore.
     *
     * @param userId          The ID of the user posting the mood.
     * @param moodDocRef      The reference to the mood document in Firestore.
     * @param username        The username of the user posting the mood.
     * @param moodType        The emotional state associated with the mood.
     * @param description     A brief description of the mood.
     * @param socialSituation The social situation during the mood event.
     * @param trigger         The trigger that caused the mood.
     * @param datePosted      The timestamp when the mood was created.
     */
    public void saveMood(String userId, DocumentReference moodDocRef, String username, String moodType,
                         String description, String socialSituation, String trigger, Date datePosted, String emoji, boolean isPrivate) {
        Map<String, Object> mood = new HashMap<>();
        mood.put("userId", userId);
        mood.put("username", username);
        mood.put("type", moodType);
        mood.put("description", description);
        mood.put("situation", socialSituation);
        mood.put("trigger", trigger);
        mood.put("timestamp", datePosted);
        mood.put("emoji", emoji);
        mood.put("private post", isPrivate);

        moodDocRef.set(mood)
                .addOnSuccessListener(aVoid ->
                        System.out.println("Mood saved with ID: " + moodDocRef.getId()))
                .addOnFailureListener(e ->
                        System.err.println("Error saving mood: " + e));
    }

    /**
     * Edits an existing mood entry in Firestore.
     *
     * @param moodId           The Firestore document ID of the mood entry.
     * @param userId           The ID of the user editing the mood.
     * @param moodType         The updated emotional state.
     * @param description      The updated description.
     * @param socialSituation  The updated social situation.
     * @param trigger          The updated trigger.
     * @param updatedAt        The new timestamp when the mood was edited.
     * @param onSuccessListener Callback for successful update.
     * @param onFailureListener Callback for failure during update.
     */
    public void editMood(String moodId, String userId, String moodType, String description,
                         String socialSituation, String trigger, Date updatedAt, String emoji, boolean isPrivate,
                         OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        DocumentReference moodRef = moodsRef.document(moodId);

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("description", description);
        updatedData.put("trigger", trigger);
        updatedData.put("type", moodType);
        updatedData.put("situation", socialSituation);
        updatedData.put("timestamp", updatedAt);
        updatedData.put("emoji", emoji);
        updatedData.put("private post", isPrivate);

        // Fetch latest username in case it has changed
        usersRef.document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("username")) {
                        String latestUsername = documentSnapshot.getString("username");
                        updatedData.put("username", latestUsername);
                    }

                    // Update Firestore document with new mood details
                    moodRef.update(updatedData)
                            .addOnSuccessListener(onSuccessListener)
                            .addOnFailureListener(onFailureListener);
                })
                .addOnFailureListener(onFailureListener);
    }

    public void searchUsersByUsername(String username, OnUserSearchCompleteListener listener) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef
                .orderBy("username") // Firestore requires ordering for range queries
                .startAt(username)  // Starts at the search term
                .endAt(username + "\uf8ff") // Ensures partial matches
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String email = document.getString("email");
                        String fetchedUsername = document.getString("username");
                        String id = document.getId();

                        // Exclude the logged-in user
                        if (email != null && fetchedUsername != null && !id.equals(currentUserId)) {
                            users.add(new User(email, fetchedUsername, id));
                        }
                    }
                    listener.onSuccess(users);
                })
                .addOnFailureListener(listener::onFailure);
    }

    public void requestToFollow(String followerId, String followedId) {
        DocumentReference followRequestRef = followersRef.document(followerId + "_" + followedId);

        Map<String, Object> followRequest = new HashMap<>();
        followRequest.put("followerId", followerId);
        followRequest.put("followedId", followedId);
        followRequest.put("status", "pending");

        followRequestRef.set(followRequest)
                .addOnSuccessListener(aVoid ->
                        System.out.println("Follow request sent to: " + followedId))
                .addOnFailureListener(e ->
                        System.err.println("Error sending follow request: " + e));
    }

    public void getFollowStatus(String followerId, String followedId, OnFollowStatusListener listener) {
        DocumentReference followRequestRef = followersRef.document(followerId + "_" + followedId);

        followRequestRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");
                        if (status != null) {
                            listener.onFollowStatusRetrieved(status); // Pass status to listener
                            return;
                        }
                    }
                    listener.onFollowStatusRetrieved("not_following"); // Default case if no record
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error fetching follow status: " + e);
                    listener.onFollowStatusRetrieved("error");
                });
    }

    public void listenForFollowStatusChanges(String followerId, String followedId, OnFollowStatusListener listener) {
        followersRef.document(followerId + "_" + followedId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        System.err.println("Error listening for follow status changes: " + error);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");
                        listener.onFollowStatusRetrieved(status);
                    } else {
                        listener.onFollowStatusRetrieved(null);
                    }
                });
    }

    public void acceptFollowRequest(String followerId, String followedId) {
        DocumentReference followRef = followersRef.document(followerId + "_" + followedId);

        followRef.update("status", "accepted")
                .addOnSuccessListener(aVoid ->
                        System.out.println("Follow request accepted for: " + followerId))
                .addOnFailureListener(e ->
                        System.err.println("Error accepting follow request: " + e));
    }


    public void getPendingFollowRequests(String userId) {
        followersRef.whereEqualTo("followedId", userId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> pendingRequests = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        pendingRequests.add(document.getString("followerId"));
                    }
                    System.out.println("Pending follow requests: " + pendingRequests);
                })
                .addOnFailureListener(e ->
                        System.err.println("Error fetching pending follow requests: " + e));
    }



}

