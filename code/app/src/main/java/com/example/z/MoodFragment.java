package com.example.z;

import android.app.AlertDialog;
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
import androidx.fragment.app.DialogFragment;

import java.util.Date;

public class MoodFragment extends DialogFragment {
    interface MoodListener {
        void addMood(Mood mood);
    }
    private MoodListener listener;
    private String editDate;
    private String editState;
    private String editTrigger;

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
        Spinner edit_social_situation = view.findViewById(R.id.spinner_social_situation);
        Spinner edit_mood_emotion = view.findViewById(R.id.spinner_mood);
        EditText edit_mood_description = view.findViewById(R.id.edit_description);
        EditText edit_trigger = view.findViewById(R.id.edit_hashtags);


        ArrayAdapter<SocialSituations> SocialAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, SocialSituations.values()
        );
        SocialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        edit_social_situation.setAdapter(SocialAdapter);

        ArrayAdapter<userMoods> EmotionalAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, userMoods.values()
        );
        EmotionalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        edit_mood_emotion.setAdapter(EmotionalAdapter);



        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view);
        Button postButton = view.findViewById(R.id.btn_post);
        postButton.setOnClickListener(v -> {
            String social_situation = edit_social_situation.getSelectedItem().toString();
            String user_mood = edit_mood_emotion.getSelectedItem().toString();
            String description = edit_mood_description.getText().toString();
            String trigger = edit_trigger.getText().toString();
            Date current_date = new Date();
            Map location = new Map();

            if (social_situation.equals("Select")) {social_situation = null;}

            if (user_mood.equals("Select")) {
                Toast.makeText(getContext(), "Please Fill Out Necessary Boxes", Toast.LENGTH_SHORT).show();
            }

            if (description.length() > 20) {
                Toast.makeText(getContext(), "Maximum Letters For Description is 20", Toast.LENGTH_SHORT).show();
            }else {
                String[] words = user_mood.split(" ");
                if (words.length > 3) {
                    Toast.makeText(getContext(), "Maximum Words For Description is 3", Toast.LENGTH_SHORT).show();
                }
            }
            //listener.addMood(new Mood("id", "ownerID", user_mood, trigger, social_situation, current_date, location, description));
        });
        return builder.create();
    }
}
