package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class SearchActivity extends AppCompatActivity{

    private EditText SearchBar;
    private Button SearchButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout."xml file");

        SearchBar = findViewById(R.id.search_bar);
        SearchButton = findViewById(R.id.search_button);

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = SearchBar.getText().toString();
            }
        });
    }
}
