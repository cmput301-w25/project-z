package com.example.z.utils;

import com.example.z.notifications.Notification;

import java.util.List;

/**
 * Listener interface to handle the retrieval of follow requests.
 * This is used when fetching a list of follow requests asynchronously.
 */
public interface OnFollowRequestsFetchedListener {

    /**
     * Called when follow requests are fetched successfully.
     *
     * @param followRequests The list of follow requests. Can be empty if no requests are found.
     */
    void onFetched(List<Notification> followRequests);
}
