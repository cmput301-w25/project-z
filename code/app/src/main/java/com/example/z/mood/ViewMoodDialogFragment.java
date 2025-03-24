package com.example.z.mood;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.z.R;
import com.example.z.utils.ImgUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A dialog fragment that displays details of a selected mood post.
 * Allows the user to view mood details and delete the post if they are the owner.
 *
 *  Outstanding issues:
 *      - Cannot display image
 */
public class ViewMoodDialogFragment extends DialogFragment {
    private Mood mood;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    /**
     * Constructor to initialize the fragment with the selected mood.
     *
     * @param mood The mood object containing post details.
     */
    public ViewMoodDialogFragment(Mood mood) {
        this.mood = mood; // Pass the selected mood to this dialog
    }

    /**
     * Called to create the dialog for displaying the mood details.
     *
     * @param savedInstanceState The last saved instance state of the Fragment, or null if it's a new instance.
     * @return A configured AlertDialog displaying the mood details.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.mood_event_dialog, null);
        builder.setView(view);

        Dialog dialog = builder.create();

        // Apply rounded corners to the dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_box);
        }

        // Initialize Firebase Authentication and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get references to UI elements
        TextView tvMoodUser = view.findViewById(R.id.tvMoodUser);
        TextView tvMoodDescription = view.findViewById(R.id.tvMoodDescription);
        TextView tvMoodTags = view.findViewById(R.id.tvMoodTags);
        TextView tvMoodDate = view.findViewById(R.id.tvMoodDate);
        ImageView imgMoodPost = view.findViewById(R.id.imgMoodPost);
        Button btnBack = view.findViewById(R.id.btnBack);
        ImageButton btnDelete = view.findViewById(R.id.btnDeletePost);

        // Populate fields with mood data
        tvMoodUser.setText(String.format("%s is feeling %s %s", mood.getUsername(), mood.getEmotionalState(), mood.getSocialSituation()));
        tvMoodDescription.setText(mood.getDescription());
        tvMoodTags.setText(String.format("#%s", mood.getTrigger()));

        // Format and display the mood's timestamp
        tvMoodDate.setText(new java.text.SimpleDateFormat("MMM dd, yyyy - HH:mm").format(mood.getCreatedAt()));

        // Hide or show image placeholder (Image upload not implemented yet)
        if (mood.getImg() != null) {
            imgMoodPost.setVisibility(View.VISIBLE);
            ImgUtil.displayBase64Image(mood.getImg(),imgMoodPost);
//            Glide.with(getContext()).load(mood.getUri().toString()).into(imgMoodPost);
        } else {
            imgMoodPost.setVisibility(View.GONE);
        }

        // Show Delete button only if the logged-in user owns this post
        String currentUserId = auth.getCurrentUser().getUid();
        if (!mood.getUserId().equals(currentUserId)) {
            btnDelete.setVisibility(View.GONE);
        } else {
            btnDelete.setVisibility(View.VISIBLE);
        }

        // Handle Back Button click event
        btnBack.setOnClickListener(v -> dismiss());

        // Handle Delete Button click event
        btnDelete.setOnClickListener(v -> deleteMood());

        return dialog;
    }

    /**
     * Deletes the mood post from Firestore if the user is the owner.
     * Displays a toast message indicating success or failure.
     */
    private void deleteMood() {
        db.collection("moods").document(mood.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Post deleted", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete post", Toast.LENGTH_SHORT).show());
    }
}



