package com.example.z.image;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.z.R;

/**
 * SecondActivity displays an image and allows users to navigate back to FirstActivity.
 * Clicking the ImageView returns the user to the previous screen.
 */
public class SecondActivity extends AppCompatActivity {

    private ImageView imageView;

    /**
     * Called when the activity is first created.
     * Initializes UI components and sets up a click listener for navigation.
     *
     * @param savedInstanceState A Bundle containing the activity's previously saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        // Initialize ImageView
        imageView = findViewById(R.id.imageView);

        // Set up click listener to return to FirstActivity when clicking the ImageView
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(SecondActivity.this, FirstActivity.class);
            startActivity(intent);
            finish(); // Close SecondActivity after navigation
        });
    }
}


