package com.example.z.views;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.z.R;
import com.example.z.comments.Comment;
import com.example.z.comments.CommentArrayAdapter;
import com.example.z.data.DatabaseManager;
import com.example.z.mood.Mood;
import com.example.z.mood.MoodFragment;
import com.example.z.utils.ImgUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostActivity extends AppCompatActivity {
    private Mood mood;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView postDetails;
    private TextView postHeading;
    private EditText userComment;
    private Button commentButton;
    private RecyclerView recyclerView;
    private CommentArrayAdapter adapter;
    private List<Comment> commentList;
    private ListenerRegistration listenerRegistration;
    private DatabaseManager databaseManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        databaseManager = new DatabaseManager();

        // Bind UI elements
        postDetails = findViewById(R.id.postContent);
        postHeading = findViewById(R.id.username);
        userComment = findViewById(R.id.commentInput);
        commentButton = findViewById(R.id.btnAddComment);

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

        // Initialize recycler view
        recyclerView = findViewById(R.id.recyclerViewComments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentList = new ArrayList<>();
        adapter = new CommentArrayAdapter(this, commentList);
        recyclerView.setAdapter(adapter);

        loadOldComments();

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
        commentButton.setOnClickListener(v -> addComment());

    }

    public void loadOldComments() {
        listenerRegistration = db.collection("comments")
                .whereEqualTo("mood_id", mood.getDocumentId())
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("Firestore Error", "Error loading comments", error);
                        return;
                    }
                    if (snapshots != null) {
                        commentList.clear();
                        for (DocumentSnapshot document : snapshots.getDocuments()) {
                            Comment comment = document.toObject(Comment.class);
                            if (comment != null) {
                                comment.setCommentId(document.getId());
                                commentList.add(comment);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void addComment() {

        String editToString = userComment.getText().toString().trim();
        if (TextUtils.isEmpty(editToString)) {
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }

        String userId = user.getUid();
        String timestamp = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("username")) {
                        String commenter = documentSnapshot.getString("username");

                        db.collection("moods")
                                .whereEqualTo("userId", userId)
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    String userRecentEmoji = "happy_1";
                                    String userRecentEmotionalState = "happy";
                                    if (!querySnapshot.isEmpty()) {
                                        DocumentSnapshot recentMood = querySnapshot.getDocuments().get(0);
                                        if (recentMood.contains("emoji")) {
                                            userRecentEmoji = recentMood.getString("emoji");
                                        }
                                        if (recentMood.contains("type")) {
                                            userRecentEmotionalState = recentMood.getString("type");
                                        }
                                    }

                                    DocumentReference commentDocRef = db.collection("comments").document();
                                    String documentId = commentDocRef.getId();

                                    Comment userCommentObject = new Comment(userId, commenter, editToString, timestamp, userRecentEmoji, mood.getDocumentId(), userRecentEmotionalState, documentId);

                                    databaseManager.saveComment(userCommentObject, new DatabaseManager.OnCommentSavedListener() {
                                        @Override
                                        public void onSuccess() {
                                            userComment.setText("");
                                        }

                                        public void onFailure(Exception e) {
                                            Toast.makeText(PostActivity.this, "Could not post comment!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                });
                    }
                    else {
                        Toast.makeText(PostActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                    }
                });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
