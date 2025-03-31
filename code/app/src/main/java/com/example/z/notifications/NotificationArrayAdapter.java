package com.example.z.notifications;


import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.z.R;
import com.example.z.data.DatabaseManager;
import com.example.z.mood.Mood;
import com.example.z.user.User;
import com.example.z.user.UserArrayAdapter;
import com.example.z.utils.GetEmoji;
import com.example.z.utils.GetEmojiColor;
import com.example.z.utils.OnUsernameFetchedListener;
import com.example.z.views.PublicProfileActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying notification items in a RecyclerView.
 * Handles displaying follow requests and actions (accept/reject).
 */
public class NotificationArrayAdapter extends RecyclerView.Adapter<NotificationArrayAdapter.NotificationViewHolder>{
    private List<Notification> notificationList = new ArrayList<>();
    private Context context;

    /**
     * Constructs a NotificationArrayAdapter with the provided context and list of notifications.
     *
     * @param context           The context where the adapter will be used.
     * @param notificationList The list of notifications to be displayed.
     */
    public NotificationArrayAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    /**
     * Creates a new ViewHolder for the notification items.
     *
     * @param parent   The parent ViewGroup where the item will be placed.
     * @param viewType The type of view to be created (used for different layouts).
     * @return A new instance of NotificationViewHolder.
     */
    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_card, parent, false);
        return new NotificationViewHolder(view);

    }

    /**
     * Binds data to the ViewHolder. Specifically, sets the username and handles button clicks for accepting/rejecting follow requests.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the item in the dataset.
     */
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

        getMood(notification.getFollowerId(), holder);

        // Handle Accept Button Click
        holder.acceptButton.setOnClickListener(v -> handleFollowRequest(notification, "accepted", position));

        // Handle Reject Button Click
        holder.rejectButton.setOnClickListener(v -> handleFollowRequest(notification, "rejected", position));
    }

    /**
     * Returns the number of notifications in the list.
     *
     * @return The size of the notification list.
     */
    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    private void getMood(String userId, NotificationViewHolder holder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("moods")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot moodDoc = queryDocumentSnapshots.getDocuments().get(0);
                        Mood mood = moodDoc.toObject(Mood.class);
                        if (mood != null) {
                            int getEmoji = GetEmoji.getEmojiPosition(mood.getEmoticon());
                            int emojiColour = GetEmojiColor.getEmojiColor(mood.getEmotionalState());

                            holder.emoji.setImageResource(getEmoji);
                            holder.emoji.setColorFilter(emojiColour, PorterDuff.Mode.SRC_IN);
                            holder.emoji.setVisibility(View.VISIBLE);
                        }
                    }
                    else {
                        holder.emoji.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> holder.emoji.setVisibility(View.GONE));
    }

    /**
     * Updates the list of notifications and notifies the adapter that the data set has changed.
     *
     * @param notifications The new list of notifications to display.
     */
    public void updateNotificationList(List<Notification> notifications) {
        this.notificationList = notifications;
        notifyDataSetChanged();
    }

    /**
     * Handles accepting or rejecting a follow request by updating or deleting the request in Firestore.
     *
     * @param notification The notification representing the follow request.
     * @param action       The action to be taken ("accepted" or "rejected").
     * @param position     The position of the notification in the list.
     */
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

    /**
     * ViewHolder for displaying each notification item in the RecyclerView.
     */
    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        Button acceptButton, rejectButton;
        ImageView emoji;

        /**
         * Constructs a NotificationViewHolder and initializes the view components.
         *
         * @param itemView The view representing a notification item.
         */
        public NotificationViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.notificationText);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.declineButton);
            emoji = itemView.findViewById(R.id.profilePicture);
        }
    }
}
