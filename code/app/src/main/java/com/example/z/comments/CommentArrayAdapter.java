package com.example.z.comments;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.z.R;
import com.example.z.utils.GetEmoji;
import com.example.z.utils.GetEmojiColor;
import com.example.z.views.PostActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import javax.annotation.Nonnull;

public class CommentArrayAdapter extends RecyclerView.Adapter<CommentArrayAdapter.CommentViewHolder> {

    private List<Comment> userComments;
    private Context context;

    public CommentArrayAdapter(Context context, List<Comment> userComments) {
        this.context = context;
        this.userComments = userComments;
    }

    @Nonnull
    @Override
    public CommentViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_card, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@Nonnull CommentViewHolder holder, int position) {
        Comment comment = userComments.get(position);
        holder.username.setText(comment.getUsername());
        holder.commentDetails.setText(comment.getCommentDetails());
        holder.timestamp.setText(comment.getTimestamp());

        if (comment.getEmoji() != null) {
            int emojiPosition = GetEmoji.getEmojiPosition(comment.getEmoji());
            int getEmojiColor = (comment.getEmotionalState() != null)
                    ? GetEmojiColor.getEmojiColor(comment.getEmotionalState())
                    : 0;
            if (emojiPosition != 0) {
                holder.emoji.setImageResource(emojiPosition);
                holder.emoji.setVisibility(View.VISIBLE);

                if (getEmojiColor != 0) {
                    holder.emoji.setColorFilter(getEmojiColor, PorterDuff.Mode.SRC_IN);
                }
                else {
                    holder.emoji.clearColorFilter();
                }
            }
            else {
                holder.emoji.setVisibility(View.GONE);
            }
        }
        else {
            holder.emoji.setVisibility(View.GONE);
        }

        holder.itemView.setOnLongClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && comment.getUserId().equals(user.getUid())) {
                displayDeleteDialog(comment, position);
            }
            else {
                Toast.makeText(context, "This comment does not belong to you!", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return userComments.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView commentDetails;
        TextView timestamp;
        ImageView emoji;

        public CommentViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.commentUsername);
            commentDetails = itemView.findViewById(R.id.commentDetails);
            timestamp = itemView.findViewById(R.id.commentTimestamp);
            emoji = itemView.findViewById(R.id.mostRecentMoodComment);
        }
    }

    private void displayDeleteDialog(Comment comment, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Comment")
                .setMessage("Confirm Delete")
                .setPositiveButton("Delete", (dialog, which) -> deleteComment(comment, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteComment(Comment comment, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("comments").document(comment.getCommentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (context instanceof PostActivity) {
                        ((PostActivity) context).loadOldComments();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Could not delete comment!", Toast.LENGTH_SHORT).show());
    }
}
