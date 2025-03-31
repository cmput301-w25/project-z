package com.example.z.user;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.z.R;
import com.example.z.user.UserController;
import com.example.z.views.ProfileActivity;
import com.example.z.views.PublicProfileActivity;

/**
 * Adapter class for displaying a list of users in a RecyclerView.
 * This adapter binds a list of `User` objects to the RecyclerView and provides functionality
 * for navigating to a public profile when a user card is clicked.
 */
public class UserArrayAdapter extends RecyclerView.Adapter<UserArrayAdapter.UserViewHolder> {

    private List<User> userList = new ArrayList<>();
    private UserController userController;
    private String currentUserId;

    /**
     * Constructor to initialize the adapter with a UserController and current user ID.
     *
     * @param userController The controller used for handling user-related operations.
     * @param currentUserId The ID of the current logged-in user.
     */
    public UserArrayAdapter(UserController userController, String currentUserId) {
        this.userController = userController;
        this.currentUserId = currentUserId;
    }

    /**
     * Creates a new ViewHolder for the user card layout.
     *
     * @param parent The parent view group for the ViewHolder.
     * @param viewType The view type of the item.
     * @return A new `UserViewHolder` instance.
     */
    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card, parent, false);
        return new UserViewHolder(view);

    }

    /**
     * Binds a user to the corresponding ViewHolder and sets the click listener.
     *
     * @param holder The ViewHolder to bind the user to.
     * @param position The position of the user in the list.
     */
    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.usernameTextView.setText(user.getUsername());

        // Make the user card clickable to open ProfileActivity
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, PublicProfileActivity.class);
            intent.putExtra("userId", user.getId());  // Pass user ID
            intent.putExtra("username", user.getUsername());  // Pass username
            context.startActivity(intent);
        });
    }

    /**
     * Returns the total number of users in the list.
     *
     * @return The size of the user list.
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * Updates the list of users and notifies the adapter to refresh the view.
     *
     * @param users The new list of users to display.
     */
    public void updateUserList(List<User> users) {
        this.userList = users;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for the user card layout.
     * Holds references to the views in each user item.
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;

        /**
         * Constructor to initialize the ViewHolder with references to the views.
         *
         * @param itemView The item view for the user card.
         */
        public UserViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameUc);
        }
    }
}
