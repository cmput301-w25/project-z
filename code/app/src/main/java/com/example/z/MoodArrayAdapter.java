package com.example.z;

import android.content.Context;
import android.graphics.Movie;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class MoodArrayAdapter extends ArrayAdapter<Mood> {
    public MoodArrayAdapter(Context context, ArrayList<Mood> moods) {
        super(context, 0, moods);
    }


}
