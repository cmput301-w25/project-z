package com.example.z.image;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.z.R;

public class CapturePhotoActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 100;
    private ImageView capturedImage;
    private Button btnRetake, btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_photo);

        capturedImage = findViewById(R.id.capturedImage);
        btnRetake = findViewById(R.id.btnRetake);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Open Camera on Activity Start
        openCamera();

        // Retake Photo
        btnRetake.setOnClickListener(v -> openCamera());

        // Confirm and Return Image to First Activity
        btnConfirm.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("capturedImage", ((BitmapDrawable) capturedImage.getDrawable()).getBitmap());
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            capturedImage.setImageBitmap(photo);
        }
    }
}

