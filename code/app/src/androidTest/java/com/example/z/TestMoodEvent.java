package com.example.z;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.z.views.HomeActivity;
import com.example.z.views.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * UI Tests for Mood Events: Adding, Editing, Deleting, and Validation.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestMoodEvent {

    private static FirebaseAuth auth;
    private static FirebaseFirestore db;

    @Rule
    public ActivityScenarioRule<ProfileActivity> activityScenarioRule =
            new ActivityScenarioRule<>(ProfileActivity.class);

    @Before
    public void seedMoodEntry() throws InterruptedException {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            throw new AssertionError("Test requires a logged-in user!");
        }

        String userId = user.getUid();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean alreadyExists = new AtomicBoolean(false);

        db.collection("moods")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Log.d("TEST", "Mood entry already exists in Emulator. Skipping seeding.");
                        alreadyExists.set(true);
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e("TEST", "Failed to check existing moods in Emulator", e);
                    latch.countDown();
                });

        latch.await(5, TimeUnit.SECONDS);

        if (alreadyExists.get()) {
            return;
        }

        // Create a single mood entry in Emulator
        Map<String, Object> moodData = new HashMap<>();
        moodData.put("userId", userId);
        moodData.put("username", "TestUser");
        moodData.put("type", "happiness");
        moodData.put("description", "Feeling great!");
        moodData.put("situation", "alone");
        moodData.put("trigger", "sunshine");
        moodData.put("timestamp", new Date());

        db.collection("moods").add(moodData)
                .addOnSuccessListener(documentReference ->
                        Log.d("TEST", "Seeded Mood Entry in Emulator: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        Log.e("TEST", "Failed to seed mood entry in Emulator", e));
    }

    @BeforeClass
    public static void setUp() throws InterruptedException {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ensure emulator is used before any Firestore operations
        auth.useEmulator("10.0.2.2", 9099);  // Firebase Auth Emulator
        db.useEmulator("10.0.2.2", 8080);    // Firestore Emulator

        CountDownLatch latch = new CountDownLatch(1);

        auth.signOut();

        auth.signInWithEmailAndPassword("testuser@example.com", "testpassword")
                .addOnCompleteListener(signInTask -> {
                    if (signInTask.isSuccessful()) {
                        Log.d("TestSetup", "User already exists in Emulator, proceeding.");
                        ensureFirestoreUserExists(auth.getCurrentUser(), latch);
                    } else {
                        // Create User in Firebase Auth
                        auth.createUserWithEmailAndPassword("testuser@example.com", "testpassword")
                                .addOnCompleteListener(createTask -> {
                                    if (createTask.isSuccessful()) {
                                        FirebaseUser user = auth.getCurrentUser();
                                        if (user != null) {
                                            ensureFirestoreUserExists(user, latch);
                                        }
                                    } else {
                                        Log.e("TestSetup", "User creation failed: " + createTask.getException().getMessage());
                                        latch.countDown();
                                    }
                                });
                    }
                });

        // Wait until Firebase Auth & Firestore setup completes
        latch.await(10, TimeUnit.SECONDS);
    }


    /**
     * Test Adding a Mood Event
     */
    @Test
    public void testAddMoodEvent() {

        SystemClock.sleep(2000);
        // Click Add Mood Button
        onView(withId(R.id.nav_add)).perform(click());
        SystemClock.sleep(2000);
        // Select a Mood from Spinner
        onView(withId(R.id.spinner_mood)).perform(click());
        SystemClock.sleep(2000);
        // Select the "happiness" option using its displayed text
        onView(withText("happiness")).inRoot(isPlatformPopup()).perform(click());
        SystemClock.sleep(2000);
        // Type a Description
        onView(withId(R.id.edit_description)).perform(typeText("Feeling great today!"));
        SystemClock.sleep(2000);
        // Type a Trigger
        onView(withId(R.id.edit_hashtags)).perform(typeText("Sunshine"));
        SystemClock.sleep(2000);
        // Click Post
        onView(withId(R.id.btn_post)).perform(click());
        SystemClock.sleep(2000);
        // Verify Mood is Added to RecyclerView
        onView(withId(R.id.recyclerViewUserMoods))
                .perform(RecyclerViewActions.scrollToPosition(0))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Editing a Mood Event
     */
    @Test
    public void testEditMoodEvent() {
        SystemClock.sleep(2000);
        // Long Press on First Mood Item to Edit
        onView(withId(R.id.recyclerViewUserMoods))
                .perform(actionOnItemAtPosition(0, longClick()));
        SystemClock.sleep(2000);
        // Modify the Mood Description
        onView(withId(R.id.edit_description))
                .perform(replaceText("Updated description!"));
        SystemClock.sleep(2000);
        // Save Changes
        onView(withId(R.id.btn_post)).perform(click());
        SystemClock.sleep(2000);
        // Verify Updated Text is Displayed
        onView(withId(R.id.recyclerViewUserMoods))
                .perform(RecyclerViewActions.scrollToPosition(0))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Deleting a Mood Event
     */
    @Test
    public void testDeleteMoodEvent() {
        SystemClock.sleep(2000);
        String moodDescription = "Feeling great!";
        // Click First Mood Item
        onView(withId(R.id.recyclerViewUserMoods))
                .perform(actionOnItemAtPosition(0, click()));
        SystemClock.sleep(2000);
        // Click Delete Button
        onView(withId(R.id.btnDeletePost)).perform(click());
        SystemClock.sleep(2000);
        // Verify the deleted mood is NOT in the RecyclerView anymore
        onView(withText(moodDescription))
                .check(doesNotExist());
    }

    /**
     * Test Error Message for Empty Mood Type
     */
    @Test
    public void testErrorMessageForEmptyMoodType() {
        // Click Add Mood Button
        SystemClock.sleep(2000);
        onView(withId(R.id.nav_add)).perform(click());
        SystemClock.sleep(2000);
        // Leave Mood Spinner Unselected
        // Type Description
        onView(withId(R.id.edit_description)).perform(typeText("This test should pop up an error!"));
        SystemClock.sleep(2000);
        // Click Post Button
        onView(withId(R.id.btn_post)).perform(click());
        SystemClock.sleep(2000);
        // Verify the message appears
        onView(withText("You must tell us how you are feeling!"))
                .check(matches(isDisplayed()));
        SystemClock.sleep(2000);
        // Click the "OK" button on the AlertDialog
        onView(withId(android.R.id.button1)).perform(click());
        SystemClock.sleep(2000);
    }

    /**
     * Test Error Message for Description Exceeding 20 Characters
     */
    @Test
    public void testErrorMessageForLongDescription() {
        // Click Add Mood Button
        SystemClock.sleep(2000);
        onView(withId(R.id.nav_add)).perform(click());
        SystemClock.sleep(2000);

        // Select a Mood from Spinner (Assuming selecting a mood is required)
        onView(withId(R.id.spinner_mood)).perform(click());
        SystemClock.sleep(2000);
        onView(withText("happiness")).inRoot(isPlatformPopup()).perform(click());
        SystemClock.sleep(2000);

        // Type a Description Longer than 20 Characters
        onView(withId(R.id.edit_description)).perform(typeText("This description is way too long for the field!"));
        SystemClock.sleep(2000);

        // Click Post Button
        onView(withId(R.id.btn_post)).perform(click());
        SystemClock.sleep(2000);

        // Verify the error message appears
        onView(withText("Description must be 20 characters max!"))
                .check(matches(isDisplayed()));
        SystemClock.sleep(2000);

        // Click the "OK" button on the AlertDialog
        onView(withId(android.R.id.button1)).perform(click());
        SystemClock.sleep(2000);
    }

    @After
    public void tearDown() {
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

    /**
     * Ensures that the Firestore user document exists before proceeding with tests.
     */
    private static void ensureFirestoreUserExists(FirebaseUser user, CountDownLatch latch) {
        if (user == null) {
            latch.countDown();
            return;
        }

        String userId = user.getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // Create user in Firestore Emulator
                        Map<String, Object> testUser = new HashMap<>();
                        testUser.put("username", "TestUser");
                        testUser.put("email", "testuser@example.com");

                        db.collection("users").document(userId)
                                .set(testUser)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("TestSetup", "Firestore Emulator user document created.");
                                    latch.countDown();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("TestSetup", "Error creating Firestore Emulator user", e);
                                    latch.countDown();
                                });
                    } else {
                        Log.d("TestSetup", "Firestore user already exists. Proceeding.");
                        latch.countDown();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("TestSetup", "Error checking Firestore user document", e);
                    latch.countDown();
                });
    }
}
