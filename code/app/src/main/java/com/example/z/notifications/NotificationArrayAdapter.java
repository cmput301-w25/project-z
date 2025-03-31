package com.example.z.notifications;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.z.R;
import com.example.z.data.DatabaseManager;
import com.example.z.user.User;
import com.example.z.user.UserArrayAdapter;
import com.example.z.utils.OnUsernameFetchedListener;
import com.example.z.views.PublicProfileActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NotificationArrayAdapter extends RecyclerView.Adapter<NotificationArrayAdapter.NotificationViewHolder>{
    private List<Notification> notificationList = new ArrayList<>();
    private Context context;


    public NotificationArrayAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_card, parent, false);
        return new NotificationViewHolder(view);

    }

    @Override
    public void onBindViewHolder(NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        // Fetch and set username asynchronously
        DatabaseManager.getUsernameById(notification.getFollowerId(), new OnUsernameFetchedListener() {
            @Override
            public void onFetched(String username) {
                if (username != null) {
                    notification.setFollowedUsername(username);
                    holder.usernameTextView.setText(username + " sent you a follow request.");
                } else {
                    holder.usernameTextView.setText("Unknown user sent you a follow request.");
                }
            }
        });

        //holder.usernameTextView.setText(notification.getFollowedUsername() );

        // Handle Accept Button Click
        holder.acceptButton.setOnClickListener(v -> handleFollowRequest(notification, "accepted", position));

        // Handle Reject Button Click
        holder.rejectButton.setOnClickListener(v -> handleFollowRequest(notification, "rejected", position));
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

    private void handleFollowRequest(Notification notification, String action, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference requestRef = db.collection("followers")
                .document(notification.getFollowerId() + "_" + notification.getFollowedId());

        if ("accepted".equals(action)) {
            requestRef.update("status", "accepted").addOnSuccessListener(aVoid -> {
                notificationList.remove(position);
                notifyItemRemoved(position);
            });
        } else if ("rejected".equals(action)) {
            requestRef.delete().addOnSuccessListener(aVoid -> {
                notificationList.remove(position);
                notifyItemRemoved(position);
            });
        }
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        Button acceptButton, rejectButton;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.notificationText);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.declineButton);
        }
    }
}
