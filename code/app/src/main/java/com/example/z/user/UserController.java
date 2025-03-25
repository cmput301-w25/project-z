package com.example.z.user;

import com.example.z.data.DatabaseManager;
import com.example.z.utils.OnUserSearchCompleteListener;

public class UserController {
    private DatabaseManager dbManager;

    public UserController(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void searchUsers(String query, OnUserSearchCompleteListener listener) {
        if (query.isEmpty()) {
            listener.onFailure(new IllegalArgumentException("Search query cannot be empty"));
            return;
        }

        dbManager.searchUsersByUsername(query, listener);
    }

    public void requestToFollow(String currentUserId, String targetUserId) {
        dbManager.requestToFollow(currentUserId, targetUserId);
    }
}

