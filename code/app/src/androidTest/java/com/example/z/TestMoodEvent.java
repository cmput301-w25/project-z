package com.example.z;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;

import static org.hamcrest.Matchers.anything;

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
import com.google.firebase.firestore.SetOptions;

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
                .limit(2)
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
        Map<String, Object> moodData1 = new HashMap<>();
        moodData1.put("userId", userId);
        moodData1.put("username", "TestUser");
        moodData1.put("type", "happiness");
        moodData1.put("description", "Feeling great!");
        moodData1.put("situation", "alone");
        moodData1.put("emoji", "happy_1");
        moodData1.put("trigger", "sunshine");
        moodData1.put("timestamp", new Date());

        Map<String, Object> moodData2 = new HashMap<>();
        moodData2.put("userId", userId);
        moodData2.put("username", "TestUser");
        moodData2.put("type", "anger");
        moodData2.put("description", "Feeling angry!");
        moodData2.put("situation", "alone");
        moodData2.put("emoji", "anger_1");
        moodData2.put("trigger", "mad");
        moodData2.put("timestamp", new Date());

        db.collection("moods").add(moodData1)
                .addOnSuccessListener(documentReference ->
                        Log.d("TEST", "Seeded Mood Entry in Emulator: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        Log.e("TEST", "Failed to seed mood entry in Emulator", e));
        db.collection("moods").add(moodData2)
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

        SystemClock.sleep(8000);
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
        // Click emoji button
        onView(withId(R.id.btn_emoji_picker)).perform(click());
        SystemClock.sleep(2000);
        // Pick emoji
        onData(anything())
                .inAdapterView(withId(R.id.emojiView))
                        .atPosition(0)
                                .perform(click());
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
                .perform(actionOnItemAtPosition(0, longClick()));
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

        // Type a Description Longer than 200 Characters
        onView(withId(R.id.edit_description)).perform(typeText("This description is way too long for the field!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"));
        SystemClock.sleep(2000);

        // Click Post Button
        onView(withId(R.id.btn_post)).perform(click());
        SystemClock.sleep(2000);

        // Verify the error message appears
        onView(withText("Description must be 200 characters max!"))
                .check(matches(isDisplayed()));
        SystemClock.sleep(2000);

        // Click the "OK" button on the AlertDialog
        onView(withId(android.R.id.button1)).perform(click());
        SystemClock.sleep(2000);
    }

    @Test
    public void testFilterByAnger() {
        SystemClock.sleep(2000);

        // Click the Filter Button
        onView(withId(R.id.btnFilterMoods)).perform(click());
        SystemClock.sleep(2000);

        // Check the anger Checkbox (by text)
        onView(withText("Anger")).inRoot(isDialog()).perform(click());
        SystemClock.sleep(2000);

        // Verify anger checkbox is checked
        onView(withText("Anger")).check(matches(isChecked()));

        // Apply filter
        onView(withId(R.id.btnApplyFilter)).perform(click());
        SystemClock.sleep(2000);
        // Check if the 1 anger mood is left
        onView(withId(R.id.recyclerViewUserMoods))
                .check(matches(hasChildCount(1)));
    }
    @Test
    public void searchUser() {
        SystemClock.sleep(2000);
        // Navigate to search page
        onView(withId(R.id.nav_search)).perform(click());
        SystemClock.sleep(2000);
        // Click edit text
        onView(withId(R.id.search_bar)).perform(click());
        SystemClock.sleep(2000);
        // Type TestUser1
        onView(withId(R.id.search_bar))
                .perform(typeText("TestUser1"), closeSoftKeyboard());
        SystemClock.sleep(2000);
        onView(withId(R.id.search_button)).perform(click());
        SystemClock.sleep(2000);
        // Check if user was searched
        onView(withId(R.id.recyclerView_search))
                .check(matches(hasChildCount(1)));
    }

    @Test
    public void addComment() {
        SystemClock.sleep(2000);
        // Click First Mood Item
        onView(withId(R.id.recyclerViewUserMoods))
                .perform(actionOnItemAtPosition(0, click()));
        SystemClock.sleep(2000);
        // Input comment
        onView(withId(R.id.commentInput))
                .perform(typeText("Great!"), closeSoftKeyboard());
        SystemClock.sleep(2000);
        // Add comment
        onView(withId(R.id.btnAddComment)).perform(click());
        // Verify the one comment is in recycle view
        onView(withId(R.id.recyclerViewComments))
                .check(matches(hasChildCount(1)));
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

        String[] testUsers = {"testuser@example.com", "testuser1@example.com"};
        String[] usernames = {"TestUser", "TestUser1"};

        // Seed database with users
        for (int i = 0; i < testUsers.length; i++) {
            String email = testUsers[i];
            String username = usernames[i];
            String userId = email.replace("@example.com", "");

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", username);
            userInfo.put("email", email);

            db.collection("users").document(userId)
                            .set(userInfo, SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> {
                                        latch.countDown();
                                    })
                                            .addOnFailureListener(e -> {
                                                latch.countDown();
                                            });

            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!documentSnapshot.exists()) {
                            // Create user in Firestore Emulator
                            Map<String, Object> testUser = new HashMap<>();
                            testUser.put("username", "TestUser");
                            testUser.put("email", "testuser@example.com");


                            db.collection("users").document(user.getUid())
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
}
