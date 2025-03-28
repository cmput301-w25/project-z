package com.example.z.mood;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.z.data.DatabaseManager;
import com.example.z.R;
import com.example.z.utils.SocialSituations;
import com.example.z.utils.userMoods;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;




/**
 * MoodFragment is a DialogFragment that allows users to add a new mood post.
 * Users must provide an emotional state, description, and optionally a social situation and trigger.
 * The mood is saved to Firestore and updates the UI accordingly.
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
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;

    private Double latitude = null;
    private Double longitude = null;

    private boolean locationRequested = false;

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        edit_social_situation = view.findViewById(R.id.spinner_social_situation);
        edit_mood_emotion = view.findViewById(R.id.spinner_mood);
        edit_mood_description = view.findViewById(R.id.edit_description);
        edit_trigger = view.findViewById(R.id.edit_hashtags);

        ImageButton btnAttachLocation = view.findViewById(R.id.btn_attach_location);
        btnAttachLocation.setOnClickListener(v -> requestUserLocation());

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

        // Set up button listener
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
                        validateInputsAndSave(userId, username);
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
    private void validateInputsAndSave(String userId, String username) {
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
        if (description.length() > 20) {
            showErrorDialog("Description must be 20 characters max!");
            return;
        }



        DocumentReference moodDocRef = db.collection("moods").document();
        String documentId = moodDocRef.getId();

        if (currentLocation != null) {
            latitude = currentLocation.getLatitude();
            longitude = currentLocation.getLongitude();
        } else {
            latitude = null;
            longitude = null;
        }

        if (latitude == null || longitude == null) {
            if (currentLocation != null) {
                latitude = currentLocation.getLatitude();
                longitude = currentLocation.getLongitude();
            } else if (locationRequested) {
                Toast.makeText(getContext(), "Waiting for location to be retrieved...", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Mood newMood = new Mood(userId, documentId, username, selectedMood.toString(), trigger, socialSituation.toString(), createdAt, null, description);

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
        databaseManager.saveMood(mood.getUserId(), moodDocRef, mood.getUsername(), mood.getEmotionalState(), mood.getDescription(), mood.getSocialSituation(), mood.getTrigger(), mood.getCreatedAt(), latitude, longitude);

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
                .setIcon(android.R.drawable.ic_dialog_alert) // Adds a red exclamation mark icon
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }



    private void requestUserLocation() {
        locationRequested = true;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            currentLocation = location;
                            Toast.makeText(getContext(), "Location attached!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }



}

