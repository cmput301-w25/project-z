package com.example.z.image;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

import com.example.z.R;

public class FirstActivity extends AppCompatActivity {

    private ImageButton btnTakePhoto, btnSelectImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_mood_event_alert_dialog);

        btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnSelectImage = findViewById(R.id.btn_upload_image);

        // Open Image.java when clicking the camera or gallery button
        btnTakePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(FirstActivity.this, Image.class);
            startActivity(intent);
        });

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(FirstActivity.this, Image.class);
            startActivity(intent);
        });
    }
}
