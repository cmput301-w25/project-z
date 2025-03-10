package com.example.z;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import android.util.Log;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

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
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class SignUpActivtyTest {

    @Rule
    public ActivityScenarioRule<SignUpActivity> activityRule = new ActivityScenarioRule<>(SignUpActivity.class);

    @BeforeClass
    public static void setup(){
        Intents.init();

        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;

        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
        FirebaseAuth.getInstance().useEmulator(androidLocalhost, 9099); // Default port for Auth Emulator

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
        CollectionReference userTestRef = db.collection("users");

        FirebaseAuth auth = FirebaseAuth.getInstance();

        CountDownLatch authLatch = new CountDownLatch(1);

        auth.createUserWithEmailAndPassword("valid@example.com", "valid123")
                .addOnCompleteListener(task -> {
                    User user = new User("valid@example.com", "username1");
                    userTestRef.document().set(user)
                            .addOnCompleteListener(firestoreTask -> {
                                authLatch.countDown();
                            });
                });

        try {
            // Wait for operations to complete (with a reasonable timeout)
            authLatch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e("Test Setup", "Database seeding was interrupted", e);
            Thread.currentThread().interrupt(); // Restore the interrupted status
        }
    }

    @Test
    public void testUIElementsDisplayed() {
        // Verify all UI elements are displayed
        onView(withId(R.id.etEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.etUsername)).check(matches(isDisplayed()));
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.btnSignup)).check(matches(isDisplayed()));
        onView(withId(R.id.tvLogin)).check(matches(isDisplayed()));

    }

    @Test
    public void testEmptyFieldsValidation() {
        // Click SignUp without entering any data
        onView(withId(R.id.btnSignup)).perform(click());

        onView(withId(R.id.etEmail)).check(matches(hasErrorText("Email cannot be empty.")));
        onView(withId(R.id.etPassword)).check(matches(hasErrorText("Password cannot be empty.")));
        onView(withId(R.id.etUsername)).check(matches(hasErrorText("Username cannot be empty.")));
    }

    @Test
    public void testNavigationToLogin() {
        // Click the sign up button
        onView(withId(R.id.tvLogin)).perform(click());

        // Verify that SignUpActivity is launched
        Intents.intended(IntentMatchers.hasComponent(LogInActivity.class.getName()));
    }

    @Test
    public void testSignUpWithValidCredentials() {
        // Generate unique credentials using timestamp
        long timestamp = System.currentTimeMillis();
        String uniqueEmail = "test" + timestamp + "@example.com";
        String uniqueUsername = "test" + timestamp;

        // Enter unique email, username, and password
        onView(withId(R.id.etEmail)).perform(typeText(uniqueEmail));
        onView(withId(R.id.etUsername)).perform(typeText(uniqueUsername));
        onView(withId(R.id.etPassword)).perform(typeText("test123"));

        // Click Sign Up button
        onView(withId(R.id.btnSignup)).perform(click());

        // Add a small delay to ensure emulators are ready
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify that ProfileActivity is launched
        Intents.intended(IntentMatchers.hasComponent(ProfileActivity.class.getName()));
    }

    @Test
    public void testSignUpWithExistingUsername() {
        long timestamp = System.currentTimeMillis();
        String uniqueEmail = "test" + timestamp + "@example.com";

        // Enter an email and username that already exist in the database
        onView(withId(R.id.etEmail)).perform(typeText(uniqueEmail));
        onView(withId(R.id.etUsername)).perform(typeText("username1"));
        onView(withId(R.id.etPassword)).perform(typeText("test321"));

        // Click the sign-up button
        onView(withId(R.id.btnSignup)).perform(click());

        // Verify that an error message is displayed
        onView(withId(R.id.etUsername)).check(matches(hasErrorText("Username already exists")));
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
