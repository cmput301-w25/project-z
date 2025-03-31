package com.example.z.user;

import com.example.z.data.DatabaseManager;
import com.example.z.utils.OnUserSearchCompleteListener;

/**
 * Controller for managing user-related operations such as searching users.
 * Communicates with the DatabaseManager to perform actions related to user data.
 */
public class UserController {
    private DatabaseManager dbManager;


    /**
     * Constructor to initialize the UserController with the given DatabaseManager.
     *
     * @param dbManager The DatabaseManager instance for interacting with the database.
     */
    public UserController(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Searches for users based on the provided query and notifies the listener upon completion.
     * If the query is empty, it triggers the failure callback on the listener.
     *
     * @param query The search query to search for users by username.
     * @param listener The listener that handles the result of the search.
     */
    public void searchUsers(String query, OnUserSearchCompleteListener listener) {
        if (query.isEmpty()) {
            listener.onFailure(new IllegalArgumentException("Search query cannot be empty"));
            return;
        }

        dbManager.searchUsersByUsername(query, listener);
    }
}

