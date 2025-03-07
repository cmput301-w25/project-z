package com.example.z;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class MoodFragment extends DialogFragment {
    interface MoodListener {
        void addMood(Mood mood);
    }
    private MoodListener listener;
    private String editDate;
    private String editState;
    private String editTrigger;
    private Spinner edit_social_situation;
    private Spinner edit_mood_emotion;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MoodListener) {
            listener = (MoodListener) context;
        } else {
            throw new RuntimeException(context + " must implement addMoodListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.add_mood_event_alert_dialog, null);
        edit_social_situation = view.findViewById(R.id.spinner_social_situation);
        edit_mood_emotion = view.findViewById(R.id.spinner_mood);

        ArrayAdapter<SocialSituations> SocialAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, SocialSituations.values()
        );
        SocialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        edit_social_situation.setAdapter(SocialAdapter);

        ArrayAdapter<userMoods> EmotionalAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, userMoods.values()
        );
        EmotionalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        edit_social_situation.setAdapter(EmotionalAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view);
        Button postButton = view.findViewById(R.id.btn_post);
        postButton.setOnClickListener(v -> {
            String social_situation = edit_social_situation.getSelectedItem().toString();
            String user_mood = edit_mood_emotion.getSelectedItem().toString();


            //listener.addMood(new Mood("id", "ownerID", user_mood, "trigger", social_situation, "date", "location"));
        });
        return builder.create();
    }
}
