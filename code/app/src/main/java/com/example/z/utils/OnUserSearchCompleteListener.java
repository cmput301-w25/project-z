package com.example.z.utils;

import com.example.z.user.User;
import java.util.List;

/**
 * Listener interface for handling the results of a user search.
 * This interface defines methods that are triggered upon the completion
 * of a user search operation (either successful or failed).
 */
public interface OnUserSearchCompleteListener {

    /**
     * Called when the user search is successful.
     *
     * @param users A list of users that match the search query.
     */
    void onSuccess(List<User> users);

    /**
     * Called when the user search fails.
     *
     * @param e The exception that caused the failure.
     */
    void onFailure(Exception e);
}