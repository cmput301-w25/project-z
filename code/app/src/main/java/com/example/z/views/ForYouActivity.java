package com.example.z.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.z.R;
import com.example.z.mood.MoodArrayAdapter;
import com.example.z.mood.Mood;
import com.example.z.utils.ForYouController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * ForYouActivity displays recommended mood posts of other users based of current user's mood history.
 * A mood and users discovery page.
 */

public class ForYouActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MoodArrayAdapter moodAdapter;
    private List<Mood> similarMoods;
    private ForYouController forYouController;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TabLayout tabLayout;
    private TabLayout.OnTabSelectedListener tabListener;

    /**
     * Saves the current tab selection when activity is recreated
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_tab", tabLayout.getSelectedTabPosition());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_for_you);

        // Initialize UI elements from layout
        recyclerView = findViewById(R.id.recycler_view_similar_moods);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateText = findViewById(R.id.empty_state_text);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        tabLayout = findViewById(R.id.tab_layout);

        // Set up RecyclerView with adapter
        similarMoods = new ArrayList<>();
        moodAdapter = new MoodArrayAdapter(this, similarMoods);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(moodAdapter);

        // Check if user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            forYouController = new ForYouController(userId);

            // Set up pull-to-refresh functionality
            swipeRefreshLayout.setOnRefreshListener(this::refreshSimilarMoods);

            // Load initial mood recommendations
            loadSimilarMoods();

            // Set up tab navigation
            tabListener = new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    switch (tab.getPosition()) {
                        case 0: // "For You" tab
                            loadSimilarMoods();  // Refresh recommendations
                            break;
                        case 1: // "Following" tab
                            // Navigate to HomeActivity
                            Intent intent = new Intent(ForYouActivity.this, HomeActivity.class);
                            startActivity(intent);
                            // Reset to "For You" tab
                            tabLayout.getTabAt(0).select();
                            break;
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    // Not needed
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 0) {  // For You tab
                        // Scroll to top when reselecting For You tab
                        recyclerView.smoothScrollToPosition(0);
                    }
                }
            };
            tabLayout.addOnTabSelectedListener(tabListener);
        } else {
            // Show message if user is not logged in
            showEmptyState("Please log in to see personalized content");
        }

        // Restore previous tab selection if activity was recreated
        if (savedInstanceState != null) {
            int selectedTab = savedInstanceState.getInt("selected_tab", 0);
            tabLayout.selectTab(tabLayout.getTabAt(selectedTab));
        }
    }

    /**
     * Clean up tab listener when activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tabLayout != null) {
            tabLayout.removeOnTabSelectedListener(tabListener);
        }
    }

    /**
     * Refreshes user's mood recommendations
     */
    private void refreshSimilarMoods() {
        forYouController.refreshUserMoods(() -> {
            loadSimilarMoods();
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
            });
        });
    }

    /**
     * Loads and displays similar moods based on user's history
     */
    private void loadSimilarMoods() {
        showLoading();
        forYouController.getSimilarMoods(new ForYouController.SimilarMoodsCallback() {
            @Override
            public void onSuccess(List<Mood> moods) {
                runOnUiThread(() -> {
                    similarMoods.clear();
                    similarMoods.addAll(moods);
                    moodAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);

                    if (moods.isEmpty()) {
                        showEmptyState("No similar moods found. Try adding more moods to improve recommendations.");
                    } else {
                        hideEmptyState();
                    }
                    hideLoading();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showEmptyState("Error loading recommendations: " + e.getMessage());
                    hideLoading();
                });
            }
        });
    }

    /**
     * Shows loading indicator and hides other views
     */
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    /**
     * Hides loading indicator and shows RecyclerView
     */
    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Shows empty state message when no moods available
     */
    private void showEmptyState(String message) {
        emptyStateText.setText(message);
        emptyStateText.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    /**
     * Hides empty state message and shows RecyclerView
     */
    private void hideEmptyState() {
        emptyStateText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }
}