package com.example.z.mood;

import android.net.Uri;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Represents a mood entry in the application.
 * This class is used to store and retrieve mood-related data from Firestore.
 *
 *  Outstanding issues:
 *      - None
 */
public class Mood implements Serializable {
    private String userId;
    private String username;
    private String emotionalState;
    private String trigger;
    private String socialSituation;
    private Date createdAt;
    private Map<String, Object> location;
    private String description;
    private String documentId;

    private Uri imgUri;

    /**
     * Constructs a Mood object with all required fields.
     *
     * @param userId          The unique ID of the user who posted the mood.
     * @param documentId      The Firestore document ID of this mood.
     * @param username        The username of the user.
     * @param emotionalState  The emotional state (mood type) selected by the user.
     * @param trigger         The mood trigger (hashtags or reasons).
     * @param socialSituation The social situation at the time of the mood.
     * @param createdAt       The timestamp when the mood was created.
     * @param location        The optional location data associated with the mood.
     * @param description     A short description of the user's mood.
     */
    public Mood(String userId, String documentId, String username, String emotionalState, String trigger,
                String socialSituation, Date createdAt, Map<String, Object> location, String description, Uri uri) {
        this.userId = userId;
        this.documentId = documentId;
        this.username = username;
        this.emotionalState = emotionalState;
        this.trigger = trigger;
        this.socialSituation = socialSituation;
        this.createdAt = createdAt;
        this.location = location;
        this.description = description;
        this.imgUri = uri;
    }

    /**
     * Default constructor required for Firestore serialization.
     */
    public Mood() {}

    // Getters

    /**
     * Gets the user ID associated with this mood entry.
     *
     * @return The user ID.
     */
    @PropertyName("userId")
    public String getUserId() {
        return userId;
    }

    /**
     * Gets the Firestore document ID of this mood.
     *
     * @return The document ID.
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * Gets the username of the user who posted the mood.
     *
     * @return The username.
     */
    @PropertyName("username")
    public String getUsername() {
        return username;
    }

    /**
     * Gets the emotional state (mood type) selected by the user.
     *
     * @return The mood type.
     */
    @PropertyName("type")
    public String getEmotionalState() {
        return emotionalState;
    }

    /**
     * Gets the trigger (hashtags or reasons) for this mood.
     *
     * @return The mood trigger.
     */
    @PropertyName("trigger")
    public String getTrigger() {
        return trigger;
    }

    /**
     * Gets the social situation at the time the mood was posted.
     *
     * @return The social situation.
     */
    @PropertyName("situation")
    public String getSocialSituation() {
        return socialSituation;
    }

    /**
     * Gets the timestamp when the mood was created.
     *
     * @return The timestamp.
     */
    @PropertyName("timestamp")
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the location data associated with this mood.
     *
     * @return The location data (can be null).
     */
    public Map<String, Object> getLocation() {
        return location;
    }

    /**
     * Gets the description of the user's mood.
     *
     * @return The mood description.
     */
    @PropertyName("description")
    public String getDescription() {
        return description;
    }

    // Setters

    /**
     * Sets the username of the user who posted the mood.
     *
     * @param username The username.
     */
    @PropertyName("username")
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the emotional state (mood type).
     *
     * @param emotionalState The mood type.
     */
    @PropertyName("type")
    public void setEmotionalState(String emotionalState) {
        this.emotionalState = emotionalState;
    }

    /**
     * Sets the trigger (hashtags or reasons) for this mood.
     *
     * @param trigger The mood trigger.
     */
    @PropertyName("trigger")
    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    /**
     * Sets the social situation at the time the mood was posted.
     *
     * @param socialSituation The social situation.
     */
    @PropertyName("situation")
    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    /**
     * Sets the timestamp when the mood was created.
     *
     * @param createdAt The timestamp.
     */
    @PropertyName("timestamp")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Sets the location data associated with this mood.
     *
     * @param location The location data.
     */
    public void setLocation(Map<String, Object> location) {
        this.location = location;
    }

    /**
     * Sets the description of the user's mood.
     *
     * @param description The mood description.
     */
    @PropertyName("description")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the Firestore document ID for this mood.
     *
     * @param documentId The document ID.
     */
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    /**
     * Sets the user ID associated with this mood entry.
     *
     * @param userId The user ID.
     */
    @PropertyName("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }
}


