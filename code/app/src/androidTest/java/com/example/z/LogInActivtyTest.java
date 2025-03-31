package com.example.z;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.util.Log;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.z.user.User;
import com.example.z.views.LogInActivity;
import com.example.z.views.ForYouActivity;
import com.example.z.views.ProfileActivity;
import com.example.z.views.SignUpActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LogInActivtyTest {

    @Rule
    public ActivityScenarioRule<LogInActivity> activityRule = new ActivityScenarioRule<>(LogInActivity.class);

    @BeforeClass
    public static void setup() {
        Intents.init();
        String androidLocalhost = "10.0.2.2";
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, 8080);
        FirebaseAuth.getInstance().useEmulator(androidLocalhost, 9099);
    }

    @Before
    public void seedDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userTestRef = db.collection("user_test");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword("valid@example.com", "valid123");
        User user = new User("valid@example.com", "valid123", "hoyoibh626");
    }

    @Test
    public void testUIElementsDisplayed() {
        // Verify all UI elements are displayed
        onView(withId(R.id.etEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
        onView(withId(R.id.btnSignUp)).check(matches(isDisplayed()));
    }

    @Test
    public void testEmptyFieldsValidation() {
        // Click login without entering any data
        onView(withId(R.id.btnLogin)).perform(click());

        onView(withId(R.id.etEmail)).check(matches(hasErrorText("Email cannot be empty.")));
        onView(withId(R.id.etPassword)).check(matches(hasErrorText("Password cannot be empty.")));
    }


    @Test
    public void testNavigationToSignUp() {
        // Click the sign up button
        onView(withId(R.id.btnSignUp)).perform(click());

        // Verify that SignUpActivity is launched
        Intents.intended(IntentMatchers.hasComponent(SignUpActivity.class.getName()));
    }

    @Test
    public void testLoginWithValidCredentials() {
        // Enter valid email and password
        onView(withId(R.id.etEmail)).perform(typeText("valid@example.com"));
        onView(withId(R.id.etPassword)).perform(typeText("valid123"));

        // Click the login button
        Log.d("TEST", "About to click login button");
        onView(withId(R.id.btnLogin)).perform(click());
        Log.d("TEST", "Login button clicked");

        // Verify that the ProfileActivity is launched
        Intents.intended(IntentMatchers.hasComponent(ForYouActivity.class.getName()));
    }

    @Test
    public void testLoginWithInvalidCredentials() {
        // Enter invalid email and password
        onView(withId(R.id.etEmail)).perform(typeText("wrong@example.com"));
        onView(withId(R.id.etPassword)).perform(typeText("wrongpassword"));

        // Click the login button
        onView(withId(R.id.btnLogin)).perform(click());

        onView(withId(R.id.etEmail)).check(matches(hasErrorText("Wrong password and/or email.")));
        onView(withId(R.id.etPassword)).check(matches(hasErrorText("Wrong password and/or email.")));
    }

}
