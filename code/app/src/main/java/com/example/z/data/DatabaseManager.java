package com.example.z.data;

import static android.app.PendingIntent.getActivity;

import com.example.z.comments.Comment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
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
    private CollectionReference commentsRef;

    /**
     * Initializes the Firestore database and references the "users" and "moods" collections.
     */
    public DatabaseManager() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        moodsRef = db.collection("moods");
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
}

