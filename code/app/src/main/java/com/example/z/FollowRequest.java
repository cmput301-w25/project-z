package com.example.z;


/**
 * Represents a pending follow request between two users.
 */
public class FollowRequest {
    private String id;
    private String followerId;
    private String followeeId;
    private long timestamp;

    /**
     * Default constructor required for Firestore deserialization.
     */
    public FollowRequest() {}

    /**
     * Constructs a new FollowRequest object.
     *     
     * @param id  The ID of the request
     * @param followerId The ID of the user sending the follow request.
     * @param followeeId The ID of the user receiving the follow request.
     */
    public FollowRequest(String id, String followerId, String followeeId) {
        this.followerId = followerId;
        this.followeeId = followeeId;
        this.id = id;
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
