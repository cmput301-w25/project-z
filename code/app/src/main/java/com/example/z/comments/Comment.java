package com.example.z.comments;

import com.google.firebase.firestore.PropertyName;

/**
 * Represents a comment made on a mood post.
 * Each comment contains user information, text, timestamp, and mood details.
 */
public class Comment {
    private String userId;
    private String username;
    private String commentDetails;
    private String timestamp;
    private String emoji;
    private String moodId;
    private String emotionalState;
    private String commentId;

    /**
     * Default constructor for Firestore deserialization.
     */
    public Comment() {}

    /**
     * Constructs a Comment object with all necessary details.
     *
     * @param userId          The ID of the user who made the comment.
     * @param username        The username of the commenter.
     * @param commentDetails  The actual comment text.
     * @param timestamp       The time the comment was posted.
     * @param emoji           The user's most recent mood emoji.
     * @param moodId          The ID of the mood post the comment belongs to.
     * @param emotionalState  The emotional state associated with the comment.
     * @param commentId       The unique ID of the comment.
     */
    public Comment(String userId, String username, String commentDetails, String timestamp, String emoji, String moodId, String emotionalState, String commentId) {
        this.userId = userId;
        this.username = username;
        this.commentDetails = commentDetails;
        this.timestamp = timestamp;
        this.emoji = emoji;
        this.moodId = moodId;
        this.emotionalState = emotionalState;
        this.commentId = commentId;
    }

    /**
     * Gets the emotional state associated with the comment.
     *
     * @return The emotional state.
     */
    @PropertyName("emotionalState")
    public String getEmotionalState() {
        return emotionalState;
    }

    /**
     * Gets the unique ID of the comment.
     *
     * @return The comment ID.
     */
    @PropertyName("commentId")
    public String getCommentId() {
        return commentId;
    }

    /**
     * Gets the user ID of the commenter.
     *
     * @return The user ID.
     */
    @PropertyName("userId")
    public String getUserId() {
        return userId;
    }

    /**
     * Gets the username of the commenter.
     *
     * @return The username.
     */
    @PropertyName("username")
    public String getUsername() {
        return username;
    }

    /**
     * Gets the text of the comment.
     *
     * @return The comment text.
     */
    @PropertyName("user_comment")
    public String getCommentDetails() {
        return commentDetails;
    }

    /**
     * Gets the timestamp of when the comment was posted.
     *
     * @return The comment timestamp.
     */
    @PropertyName("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the most recent mood emoji associated with the user.
     *
     * @return The emoji representing the user's recent mood.
     */
    @PropertyName("most_recent_mood")
    public String getEmoji() {
        return emoji;
    }

    /**
     * Gets the mood ID of the post that this comment belongs to.
     *
     * @return The mood ID.
     */
    @PropertyName("mood_id")
    public String getMoodId() {
        return moodId;
    }

    /**
     * Sets the user ID of the commenter.
     *
     * @param userId The user ID to set.
     */
    @PropertyName("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Sets the emotional state associated with the comment.
     *
     * @param emotionalState The emotional state to set.
     */
    @PropertyName("emotionalState")
    public void setEmotionalState(String emotionalState) {
        this.emotionalState = emotionalState;
    }

    /**
     * Sets the username of the commenter.
     *
     * @param username The username to set.
     */
    @PropertyName("username")
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the text of the comment.
     *
     * @param commentDetails The comment text to set.
     */
    @PropertyName("user_comment")
    public void setCommentDetails(String commentDetails) {
        this.commentDetails = commentDetails;
    }

    /**
     * Sets the timestamp of when the comment was posted.
     *
     * @param timestamp The timestamp to set.
     */
    @PropertyName("timestamp")
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Sets the most recent mood emoji associated with the user.
     *
     * @param emoji The emoji to set.
     */
    @PropertyName("most_recent_mood")
    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    /**
     * Sets the mood ID of the post that this comment belongs to.
     *
     * @param moodId The mood ID to set.
     */
    @PropertyName("mood_id")
    public void setMoodId(String moodId) {
        this.moodId = moodId;
    }

    /**
     * Sets the unique ID of the comment.
     *
     * @param commentId The comment ID to set.
     */
    @PropertyName("commentId")
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
}

