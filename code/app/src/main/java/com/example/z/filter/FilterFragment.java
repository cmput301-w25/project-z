package com.example.z.filter;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.z.R;

import java.util.Map;

public class FilterFragment extends DialogFragment {
    private Map<String, Boolean> FilterMoods;
    private boolean RecentMood;
    private String SearchText;
    private FilterListener listener;
    private Button btnApply;
    private Button btnClear;

    public FilterFragment(Map<String, Boolean> FilterMoods, boolean RecentMood, String SearchText, FilterListener listener) {
        this.FilterMoods = FilterMoods;
        this.RecentMood = RecentMood;
        this.SearchText = SearchText;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.filter_mood_dialog, null);
        builder.setView(view);

        btnApply = view.findViewById(R.id.btnApplyFilter);
        btnClear = view.findViewById(R.id.btnClearFilters);

        CheckBox recent = view.findViewById(R.id.checkBoxRecent);
        CheckBox happy_filter = view.findViewById(R.id.checkBoxHappiness);
        CheckBox sad_filter = view.findViewById(R.id.checkBoxSadness);
        CheckBox angry_filter = view.findViewById(R.id.checkBoxAnger);
        CheckBox confused_filter = view.findViewById(R.id.checkBoxConfusion);
        CheckBox disgust_filter = view.findViewById(R.id.checkBoxDisgust);
        CheckBox fear_filter = view.findViewById(R.id.checkBoxFear);
        CheckBox shame_filter = view.findViewById(R.id.checkBoxShame);
        CheckBox surprised_filter = view.findViewById(R.id.checkBoxSurprise);
        EditText description = view.findViewById(R.id.editTextSearch);

        recent.setChecked(RecentMood);
        happy_filter.setChecked(FilterMoods.getOrDefault("happiness", false));
        sad_filter.setChecked(FilterMoods.getOrDefault("sadness", false));
        angry_filter.setChecked(FilterMoods.getOrDefault("anger", false));
        confused_filter.setChecked(FilterMoods.getOrDefault("confusion", false));
        disgust_filter.setChecked(FilterMoods.getOrDefault("disgust", false));
        fear_filter.setChecked(FilterMoods.getOrDefault("fear", false));
        shame_filter.setChecked(FilterMoods.getOrDefault("shame", false));
        surprised_filter.setChecked(FilterMoods.getOrDefault("surprise", false));
        description.setText(SearchText.toLowerCase());


        btnApply.setOnClickListener(v -> {
            RecentMood = recent.isChecked();
            FilterMoods.put("happiness", happy_filter.isChecked());
            FilterMoods.put("sadness", sad_filter.isChecked());
            FilterMoods.put("anger", angry_filter.isChecked());
            FilterMoods.put("confusion", confused_filter.isChecked());
            FilterMoods.put("disgust", disgust_filter.isChecked());
            FilterMoods.put("fear", fear_filter.isChecked());
            FilterMoods.put("shame", shame_filter.isChecked());
            FilterMoods.put("surprise", surprised_filter.isChecked());
            SearchText = description.getText().toString();

            listener.onFilterApplied(FilterMoods, RecentMood, SearchText);
            dismiss();
        });

        btnClear.setOnClickListener(v -> {
            recent.setChecked(false);
            happy_filter.setChecked(false);
            sad_filter.setChecked(false);
            angry_filter.setChecked(false);
            confused_filter.setChecked(false);
            disgust_filter.setChecked(false);
            fear_filter.setChecked(false);
            shame_filter.setChecked(false);
            surprised_filter.setChecked(false);
            description.setText("");

            RecentMood = false;
            FilterMoods.replaceAll((m, value) -> false);
            SearchText = "";


            listener.onFilterApplied(FilterMoods, RecentMood, SearchText);
            dismiss();
        });


        return builder.create();
    }

    public interface FilterListener {
        void onFilterApplied(Map<String, Boolean> FilterMoods, boolean RecentMood, String SearchText);
    }
}


