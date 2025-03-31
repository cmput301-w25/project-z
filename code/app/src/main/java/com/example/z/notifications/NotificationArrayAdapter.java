package com.example.z.notifications;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.z.R;
import com.example.z.user.User;
import com.example.z.user.UserArrayAdapter;
import com.example.z.views.PublicProfileActivity;

import java.util.ArrayList;
import java.util.List;

public class NotificationArrayAdapter extends RecyclerView.Adapter<NotificationArrayAdapter.NotificationViewHolder>{
    private List<Notification> notificationList = new ArrayList<>();


    public NotificationArrayAdapter(String notificationList) {
        this.notificationList = notificationList;

    }
    @Override
    public NotificationArrayAdapter.NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_card, parent, false);
        return new NotificationArrayAdapter.NotificationViewHolder(view);

    }

    @Override
    public void onBindViewHolder(NotificationArrayAdapter.NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.usernameTextView.setText(notification.getUsername() );
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    // Method to update the notifications list
    public void updateNotificationList(List<Notification> notifications) {
        this.notificationList = notifications;
        notifyDataSetChanged();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.notificationText);
        }
    }
}
