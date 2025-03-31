package com.example.z.image;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.z.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Handles image selection and capturing to provide additional context for a user's mood.
 *
 * Outstanding Issues:
 *      - Not integrated with add/edit mood functionality (further logic required).
 */
public class Image extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private static final int TAKE_PHOTO = 2;
    private static final int CAMERA_REQUEST = 100;

    private ImageView imageView;
    private Button btnSelectImage, btnCaptureImage;

    /**
     * Called when the activity is first created.
     * Initializes UI components and sets up click listeners for selecting or capturing an image.
     *
     * @param savedInstanceState A Bundle containing the activity's previously saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        // Initialize UI elements
        imageView = findViewById(R.id.imageView);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnCaptureImage = findViewById(R.id.btnCaptureImage);

        // Set up click listeners
        btnSelectImage.setOnClickListener(view -> openGallery());
        btnCaptureImage.setOnClickListener(view -> capturePhoto());
    }

    /**
     * Opens the device's gallery to select an image.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    /**
     * Launches the device's camera to capture a photo.
     * Displays a toast message if the camera is unavailable.
     */
    private void capturePhoto() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the result of an image selection or photo capture operation.
     *
     * @param requestCode The request code used to identify the action taken.
     * @param resultCode  The result code indicating success or failure.
     * @param data        The intent containing image data, if applicable.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE) {
                // Handle image selection from the gallery
                Uri imageUri = data.getData();
                try {
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    imageView.setImageBitmap(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == TAKE_PHOTO) {
                // Handle photo captured from the camera
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imageView.setImageBitmap(imageBitmap);
            }
        }
    }
}


