package com.example.z.views;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.z.R;
import com.example.z.mood.Mood;
import com.example.z.mood.MoodFragment;
import com.example.z.utils.ImgUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PostActivity extends AppCompatActivity {
    private Mood mood;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView postDetails;
    private TextView postHeading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind UI elements
        postDetails = findViewById(R.id.postContent);
        postHeading = findViewById(R.id.username);

        // Change post font, color, size, etc
        postHeading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        postHeading.setTextColor(Color.parseColor("#E9D8A6"));

        postDetails.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        postDetails.setTextColor(Color.parseColor("#E9D8A6"));

        Typeface font = ResourcesCompat.getFont(this, R.font.itim_regular);
        postHeading.setTypeface(font, Typeface.BOLD);
        postDetails.setTypeface(font);

        // Find navigation buttons
        ImageButton home = findViewById(R.id.nav_home);
        ImageButton profile = findViewById(R.id.nav_profile);
        ImageButton notifications = findViewById(R.id.nav_notifications);
        ImageButton search = findViewById(R.id.nav_search);
        ImageButton addPostButton = findViewById(R.id.nav_add);
        ImageView img = findViewById(R.id.postImage);

        // Get mood to view
        mood = (Mood) getIntent().getSerializableExtra("mood");
        if (mood == null) {
            Toast.makeText(this, "Mood does not exist!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Format string
        String headingContent = String.format(
                "%s is feeling %s %s",
                mood.getUsername(),
                mood.getEmotionalState(),
                mood.getSocialSituation()
        );

        String postContent = String.format(
                "%s\n#%s\n\nPosted on: %s",
                mood.getDescription(),
                mood.getTrigger(),
                new SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()).format(mood.getCreatedAt())
        );

        postHeading.setText(headingContent);
        postDetails.setText(postContent);
        if (mood.getImg() != null) {
            img.setVisibility(View.VISIBLE);
            ImgUtil.displayBase64Image(mood.getImg(), img);
        } else {
            img.setVisibility(View.GONE);
        }
        // Set click listeners for navigation
        home.setOnClickListener(v -> navigateTo(HomeActivity.class));
        profile.setOnClickListener(v -> navigateTo(ProfileActivity.class));
        notifications.setOnClickListener(v -> navigateTo(NotificationActivity.class));
        search.setOnClickListener(v -> navigateTo(SearchActivity.class));

        // Open dialog to add a new mood post
        addPostButton.setOnClickListener(v -> openAddPostDialog());
    }

    /**
     * Navigates to the specified activity.
     *
     * @param targetActivity The activity class to navigate to.
     */
    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // Smooth transition effect
    }

    /**
     * Opens the MoodFragment dialog to allow the user to add a new mood post.
     */
    private void openAddPostDialog() {
        MoodFragment moodFragment = new MoodFragment();
        moodFragment.show(getSupportFragmentManager(), "AddMoodFragment");
    }
}
