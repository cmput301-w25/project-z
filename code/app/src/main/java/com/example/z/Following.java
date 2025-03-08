package com.example.z;

import java.io.Serializable;

/**
 * Represents an accepted follow relationship between two users.
 */
public class Following {
    private String followerId;
    private String followeeId;
    private long timestamp;

    /**
     * Default constructor required for Firestore deserialization.
     */
    public Following() {}

    /**
     * Constructs a new Following object.
     *
     * @param followerId The ID of the user who follows another user.
     * @param followeeId The ID of the user being followed.
     * @param timestamp  The time when the follow request was accepted.
     */
    public Following(String followerId, String followeeId, long timestamp) {
        this.followerId = followerId;
        this.followeeId = followeeId;
        this.timestamp = timestamp;
    }

    public String getFollowerId() {
        return followerId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }

    public String getFolloweeId() {
        return followeeId;
    }

    public void setFolloweeId(String followeeId) {
        this.followeeId = followeeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

