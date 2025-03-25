package com.example.z;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import android.util.Log;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.z.mood.Mood;
import com.example.z.views.ForYouActivity;
import com.example.z.views.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class ForYouActivityTest {

    @Rule
    public ActivityScenarioRule<ForYouActivity> activityRule = new ActivityScenarioRule<>(ForYouActivity.class);

    @BeforeClass
    public static void setup() {
        Intents.init();

        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;

        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
        FirebaseAuth.getInstance().useEmulator(androidLocalhost, 9099);

        // Add delay to ensure emulators are ready
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void seedDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference moodsRef = db.collection("moods");
        FirebaseAuth auth = FirebaseAuth.getInstance();

        CountDownLatch authLatch = new CountDownLatch(1);

        // Create test user and sign in
        auth.createUserWithEmailAndPassword("test@example.com", "test123")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Create test moods
                        createTestMoods(moodsRef, auth.getCurrentUser().getUid());
                        authLatch.countDown();
                    }
                });

        try {
            authLatch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e("Test Setup", "Database seeding was interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    private void createTestMoods(CollectionReference moodsRef, String userId) {
        // Create some test moods
        Mood mood1 = new Mood(userId, "mood1", "testuser", "Happy", "Good day", "Social", new Date(), null, "Test mood 1");
        Mood mood2 = new Mood(userId, "mood2", "testuser", "Sad", "Bad day", "Alone", new Date(), null, "Test mood 2");

        moodsRef.document("mood1").set(mood1);
        moodsRef.document("mood2").set(mood2);
    }

    @Test
    public void testUIElementsDisplayed() {
        // Verify all UI elements are displayed
        onView(withId(R.id.tab_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.swipe_refresh_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.recycler_view_similar_moods)).check(matches(isDisplayed()));
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()));
        onView(withId(R.id.empty_state_text)).check(matches(isDisplayed()));
    }

    @Test
    public void testTabNavigation() {
        // Click on "Following" tab
        onView(withText("Following")).perform(click());

        // Verify that HomeActivity is launched
        Intents.intended(IntentMatchers.hasComponent(HomeActivity.class.getName()));
    }

    @Test
    public void testPullToRefresh() {
        // Perform pull-to-refresh action
        onView(withId(R.id.swipe_refresh_layout)).perform(androidx.test.espresso.action.ViewActions.swipeDown());

        // Verify that the RecyclerView is still displayed
        onView(withId(R.id.recycler_view_similar_moods)).check(matches(isDisplayed()));
    }

    @Test
    public void testEmptyState() {
        // Clear the database to trigger empty state
        clearDatabase();

        // Verify empty state message is displayed
        onView(withId(R.id.empty_state_text)).check(matches(isDisplayed()));
    }

    private void clearDatabase() {
        String projectId = "project-z-24051";
        URL url = null;
        try {
            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
        } catch (MalformedURLException exception) {
            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
        }

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Response Code", "Response Code: " + response);
        } catch (IOException exception) {
            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @After
    public void tearDown() {
        clearDatabase();
    }
} 