package com.example.z;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.SystemClock;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;


import com.example.z.views.HomeActivity;
import com.example.z.views.MapActivity;
import com.example.z.views.NotificationActivity;
import com.example.z.views.ProfileActivity;
import com.example.z.views.SearchActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Intent tests to verify navigation between activities.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestNav {

    @Rule
    public ActivityScenarioRule<ProfileActivity> activityScenarioRule =
            new ActivityScenarioRule<>(ProfileActivity.class);

    @Before
    public void setUp() {
        // Initialize Espresso Intents before each test
        Intents.init();
    }

    @After
    public void tearDown() {
        // Release Intents after each test
        Intents.release();
    }

    /**
     * Test Navigation: Profile → Home Page
     */
    @Test
    public void testNavigationToHomePage() {
        SystemClock.sleep(2000);
        // Click the Home Button
        onView(withId(R.id.nav_home)).perform(click());
        SystemClock.sleep(2000);
        // Verify that HomeActivity is launched
        intended(hasComponent(HomeActivity.class.getName()));
    }

    /**
     * Test Navigation: Profile → Notification Page
     */
    @Test
    public void testNavigationToNotificationPage() {
        SystemClock.sleep(2000);
        // Click the Notification Button
        onView(withId(R.id.nav_notifications)).perform(click());
        SystemClock.sleep(2000);
        // Verify that NotificationActivity is launched
        intended(hasComponent(NotificationActivity.class.getName()));
    }

    /**
     * Test Navigation: Profile → Search Page
     */
    @Test
    public void testNavigationToSearchPage() {
        SystemClock.sleep(2000);
        // Click the Search Button
        onView(withId(R.id.nav_search)).perform(click());
        SystemClock.sleep(2000);
        // Verify that SearchActivity is launched
        intended(hasComponent(SearchActivity.class.getName()));
    }

    /**
     * Test Navigation: Profile → MapActivity
     */
    @Test
    public void testNavigationToMapActivityFromProfile() {
        SystemClock.sleep(2000);
        // Click the Map Button
        onView(withId(R.id.btnMapMoods)).perform(click());
        SystemClock.sleep(2000);
        // Verify that MapActivity is launched
        intended(hasComponent(MapActivity.class.getName()));
    }

    /**
     * Test Navigation: Profile → Home → MapActivity
     */
    @Test
    public void testNavigationToMapActivityFromHome() {
        SystemClock.sleep(2000);
        // Click the Home Button
        onView(withId(R.id.nav_home)).perform(click());
        SystemClock.sleep(2000);
        // Verify HomeActivity is launched
        intended(hasComponent(HomeActivity.class.getName()));
        SystemClock.sleep(2000);
        // Click the Map Button from HomeActivity
        onView(withId(R.id.btnMap)).perform(click());
        SystemClock.sleep(2000);
        // Verify that MapActivity is launched
        intended(hasComponent(MapActivity.class.getName()));
    }
}

