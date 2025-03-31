package com.example.z.data;

import static android.app.PendingIntent.getActivity;

import com.example.z.comments.Comment;
import com.example.z.notifications.Notification;
import com.example.z.user.User;
import com.example.z.utils.OnFollowRequestsFetchedListener;
import com.example.z.utils.OnFollowStatusListener;
import com.example.z.utils.OnUserSearchCompleteListener;
import com.example.z.utils.OnUsernameFetchedListener;
import com.google.android.gms.tasks.OnCompleteListener;
import android.net.Uri;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private StorageReference imgRef;
    private String impref = "imgs";
    private CollectionReference followersRef;
    private CollectionReference commentsRef;

    /**
     * Initializes the Firestore database and references the "users" and "moods" collections.
     */
    public DatabaseManager() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        moodsRef = db.collection("moods");
        imgRef = FirebaseStorage.getInstance().getReference("user_images/" + UUID.randomUUID() + ".jpg");
        followersRef = db.collection("followers");
        commentsRef = db.collection(("comments"));
    }

    public void saveComment(Comment newComment, OnCommentSavedListener listener) {

        DocumentReference commentDocRef = commentsRef.document(newComment.getCommentId());

        commentDocRef.set(newComment)
                .addOnSuccessListener(docRef -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    public interface OnCommentSavedListener {
        void onSuccess();
        void onFailure(Exception e);
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
                         String description, String socialSituation, String trigger, Date datePosted, String img, Double latitude, Double longitude, String emoji, boolean isPrivate) {

        Map<String, Object> mood = new HashMap<>();
        mood.put("userId", userId);
        mood.put("username", username);
        mood.put("type", moodType);
        mood.put("description", description);
        mood.put("situation", socialSituation);
        mood.put("trigger", trigger);
        mood.put("timestamp", datePosted);
        mood.put("img", img);
        mood.put("emoji", emoji);
        mood.put("private post", isPrivate);

        if (latitude != null && longitude != null) {
            Map<String, Object> locationMap = new HashMap<>();
            locationMap.put("latitude", latitude);
            locationMap.put("longitude", longitude);
            mood.put("location", locationMap);
        }

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

                         String socialSituation, String trigger, Date updatedAt, String img, String emoji, boolean isPrivate,

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

        if (img != null) {
            updatedData.put("img", img);
        }

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

    /**
     * Searches for users by their username, excluding the logged-in user.
     * The search is case-insensitive and supports partial matching.
     *
     * @param username The username to search for.
     * @param listener The listener to handle the result of the search.
     */
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

    /**
     * Sends a follow request from one user to another.
     * The request is stored with a "pending" status in the Firestore database.
     *
     * @param followerId The ID of the user who is sending the follow request.
     * @param followedId The ID of the user who is being followed.
     */
    public void requestToFollow(String followerId, String followedId) {
        DocumentReference followRequestRef = followersRef.document(followerId + "_" + followedId);

        Map<String, Object> followRequest = new HashMap<>();
        followRequest.put("followerId", followerId);
        followRequest.put("followedId", followedId);
        followRequest.put("status", "pending");
        followRequest.put("createdAt", FieldValue.serverTimestamp());

        followRequestRef.set(followRequest)
                .addOnSuccessListener(aVoid ->
                        System.out.println("Follow request sent to: " + followedId))
                .addOnFailureListener(e ->
                        System.err.println("Error sending follow request: " + e));
    }

    /**
     * Listens for changes to the follow status between two users.
     * The listener is triggered when the follow status changes.
     *
     * @param followerId The ID of the user who is following.
     * @param followedId The ID of the user being followed.
     * @param listener   The listener to handle follow status changes.
     */
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

    /**
     * Retrieves the username of a user by their user ID.
     * The retrieved username is passed to the provided listener.
     *
     * @param userId   The ID of the user whose username is being retrieved.
     * @param listener The listener to handle the fetched username.
     */
    public static void getUsernameById(String userId, OnUsernameFetchedListener listener) {
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        listener.onFetched(username);
                    }
                });
    }

    /**
     * Retrieves the pending follow requests for a user.
     * The follow requests are fetched from Firestore and passed to the listener.
     *
     * @param userId   The ID of the user whose pending follow requests are being fetched.
     * @param listener The listener to handle the fetched follow requests.
     */
    public void getPendingFollowRequests(String userId, OnFollowRequestsFetchedListener listener) {
        followersRef.whereEqualTo("followedId", userId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notification> notifications = new ArrayList<>();
                    List<Task<String>> usernameTasks = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String followerId = doc.getString("followerId");
                        Date createdAt = doc.getDate("createdAt");

                        Notification notification = new Notification(userId, followerId, "pending", createdAt);
                        notifications.add(notification);
                    }

                    // Wait until all username fetch tasks are complete before passing the list
                    Tasks.whenAllSuccess(usernameTasks).addOnSuccessListener(results -> {
                        listener.onFetched(notifications);
                    });
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error fetching follow requests: " + e);
                    listener.onFetched(new ArrayList<>()); // Return empty list on failure
                });
    }
}

