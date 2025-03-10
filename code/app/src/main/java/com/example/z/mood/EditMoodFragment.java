package com.example.z.mood;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.z.data.DatabaseManager;
import com.example.z.R;
import com.example.z.utils.SocialSituations;
import com.example.z.utils.userMoods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;

/**
 * Fragment for editing an existing mood entry.
 * Users can modify the details of their previously posted mood entries.
 */
public class EditMoodFragment extends DialogFragment {
    private Spinner edit_social_situation, edit_mood_emotion;
    private EditText edit_mood_description, edit_trigger;
    private Button btnSave;
    private DatabaseManager databaseManager;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String moodId;
    private OnMoodUpdatedListener moodUpdatedListener;

    /**
     * Creates a new instance of the EditMoodFragment with pre-filled data from an existing mood.
     *
     * @param mood The mood object containing details to be edited.
     * @return An instance of EditMoodFragment with pre-filled data.
     */
    public static EditMoodFragment newInstance(Mood mood) {
        EditMoodFragment fragment = new EditMoodFragment();
        Bundle args = new Bundle();
        args.putString("moodId", mood.getDocumentId());
        args.putString("description", mood.getDescription());
        args.putString("trigger", mood.getTrigger());
        args.putString("moodType", mood.getEmotionalState());
        args.putString("socialSituation", mood.getSocialSituation());
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Creates and initializes the dialog for editing a mood.
     *
     * @param savedInstanceState The saved instance state bundle.
     * @return A dialog containing the edit mood form.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.add_mood_event_alert_dialog, null);
        builder.setView(view);

        Dialog dialog = builder.create();

        // Apply rounded corners to dialog background
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_box);
        }

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        databaseManager = new DatabaseManager();

        // Bind UI components
        edit_social_situation = view.findViewById(R.id.spinner_social_situation);
        edit_mood_emotion = view.findViewById(R.id.spinner_mood);
        edit_mood_description = view.findViewById(R.id.edit_description);
        edit_trigger = view.findViewById(R.id.edit_hashtags);
        btnSave = view.findViewById(R.id.btn_post);

        // Load spinners with predefined values
        ArrayAdapter<SocialSituations> socialAdapter = new ArrayAdapter<>(
                getContext(), R.layout.custom_spinner_items, SocialSituations.values()
        );
        socialAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        edit_social_situation.setAdapter(socialAdapter);

        ArrayAdapter<userMoods> emotionalAdapter = new ArrayAdapter<>(
                getContext(), R.layout.custom_spinner_items, userMoods.values()
        );
        emotionalAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        edit_mood_emotion.setAdapter(emotionalAdapter);

        // Retrieve and pre-fill mood details if available
        if (getArguments() != null) {
            moodId = getArguments().getString("moodId");
            edit_mood_description.setText(getArguments().getString("description"));
            edit_trigger.setText(getArguments().getString("trigger"));
            btnSave.setText("Save Changes");

            String prevMood = getArguments().getString("moodType");
            String prevSocialSituation = getArguments().getString("socialSituation");

            // Set previous selection for Mood Spinner
            if (prevMood != null) {
                for (int i = 0; i < edit_mood_emotion.getCount(); i++) {
                    if (edit_mood_emotion.getItemAtPosition(i).toString().equals(prevMood)) {
                        edit_mood_emotion.setSelection(i);
                        break;
                    }
                }
            }

            // Set previous selection for Social Situation Spinner
            if (prevSocialSituation != null) {
                for (int i = 0; i < edit_social_situation.getCount(); i++) {
                    if (edit_social_situation.getItemAtPosition(i).toString().equals(prevSocialSituation)) {
                        edit_social_situation.setSelection(i);
                        break;
                    }
                }
            }
        }

        // Set save button click listener
        btnSave.setOnClickListener(v -> validateAndSaveEdit());

        return dialog;
    }

    /**
     * Validates user input and updates the mood entry in Firestore.
     */
    private void validateAndSaveEdit() {
        if (moodId == null) return;

        userMoods selectedMood = (userMoods) edit_mood_emotion.getSelectedItem();
        SocialSituations socialSituation = (SocialSituations) edit_social_situation.getSelectedItem();
        String updatedDescription = edit_mood_description.getText().toString().trim();
        String updatedTrigger = edit_trigger.getText().toString().trim();
        Date updatedAt = new Date();

        // Validate Mood Selection
        if (selectedMood == null || selectedMood.toString().equalsIgnoreCase("Select")) {
            showErrorDialog("You must tell us how you are feeling!");
            return;
        }

        // Validate Description Length
        if (updatedDescription.length() > 20) {
            showErrorDialog("Description must be 20 characters max!");
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User authentication error!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        // Update mood entry in Firestore using DatabaseManager
        databaseManager.editMood(moodId, userId, selectedMood.toString(), updatedDescription,
                socialSituation.toString(), updatedTrigger, updatedAt,
                aVoid -> {
                    if (moodUpdatedListener != null) {
                        moodUpdatedListener.onMoodUpdated();
                    }
                    Toast.makeText(getContext(), "Mood updated!", Toast.LENGTH_SHORT).show();
                    dismiss();
                },
                e -> Toast.makeText(getContext(), "Error updating mood", Toast.LENGTH_SHORT).show());
    }

    /**
     * Sets the listener to notify when a mood has been updated.
     *
     * @param listener The listener instance to be notified.
     */
    public void setMoodUpdatedListener(OnMoodUpdatedListener listener) {
        this.moodUpdatedListener = listener;
    }

    /**
     * Interface to notify when a mood has been updated.
     */
    public interface OnMoodUpdatedListener {
        /**
         * Called when a mood update is successfully completed.
         */
        void onMoodUpdated();
    }

    private void showErrorDialog(String message) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Error")
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert) // Adds a red exclamation mark icon
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}


