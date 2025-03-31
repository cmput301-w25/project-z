package com.example.z;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.graphics.Movie;
import android.os.SystemClock;
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

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        if (!completed) {
            throw new AssertionError("Authentication setup timed out!");
        }

        if (auth.getCurrentUser() == null) {
            throw new AssertionError("User authentication failed!");
        }
    }


    @Before
    public void seedFollowRequests() throws InterruptedException {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            throw new AssertionError("Test requires a logged-in user!");
        }

        String userId = user.getUid();
        CountDownLatch latch = new CountDownLatch(1);

        // Create follower1 and follower2 user documents
        ensureUserExists("1", "follower1");
        ensureUserExists("2", "follower2");

        db.collection("followers")
                .whereEqualTo("followedId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Log.d("TEST", "Follow requests exist. Skipping seeding.");
                    } else {
                        addFollowRequest(userId, "1");
                        addFollowRequest(userId, "2");
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e("TEST", "Error checking existing follow requests", e);
                    latch.countDown();
                });

        latch.await(5, TimeUnit.SECONDS);
    }

    // Creates a user document if it does not already exist
    private void ensureUserExists(String userId, String username) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("username", username);
                        userData.put("email", userId + "@example.com");

                        db.collection("users").document(userId)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> Log.d("TEST", "User created: " + userId))
                                .addOnFailureListener(e -> Log.e("TEST", "Error creating user", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("TEST", "Firestore check failed", e));
    }

    private void addFollowRequest(String followedId, String followerId) {
        Map<String, Object> followRequest = new HashMap<>();
        followRequest.put("followedId", followedId);
        followRequest.put("followerId", followerId);
        followRequest.put("status", "pending");

        db.collection("followers").add(followRequest)
                .addOnSuccessListener(docRef -> Log.d("TEST", "Follow request added: " + docRef.getId()))
                .addOnFailureListener(e -> Log.e("TEST", "Error adding follow request", e));
    }

    @Test
    public void testTwoFollowRequestsDisplayed() {
        SystemClock.sleep(2000);
        onView(withText("follower1")).check(matches(isDisplayed()));
        onView(withText("follower2")).check(matches(isDisplayed()));
    }

    @Test
    public void testAcceptFollowRequestRemovesCard() {
        SystemClock.sleep(2000);
        onView(withId(R.id.notificationsRecyclerView))
                .perform(actionOnItemAtPosition(0, click()));
        SystemClock.sleep(2000);
        onView(withText("Accept")).perform(click());
        SystemClock.sleep(2000);
        onView(withText("follower1")).check(doesNotExist());
    }

    @Test
    public void testRejectFollowRequestRemovesCard() {
        SystemClock.sleep(2000);
        onView(withId(R.id.notificationsRecyclerView))
                .perform(actionOnItemAtPosition(1, click()));
        SystemClock.sleep(2000);
        onView(withText("Reject")).perform(click());
        SystemClock.sleep(2000);
        onView(withText("follower2")).check(doesNotExist());
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