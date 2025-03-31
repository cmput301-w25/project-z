package com.example.z.utils;

import com.example.z.notifications.Notification;

import java.util.List;

public interface OnFollowRequestsFetchedListener {
    void onFetched(List<Notification> followRequests);
}
