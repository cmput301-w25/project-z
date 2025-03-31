package com.example.z;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.graphics.Movie;
import android.util.Log;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.z.user.User;
import com.example.z.views.NotificationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class NotificationActivityTest {

    private static FirebaseAuth auth;
    private static FirebaseFirestore db;

    @Rule
    public ActivityScenarioRule<NotificationActivity> scenario =
            new ActivityScenarioRule<>(NotificationActivity.class);

    private static final String TEST_USER_ID = "1"; // The user receiving follow requests
    private static final String FOLLOWER_ID_1 = "2"; // First follower
    private static final String FOLLOWER_ID_2 = "3"; // Second follower

    @BeforeClass
    public static void setup() throws InterruptedException {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        auth.useEmulator("10.0.2.2", 9099); // Firebase Auth Emulator
        db.useEmulator("10.0.2.2", 8080);   // Firestore Emulator

        auth.signOut();
        CountDownLatch latch = new CountDownLatch(1);

        auth.signInWithEmailAndPassword("testuser@example.com", "testpassword")
                .addOnCompleteListener(signInTask -> {
                    if (signInTask.isSuccessful()) {
                        ensureFirestoreUserExists(auth.getCurrentUser(), latch);
                    } else {
                        auth.createUserWithEmailAndPassword("testuser@example.com", "testpassword")
                                .addOnCompleteListener(createTask -> {
                                    if (createTask.isSuccessful()) {
                                        ensureFirestoreUserExists(auth.getCurrentUser(), latch);
                                    } else {
                                        Log.e("TestSetup", "User creation failed", createTask.getException());
                                        latch.countDown();
                                    }
                                });
                    }
                });

        latch.await(10, TimeUnit.SECONDS);
    }

    @Before
    public void seedDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference followersRef = db.collection("followers");
//        CollectionReference usersRef = db.collection("users");
//        User[] users = {
//                new User("user1@gmail.com", "user1", "1"),
//                new User("user2@gmil.com", "user2", "2")
//        };
//        for (User user : users) {
//            usersRef.document().set(user);
//        }

        // Create follow request from follower 1
        Map<String, Object> followRequest1 = new HashMap<>();
        followRequest1.put("followerId", FOLLOWER_ID_1);
        followRequest1.put("followedId", TEST_USER_ID);
        followRequest1.put("status", "pending");

        // Create follow request from follower 2
        Map<String, Object> followRequest2 = new HashMap<>();
        followRequest2.put("followerId", FOLLOWER_ID_2);
        followRequest2.put("followedId", TEST_USER_ID);
        followRequest2.put("status", "pending");

        // Add requests to Firestore emulator
        followersRef.document(FOLLOWER_ID_1 + "_" + TEST_USER_ID).set(followRequest1);
        followersRef.document(FOLLOWER_ID_2 + "_" + TEST_USER_ID).set(followRequest2);

    }

    @After
    public void tearDown() {
        String projectId = "YOUR-PROJECT-ID-HERE";
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
     * Test if two follow request notifications are displayed.
     */
    @Test
    public void testFollowRequestsAreDisplayed() {
        // Check if both follow request notifications exist in RecyclerView
        onView(withText("user2 has requested to follow you")).check(matches(isDisplayed()));
        onView(withText("user3 has requested to follow you")).check(matches(isDisplayed()));
    }

    /**
     * Test accepting a follow request removes it from the list.
     */
    @Test
    public void testAcceptFollowRequest() {
        // Click accept button for first follow request (position 0)
        onView(withId(R.id.notificationsRecyclerView))
                .perform(actionOnItemAtPosition(0, click()));

        // Verify that the first request has disappeared
        onView(withText("user2 sent you a follow request."))
                .check(matches(isDisplayed())); // Should FAIL if it was removed
    }

    /**
     * Test rejecting a follow request removes it from the list.
     */
    @Test
    public void testRejectFollowRequest() {
        // Click reject button for second follow request (position 1)
        onView(withId(R.id.notificationsRecyclerView))
                .perform(actionOnItemAtPosition(1, click()));

        // Verify that the second request has disappeared
        onView(withText("user3 sent you a follow request."))
                .check(matches(isDisplayed())); // Should FAIL if it was removed
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
//    @Test
//    public void appShouldDisplayExistingMoviesOnLaunch() {
//        // Check that the initial data is loaded
//        onView(withText("Oppenheimer")).check(matches(isDisplayed()));
//        onView(withText("Barbie")).check(matches(isDisplayed()));
//        // Click on Oppenheimer
//        onView(withText("Oppenheimer")).perform(click());
//        // Check that the movie details are displayed correctly
//        onView(withId(R.id.edit_title)).check(matches(withText("Oppenheimer")));
//        onView(withId(R.id.edit_genre)).check(matches(withText("Thriller/Historical Drama")));
//        onView(withId(R.id.edit_year)).check(matches(withText("2023")));
//    }
//
//    @Test
//    public void addMovieShouldAddValidMovieToMovieList() {
//        // Click on button to open addMovie dialog
//        onView(withId(R.id.buttonAddMovie)).perform(click());
//
//        // Input Movie Details
//        onView(withId(R.id.edit_title)).perform(ViewActions.typeText("Interstellar"));
//        onView(withId(R.id.edit_genre)).perform(ViewActions.typeText("Science Fiction"));
//        onView(withId(R.id.edit_year)).perform(ViewActions.typeText("2014"));
//
//        // Submit Form
//        onView(withId(android.R.id.button1)).perform(click());
//
//        // Check that our movie list has our new movie
//        onView(withText("Interstellar")).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void addMovieShouldShowErrorForInvalidMovieName() {
//        // Click on button to open addMovie dialog
//        onView(withId(R.id.buttonAddMovie)).perform(click());
//        // Add movie details, but no title
//        onView(withId(R.id.edit_genre)).perform(ViewActions.typeText("Science Fiction"));
//        onView(withId(R.id.edit_year)).perform(ViewActions.typeText("2014"));
//        // Submit Form
//        onView(withId(android.R.id.button1)).perform(click());
//        // Check that an error is shown to the user
//        onView(withId(R.id.edit_title)).check(matches(hasErrorText("Move name cannot be empty!")));
//  }
}
