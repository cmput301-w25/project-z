package com.example.z.image;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.z.R;

public class FirstActivity extends AppCompatActivity {

    private static final int CAMERA_RESULT = 101;
    private ImageButton btnTakePhoto;
    private ImageView selectedImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_mood_event_alert_dialog);

        btnTakePhoto = findViewById(R.id.btn_take_photo);
        selectedImageView = findViewById(R.id.imageView);

        btnTakePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(FirstActivity.this, CapturePhotoActivity.class);
            startActivityForResult(intent, CAMERA_RESULT);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_RESULT && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("capturedImage");
            selectedImageView.setImageBitmap(photo);
        }
    }
}
