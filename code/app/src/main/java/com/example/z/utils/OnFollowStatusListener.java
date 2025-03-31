package com.example.z.utils;

/**
 * Listener interface for retrieving the follow status of a user.
 * Implement this interface to handle the retrieval of follow status.
 */
public interface OnFollowStatusListener {
    /**
     * Called when the follow status is retrieved.
     *
     * @param status The follow status, which could be "pending", "accepted", or "rejected".
     */
    void onFollowStatusRetrieved(String status);
}