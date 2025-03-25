package com.example.z.mood;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.example.z.data.DatabaseManager;
import com.example.z.R;
import com.example.z.utils.GetEmoji;
import com.example.z.utils.SocialSituations;
import com.example.z.utils.userMoods;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
 *
 *  Outstanding issues:
 *      - Cannot edit image yet
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
    private ImageButton btnEmoji;
    private String userStringEmoji;
    private boolean isPrivate;
    private SwitchCompat privacy_switch;
    private ImageButton btnDelete;
    private Mood mood;


    /**
     * Creates a new instance of the EditMoodFragment with pre-filled data from an existing mood.
     *
     * @param mood The mood object containing details to be edited.
     * @return An instance of EditMoodFragment with pre-filled data.
     */
    public static EditMoodFragment newInstance(Mood mood) {
        EditMoodFragment fragment = new EditMoodFragment();
        Bundle args = new Bundle();
        args.putSerializable("mood", mood);
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
        privacy_switch = view.findViewById(R.id.switch_privacy);
        btnSave = view.findViewById(R.id.btn_post);
        btnEmoji = view.findViewById(R.id.btn_emoji_picker);
        btnDelete = view.findViewById(R.id.btnDeletePost);

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
            mood = (Mood) getArguments().getSerializable("mood");
            if (mood != null) {
                moodId = mood.getDocumentId();
                edit_mood_description.setText(mood.getDescription());
                edit_trigger.setText(mood.getTrigger());
                userStringEmoji = mood.getEmoticon();
                isPrivate = mood.isPrivate();
                btnSave.setText("Save Changes");

                String prevMood = mood.getEmotionalState();
                String prevSocialSituation = mood.getSocialSituation();

                privacy_switch.setChecked(isPrivate);

                if (userStringEmoji != null) {
                    int emojiId = GetEmoji.getEmojiPosition(userStringEmoji);
                    if (emojiId != 0) {
                        btnEmoji.setImageResource(emojiId);
                    }
                }

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
        }

        // Show Delete button only if the logged-in user owns this post
        String currentUserId = auth.getCurrentUser().getUid();
        if (!mood.getUserId().equals(currentUserId)) {
            btnDelete.setVisibility(View.GONE);
        } else {
            btnDelete.setVisibility(View.VISIBLE);
        }

        // Handle Delete Button click event
        btnDelete.setOnClickListener(v -> deleteMood());

        btnEmoji.setOnClickListener(v -> displayEmojiView());
        // Set save button click listener
        btnSave.setOnClickListener(v -> validateAndSaveEdit());

        return dialog;
    }

    private void displayEmojiView() {

        BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.emoticon_picker, null);
        bottomSheet.setContentView(view);

        GridView emojiView = view.findViewById(R.id.emojiView);
        int[] emojis = GetEmoji.getEmojiList();

        EmojiAdapter adapter = new EmojiAdapter(getContext(), emojis);
        emojiView.setAdapter(adapter);

        emojiView.setOnItemClickListener(((parent, view1, position, id) -> {
            userStringEmoji = getResources().getResourceEntryName(emojis[position]);
            btnEmoji.setImageResource(emojis[position]);
            bottomSheet.dismiss();
        }));
        bottomSheet.show();
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
        isPrivate = privacy_switch.isChecked();

        // Validate Mood Selection
        if (selectedMood == null || selectedMood.toString().equalsIgnoreCase("Select")) {
            showErrorDialog("You must tell us how you are feeling!");
            return;
        }

        // Validate Description Length
        if (updatedDescription.length() > 200) {
            showErrorDialog("Description must be 200 characters max!");
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
                socialSituation.toString(), updatedTrigger, updatedAt, userStringEmoji, isPrivate,
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


