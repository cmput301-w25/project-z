package com.example.z.mood;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;


/**
 * MoodFragment is a DialogFragment that allows users to add a new mood post.
 * Users must provide an emotional state, description, and optionally a social situation and trigger.
 * The mood is saved to Firestore and updates the UI accordingly.
 *
 *  Outstanding issues:
 *      - Cannot add picture/image
 */
public class MoodFragment extends DialogFragment {
    private Spinner edit_social_situation;
    private Spinner edit_mood_emotion;
    private EditText edit_mood_description;
    private EditText edit_trigger;
    private DatabaseManager databaseManager;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private OnMoodAddedListener moodAddedListener;
    private String userStringEmoji;
    private ImageButton btnEmoji;
    private SwitchCompat privateSwitch;

    /**
     * Called to create the dialog for adding a new mood post.
     *
     * @param savedInstanceState The last saved instance state of the Fragment, or null if it's a new instance.
     * @return A configured AlertDialog for mood entry.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.add_mood_event_alert_dialog, null);
        edit_social_situation = view.findViewById(R.id.spinner_social_situation);
        edit_mood_emotion = view.findViewById(R.id.spinner_mood);
        edit_mood_description = view.findViewById(R.id.edit_description);
        edit_trigger = view.findViewById(R.id.edit_hashtags);

        btnEmoji = view.findViewById(R.id.btn_emoji_picker);
        privateSwitch = view.findViewById(R.id.switch_privacy);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        databaseManager = new DatabaseManager();

        // Set up spinners with custom adapters
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

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view);
        Button postButton = view.findViewById(R.id.btn_post);
        Dialog dialog = builder.create();

        // Apply rounded corners to the dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_box);
        }

        // Set up button listeners
        btnEmoji.setOnClickListener(v -> displayEmojiView());
        postButton.setOnClickListener(v -> validateAndPostMood());

        return dialog;
    }

    /**
     * Sets the listener for when a mood is successfully added.
     *
     * @param listener The listener to notify UI updates.
     */
    public void setMoodAddedListener(OnMoodAddedListener listener) {
        this.moodAddedListener = listener;
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
     * Validates user authentication and retrieves the username before posting the mood.
     */
    private void validateAndPostMood() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "You need to be logged in!", Toast.LENGTH_SHORT).show();
            return;
        }


        String userId = user.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("username")) {
                        String username = documentSnapshot.getString("username");
                        boolean isPrivate = privateSwitch.isChecked();
                        validateInputsAndSave(userId, username, userStringEmoji, isPrivate);
                    } else {
                        Toast.makeText(getContext(), "Username not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching username", e);
                    Toast.makeText(getContext(), "Failed to fetch username", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Validates user inputs, creates a new Mood object, and saves it to Firestore.
     *
     * @param userId   The Firebase user ID.
     * @param username The username associated with the user.
     */
    private void validateInputsAndSave(String userId, String username, String userStringEmoji, boolean isPrivate) {
        userMoods selectedMood = (userMoods) edit_mood_emotion.getSelectedItem();
        SocialSituations socialSituation = (SocialSituations) edit_social_situation.getSelectedItem();
        String description = edit_mood_description.getText().toString().trim();
        String trigger = edit_trigger.getText().toString().trim();
        Date createdAt = new Date();

        // Mood Validation
        if (selectedMood == null || selectedMood.toString().equalsIgnoreCase("Select")) {
            showErrorDialog("You must tell us how you are feeling!");
            return;
        }

        // Description Validation
        if (description.length() > 200) {
            showErrorDialog("Description must be 200 characters max!");
            return;
        }

        DocumentReference moodDocRef = db.collection("moods").document();
        String documentId = moodDocRef.getId();

        Mood newMood = new Mood(userId, documentId, username, selectedMood.toString(), trigger, socialSituation.toString(), createdAt, null, description, userStringEmoji, isPrivate);

        // Save Mood
        saveMoodToFirebase(moodDocRef, newMood);
    }

    /**
     * Saves the mood entry to Firestore and notifies the listener for UI updates.
     *
     * @param moodDocRef The Firestore document reference.
     * @param mood       The Mood object containing user input.
     */
    private void saveMoodToFirebase(DocumentReference moodDocRef, Mood mood) {
        databaseManager.saveMood(mood.getUserId(), moodDocRef, mood.getUsername(), mood.getEmotionalState(), mood.getDescription(), mood.getSocialSituation(), mood.getTrigger(), mood.getCreatedAt(), mood.getEmoticon(), mood.isPrivate());

        // Notify UI & Close Dialog
        if (moodAddedListener != null) {
            moodAddedListener.onMoodAdded(mood);
        }
        Toast.makeText(getContext(), "Mood added successfully!", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    /**
     * Interface for notifying when a new mood is added.
     */
    public interface OnMoodAddedListener {
        /**
         * Called when a mood has been successfully added.
         *
         * @param newMood The newly added mood object.
         */
        void onMoodAdded(Mood newMood);
    }

    /**
     * Shows a built-in error AlertDialog.
     */
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Error")
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert) // Adds an alert dialog for errors
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}

