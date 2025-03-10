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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

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
    }

    @Before
    public void seedDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userTestRef = db.collection("user_test");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword("valid@example.com", "valid123");

        User user = new User("valid@example.com", "valid123");
        userTestRef.document().set(user);
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
        // Enter valid email, username, and password
        onView(withId(R.id.etEmail)).perform(typeText("idk@example.com"));
        onView(withId(R.id.etUsername)).perform(typeText("valid123"));
        onView(withId(R.id.etPassword)).perform(typeText("test123"));

        // Click the sign-up button
        Log.d("TEST", "About to click sign-up button");
        onView(withId(R.id.btnSignup)).perform(click());
        Log.d("TEST", "Sign-up button clicked");

        // Verify that ProfileActivity is launched
        Intents.intended(IntentMatchers.hasComponent(ProfileActivity.class.getName()));
    }

    @Test
    public void testSignUpWithExistingUsername() {
        // Enter an email and username that already exist in the database
        onView(withId(R.id.etEmail)).perform(typeText("test2@example.com"));
        onView(withId(R.id.etUsername)).perform(typeText("test"));
        onView(withId(R.id.etPassword)).perform(typeText("test321"));

        // Click the sign-up button
        onView(withId(R.id.btnSignup)).perform(click());

        // Verify that an error message is displayed
        onView(withId(R.id.etUsername)).check(matches(hasErrorText("Username already exists")));
    }

}
