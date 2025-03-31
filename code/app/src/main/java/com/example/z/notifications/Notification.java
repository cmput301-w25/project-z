package com.example.z.notifications;

import java.util.Date;
/**
 * Represents a notification for a follow request.
 * Contains information about the user who sent the follow request,
 * the user who received the request, the request status,
 * and the timestamp of when the request was created.
 */
public class Notification {
    private String followedId;
    private String followerId;
    private String status;
    private String followedUsername;
    private Date createdAt;

    /**
     * Constructs a Notification with the given details.
     *
     * @param followedId    The ID of the user being followed.
     * @param followerId    The ID of the user sending the follow request.
     * @param status        The status of the follow request (e.g., "pending", "accepted").
     * @param createdAt     The date and time when the follow request was created.
     */
    public Notification(String followedId, String followerId, String status, Date createdAt){
        this.followedId = followedId;
        this.followerId = followerId;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * Default constructor for Notification.
     */
    public Notification() {}

    /**
     * Gets the ID of the user being followed.
     *
     * @return The followed user's ID.
     */
    public String getFollowedId() {
        return followedId;
    }

    /**
     * Gets the ID of the user who sent the follow request.
     *
     * @return The follower's ID.
     */
    public String getFollowerId() {
        return followerId;
    }

    /**
     * Gets the status of the follow request.
     * Example values could be "pending", "accepted", or "rejected".
     *
     * @return The status of the follow request.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Gets the creation date of the follow request.
     *
     * @return The creation date of the follow request.
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the username of the user being followed.
     *
     * @return The username of the followed user.
     */
    public String getFollowedUsername() {
        return followedUsername;
    }

    /**
     * Sets the username of the user being followed.
     *
     * @param followedUsername The username of the followed user.
     */
    public void setFollowedUsername(String followedUsername) {
        this.followedUsername = followedUsername;
    }

}
