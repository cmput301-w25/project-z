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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save any necessary state
        outState.putInt("selected_tab", tabLayout.getSelectedTabPosition());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_for_you);

        // Initialize UI elements
        recyclerView = findViewById(R.id.recycler_view_similar_moods);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateText = findViewById(R.id.empty_state_text);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        tabLayout = findViewById(R.id.tab_layout);

        // Set up RecyclerView with MoodArrayAdapter
        similarMoods = new ArrayList<>();
        moodAdapter = new MoodArrayAdapter(this, similarMoods);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(moodAdapter);

        // Initialize controller
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            forYouController = new ForYouController(userId);

            // Set up swipe refresh
            swipeRefreshLayout.setOnRefreshListener(this::refreshSimilarMoods);

            // Initial load of similar moods
            loadSimilarMoods();

            // Store listener in a field for cleanup
            tabListener = new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    switch (tab.getPosition()) {
                        case 0: // "For You" tab
                            loadSimilarMoods();
                            break;
                        case 1: // "Following" tab
                            // Start Following Activity
                            Intent intent = new Intent(ForYouActivity.this, HomeActivity.class);
                            startActivity(intent);
                            // Reset tab selection to "For You" since we're leaving this activity
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
                    // Optional: Handle tab reselection (e.g., scroll to top)
                }
            };
            tabLayout.addOnTabSelectedListener(tabListener);
        } else {
            // Handle not logged in state
            showEmptyState("Please log in to see personalized content");
        }

        if (savedInstanceState != null) {
            int selectedTab = savedInstanceState.getInt("selected_tab", 0);
            tabLayout.selectTab(tabLayout.getTabAt(selectedTab));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tabLayout != null) {
            tabLayout.removeOnTabSelectedListener(tabListener);
        }
    }

    private void refreshSimilarMoods() {
        forYouController.refreshUserMoods(() -> {
            loadSimilarMoods();
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
            });
        });
    }

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

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showEmptyState(String message) {
        emptyStateText.setText(message);
        emptyStateText.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyStateText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }
}