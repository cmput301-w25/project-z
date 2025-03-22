package com.example.z.user;

import com.example.z.data.DatabaseManager;
import com.example.z.utils.OnUserSearchCompleteListener;

import java.util.List;

public class SearchUserController {
    private DatabaseManager dbManager;
    private OnUserSearchCompleteListener listener;

    public SearchUserController(DatabaseManager dbManager, OnUserSearchCompleteListener listener) {
        this.dbManager = dbManager;
        this.listener = listener;
    }

    public void searchUsers(String query) {
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

