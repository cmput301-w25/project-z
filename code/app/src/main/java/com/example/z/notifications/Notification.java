package com.example.z.notifications;

import java.util.Date;

public class Notification {
    private String followedId;
    private String followerId;
    private String status;
    private String followedUsername;
    private Date createdAt;

    public Notification(String followedId, String followerId, String status, Date createdAt){
        this.followedId = followedId;
        this.followerId = followerId;
        this.status = status;
        this.createdAt = createdAt;
        // do query to get username of followedId user and store it to display
        this.followedUsername = status;

    }

    public Notification() {}


    public String getFollowedId() {
        return followedId;
    }

    public String getFollowerId() {
        return followerId;
    }

    public String getStatus() {
        return status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getFollowedUsername() {
        return followedUsername;
    }

    public void setFollowedUsername(String followedUsername) {
        this.followedUsername = followedUsername;
    }

}
