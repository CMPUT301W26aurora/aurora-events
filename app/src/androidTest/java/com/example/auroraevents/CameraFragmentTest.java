package com.example.auroraevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.Build;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import com.example.auroraevents.view.CameraFragment;


import org.junit.Rule;
import org.junit.Test;

public class CameraFragmentTest {
    @Rule
    // bypass system notification permission dialogue which may interfere with tests
    public GrantPermissionRule notificationPermissionRule = Build.VERSION.SDK_INT >= 33
            ? GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)
            : GrantPermissionRule.grant();

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);


    /**
     *Tests to see if scanner pops up on button press
     */
    @Test
    public void testScanbtn(){

        onView(withId(R.id.nav_scan)).perform(click());
        onView(withId(R.id.scan)).check(matches(isDisplayed())).check(matches(isClickable()));
        onView(withId(R.id.scan)).perform(click()); //the scanner breaks everything in terms of testing, very nice
    }

    @Test
    public void testInvalid(){

    }
}
