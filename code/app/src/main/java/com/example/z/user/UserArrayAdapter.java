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
import com.example.z.user.SearchUserController;
import com.example.z.views.ProfileActivity;
import com.example.z.views.PublicProfileActivity;

public class UserArrayAdapter extends RecyclerView.Adapter<UserArrayAdapter.UserViewHolder> {

    private List<User> userList = new ArrayList<>();
    private SearchUserController searchUserController;
    private String currentUserId;

    // Constructor to initialize controller and userId
    public UserArrayAdapter(SearchUserController searchUserController, String currentUserId) {
        this.searchUserController = searchUserController;
        this.currentUserId = currentUserId;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card, parent, false);
        return new UserViewHolder(view);

    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.usernameTextView.setText(user.getUsername());

        // **Make the user card clickable to open ProfileActivity**
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, PublicProfileActivity.class);
            intent.putExtra("userId", user.getId());  // Pass user ID
            intent.putExtra("username", user.getUsername());  // Pass username
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // Method to update the user list
    public void updateUserList(List<User> users) {
        this.userList = users;
        notifyDataSetChanged();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;

        public UserViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameUc);
        }
    }
}
