package com.example.z.comments;

import com.google.firebase.firestore.PropertyName;

public class Comment {
    private String userId;
    private String username;
    private String commentDetails;
    private String timestamp;
    private String emoji;
    private String moodId;
    private String emotionalState;
    private String commentId;

    public Comment() {}

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

    @PropertyName("emotionalState")
    public String getEmotionalState() {
        return emotionalState;
    }

    @PropertyName("commentId")
    public String getCommentId() {
        return commentId;
    }

    @PropertyName("userId")
    public String getUserId() {
        return userId;
    }


    @PropertyName("username")
    public String getUsername() {
        return username;
    }

    @PropertyName("user_comment")
    public String getCommentDetails() {
        return commentDetails;
    }

    @PropertyName("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    @PropertyName("most_recent_mood")
    public String getEmoji() {
        return emoji;
    }

    @PropertyName("mood_id")
    public String getMoodId() {
        return moodId;
    }

    @PropertyName("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @PropertyName("emotionalState")
    public void setEmotionalState(String emotionalState) {
        this.emotionalState = emotionalState;
    }

    @PropertyName("username")
    public void setUsername(String username) {
        this.username = username;
    }

    @PropertyName("user_comment")
    public void setCommentDetails(String commentDetails) {
        this.commentDetails = commentDetails;
    }

    @PropertyName("timestamp")
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @PropertyName("most_recent_mood")
    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    @PropertyName("mood_id")
    public void setMoodId(String moodId) {
        this.moodId = moodId;
    }

    @PropertyName("commentId")
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
}
