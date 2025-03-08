package com.example.z;

import android.app.AlertDialog;
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
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MoodFragment extends DialogFragment {
    private Spinner edit_social_situation;
    private Spinner edit_mood_emotion;
    private EditText edit_mood_description;
    private EditText edit_trigger;
    private DatabaseManager databaseManager;
    private FirebaseAuth auth;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.add_mood_event_alert_dialog, null);
        edit_social_situation = view.findViewById(R.id.spinner_social_situation);
        edit_mood_emotion = view.findViewById(R.id.spinner_mood);
        edit_mood_description = view.findViewById(R.id.edit_description);
        edit_trigger = view.findViewById(R.id.edit_hashtags);

        auth = FirebaseAuth.getInstance();
        databaseManager = new DatabaseManager();

        ArrayAdapter<SocialSituations> socialAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, SocialSituations.values()
        );
        socialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        edit_social_situation.setAdapter(socialAdapter);

        ArrayAdapter<userMoods> emotionalAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, userMoods.values()
        );
        emotionalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        edit_mood_emotion.setAdapter(emotionalAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view);
        Button postButton = view.findViewById(R.id.btn_post);

        postButton.setOnClickListener(v -> saveMoodToFirebase());

        return builder.create();
    }

    private void saveMoodToFirebase() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "You need to be logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String mood = edit_mood_emotion.getSelectedItem().toString();
        String socialSituation = edit_social_situation.getSelectedItem().toString();
        String description = edit_mood_description.getText().toString();
        String trigger = edit_trigger.getText().toString();

        if (mood.equals("Select") || socialSituation.equals("Select")) {
            Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.length() > 20) {
            Toast.makeText(getContext(), "Description must be 20 characters max", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseManager.saveMood(userId, mood, description, socialSituation, trigger);

        Toast.makeText(getContext(), "Mood added successfully!", Toast.LENGTH_SHORT).show();
        dismiss();
    }
}
