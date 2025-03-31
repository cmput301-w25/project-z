package com.example.z.mood;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.example.z.data.DatabaseManager;
import com.example.z.R;
import com.example.z.utils.ImgUtil;
import com.example.z.utils.GetEmoji;
import com.example.z.utils.SocialSituations;
import com.example.z.utils.userMoods;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * MoodFragment is a DialogFragment that allows users to add a new mood post.
 * Users must provide an emotional state, description, and optionally a social situation and trigger.
 * The mood is saved to Firestore and updates the UI accordingly.
 *
 *  Outstanding issues:
 *      - None
 */
public class MoodFragment extends DialogFragment {
    private Spinner edit_social_situation;
    private Spinner edit_mood_emotion;
    private EditText edit_mood_description;
    private EditText edit_trigger;
    private ImageButton btn_take_photo, btn_select_image;
    private ImageView imageView;
    private DatabaseManager databaseManager;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private OnMoodAddedListener moodAddedListener;
    private Uri imgUri;
    private String img;
    private String imgPath;
    private String userStringEmoji;
    private ImageButton btnEmoji;
    private SwitchCompat privateSwitch;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;

    private Double latitude = null;
    private Double longitude = null;

    private boolean locationRequested = false;

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private static final int PICK_IMAGE = 1;
    private static final int TAKE_PHOTO = 2;
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
        btn_take_photo = view.findViewById(R.id.btn_take_photo);
        btn_select_image = view.findViewById(R.id.btn_upload_image);
        imageView = view.findViewById(R.id.image_view);

        btnEmoji = view.findViewById(R.id.btn_emoji_picker);
        privateSwitch = view.findViewById(R.id.switch_privacy);

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

        // Set up button listeners
        btnEmoji.setOnClickListener(v -> displayEmojiView());
        postButton.setOnClickListener(v -> validateAndPostMood());
        btn_take_photo.setOnClickListener(v -> capturePhoto());
        btn_select_image.setOnClickListener(v -> openGallery());

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
     * Opens an emoji picker and allows the user to select an emoji.
     */
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

        Map<String, Object> location = new HashMap<>();
        location.put("latitude", latitude);
        location.put("longitude", longitude);

        Mood newMood = new Mood(userId, documentId, username, selectedMood.toString(), trigger, socialSituation.toString(), createdAt, location, description, img, userStringEmoji, isPrivate);

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

        databaseManager.saveMood(mood.getUserId(), moodDocRef, mood.getUsername(), mood.getEmotionalState(), mood.getDescription(), mood.getSocialSituation(), mood.getTrigger(), mood.getCreatedAt(), img, latitude, longitude, mood.getEmoticon(), mood.isPrivate());

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

    /**
     * Handles location requests, image selection from the gallery, and capturing photos.
     */
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

    /**
     * Opens the device's gallery to allow the user to select an image.
     * The selected image's URI will be retrieved.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    /**
     * Captures a photo using the device's camera and saves it as a file.
     * If the camera is available, it will request the necessary permissions.
     * The captured photo's URI will be retrieved.
     */
    private void capturePhoto() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            } else {
                File photoFile = createImageFile();
                imgUri = FileProvider.getUriForFile(getContext(),
                        "com.example.z", photoFile);
                imgPath = photoFile.getAbsolutePath();
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
                startActivityForResult(cameraIntent, TAKE_PHOTO);
            }
        } else {
            Toast.makeText(getActivity(), "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the result from an activity launched via {@code startActivityForResult}.
     * This method processes the selected or captured image.
     *
     * @param requestCode The request code passed when starting the activity.
     * @param resultCode  The result code returned from the activity.
     * @param data        The intent containing result data (if available).
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            imgUri = data.getData();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    imgPath = ImgUtil.createTempFile(getContext(), imgUri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        try {
            img = ImgUtil.compressToBase64(imgPath);
            ImgUtil.displayBase64Image(img, imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a temporary image file for storing a captured photo.
     *
     * @return The created file, or null if an error occurs.
     */
    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile(fileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Handles permission request results, specifically for camera access.
     *
     * @param requestCode  The request code associated with the permission request.
     * @param permissions  The requested permissions.
     * @param grantResults The results for each requested permission.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, TAKE_PHOTO);
            } else {
                Toast.makeText(getActivity(), "Camera not granted!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

