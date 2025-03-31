package com.example.z.image;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

import com.example.z.R;

/**
 * FirstActivity allows users to either take a photo or select an image from their gallery.
 * When a button is clicked, it opens the Image activity.
 */
public class FirstActivity extends AppCompatActivity {

    private ImageButton btnTakePhoto, btnSelectImage;

    /**
     * Called when the activity is first created.
     * Initializes UI components and sets up click listeners.
     *
     * @param savedInstanceState A Bundle containing the activity's previously saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_mood_event_alert_dialog);

        // Initialize buttons
        btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnSelectImage = findViewById(R.id.btn_upload_image);

        // Set click listener to open Image activity when "Take Photo" button is clicked
        btnTakePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(FirstActivity.this, Image.class);
            startActivity(intent);
        });

        // Set click listener to open Image activity when "Upload Image" button is clicked
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(FirstActivity.this, Image.class);
            startActivity(intent);
        });
    }
}

