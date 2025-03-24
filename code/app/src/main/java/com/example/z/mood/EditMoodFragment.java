package com.example.z.mood;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.z.data.DatabaseManager;
import com.example.z.R;
import com.example.z.image.Image;
import com.example.z.utils.SocialSituations;
import com.example.z.utils.userMoods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
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
    private ImageButton btn_take_photo, btn_select_image;

    private ImageView imageView;
    private DatabaseManager databaseManager;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String moodId;
    private Uri uri;
    private OnMoodUpdatedListener moodUpdatedListener;

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private static final int PICK_IMAGE = 1;
    private static final int TAKE_PHOTO = 2;

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
        if (mood.getUri() != null) {
            args.putString("uri", mood.getUri().toString());
        }
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
        btn_take_photo = view.findViewById(R.id.btn_take_photo);
        btn_select_image = view.findViewById(R.id.btn_upload_image);
        imageView = view.findViewById(R.id.image_view);

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

        btn_take_photo.setOnClickListener(v -> capturePhoto());
        btn_select_image.setOnClickListener(v -> openGallery());

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

            if (getArguments().getString("uri") != null) {
                uri = Uri.parse(getArguments().getString("uri"));
                Glide.with(getContext()).load(uri.toString()).into(imageView);
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
                socialSituation.toString(), updatedTrigger, updatedAt, uri,
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            uri = data.getData();
        }
        if (uri != null) {
            Glide.with(getContext()).load(uri.toString()).into(imageView);
        }
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    private void capturePhoto() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            } else {
                File photoFile = createImageFile();
                uri = FileProvider.getUriForFile(getContext(),
                        "com.example.z", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(cameraIntent, TAKE_PHOTO);
            }
        } else {
            Toast.makeText(getActivity(), "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, TAKE_PHOTO);
            } else {
                // 用户拒绝权限，提示或关闭功能
                Toast.makeText(getActivity(), "Camera not granted!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


