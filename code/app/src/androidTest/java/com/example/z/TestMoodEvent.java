package com.example.z;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static java.util.EnumSet.allOf;

import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.z.views.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * UI Tests for Mood Events: Adding, Editing, Deleting, and Validation.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestMoodEvent {

    @Rule
    public ActivityScenarioRule<ProfileActivity> activityScenarioRule =
            new ActivityScenarioRule<>(ProfileActivity.class);

    @BeforeClass
    public static void setup(){
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Log in test user (Make sure this user exists in Firebase)
        auth.signInWithEmailAndPassword("0XDZAMXVL2WA7Q35qlP3Y2UdpZI3", "123456")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("TestSetup", "Test user logged in successfully.");
                    } else {
                        Log.e("TestSetup", "Failed to log in test user: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Test Adding a Mood Event
     */
    @Test
    public void testAddMoodEvent() {
        // Click Add Mood Button
        onView(withId(R.id.nav_add)).perform(click());

        // Select a Mood (Spinner)
        onView(withId(R.id.spinner_mood)).perform(click());
        onView(withText("Happiness")).perform(click());

        // Type a Description
        onView(withId(R.id.edit_description)).perform(typeText("Feeling great today!"));

        // Type a Trigger
        onView(withId(R.id.edit_hashtags)).perform(typeText("Sunshine"));

        // Click Post
        onView(withId(R.id.btn_post)).perform(click());

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
        // Long Press on First Mood Item to Edit
        onView(withId(R.id.recyclerViewUserMoods))
                .perform(actionOnItemAtPosition(0, click()));

        // Modify the Mood Description
        onView(withId(R.id.edit_description))
                .perform(replaceText("Updated description!"));

        // Save Changes
        onView(withId(R.id.btn_post)).perform(click());

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
        // Click First Mood Item
        onView(withId(R.id.recyclerViewUserMoods))
                .perform(actionOnItemAtPosition(0, click()));

        // Click Delete Button
        onView(withId(R.id.btnDeletePost)).perform(click());

        // Verify Mood is Removed from RecyclerView
        onView(withId(R.id.recyclerViewUserMoods))
                .perform(RecyclerViewActions.scrollToPosition(0))
                .check(matches(isDisplayed())); // Might need to check item count instead
    }

    /**
     * Test Error Message for Empty Mood Type
     */
    @Test
    public void testErrorMessageForEmptyMoodType() {
        // Click Add Mood Button
        onView(withId(R.id.nav_add)).perform(click());
        SystemClock.sleep(2000);
        // Leave Mood Spinner Unselected
        // Type Description
        onView(withId(R.id.edit_description)).perform(typeText("This should fail!"));
        SystemClock.sleep(2000);
        // Click Post Button
        onView(withId(R.id.btn_post)).perform(click());
        SystemClock.sleep(2000);
        // Verify Toast Error Message (Requires Espresso-Intents)
        onView(withText("You must tell us how you are feeling!"))
                .check(matches(isDisplayed()));
        SystemClock.sleep(2000);
        // Press Back to Close Dialog
        pressBack();
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
}
