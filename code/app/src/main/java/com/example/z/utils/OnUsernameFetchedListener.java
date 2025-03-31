package com.example.z.utils;

/**
 * Listener interface to handle the retrieval of a username from a database.
 * This is used when fetching a username asynchronously.
 */
public interface OnUsernameFetchedListener {

    /**
     * Called when a username is fetched successfully.
     *
     * @param username The fetched username. Can be null if the username could not be fetched.
     */
    void onFetched(String username);
}