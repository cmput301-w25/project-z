package com.example.z.mood;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Intent;
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
import com.example.z.views.PostActivity;
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
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), PostActivity.class);
            intent.putExtra("mood", mood);
            startActivity(intent);
        }
        dismiss();
        return super.onCreateDialog(savedInstanceState);
    }
}



