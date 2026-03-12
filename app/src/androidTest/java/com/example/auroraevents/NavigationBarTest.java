package com.example.auroraevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.fragment.app.Fragment;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;

import com.example.auroraevents.view.CameraFragment;
import com.example.auroraevents.view.EventListFragment;
import com.example.auroraevents.view.NotificationFragment;
import com.example.auroraevents.view.ProfileFragment;

import androidx.test.rule.GrantPermissionRule;

import android.os.Build;

import org.junit.Rule;
import org.junit.Test;

@LargeTest
public class NavigationBarTest {
    @Rule
    // bypass system notification permission dialogue which may interfere with tests
    public GrantPermissionRule notificationPermissionRule = Build.VERSION.SDK_INT >= 33
            ? GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)
            : GrantPermissionRule.grant();

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);


    @Test
    public void testDefaultFragment_isEventList() {
        // On launch, EventListFragment should be loaded by default
        activityRule.getScenario().onActivity(activity -> {
            Fragment current = activity.getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            assertNotNull("Fragment container should not be empty", current);
            assertTrue(
                    "Default fragment should be EventListFragment",
                    current instanceof EventListFragment
            );
        });
    }

    @Test
    public void testNavBar_isVisible() {
        onView(withId(R.id.nav_bar)).check(matches(isDisplayed()));
    }

    @Test
    public void testAllNavButtons_areVisible() {
        onView(withId(R.id.nav_scan)).check(matches(isDisplayed()));
        onView(withId(R.id.nav_browse)).check(matches(isDisplayed()));
        onView(withId(R.id.nav_notifications)).check(matches(isDisplayed()));
        onView(withId(R.id.nav_profile)).check(matches(isDisplayed()));
    }

    @Test
    public void testTapScan_loadsCameraFragment() {
        onView(withId(R.id.nav_scan)).perform(click());

        activityRule.getScenario().onActivity(activity -> {
            Fragment current = activity.getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            assertTrue(
                    "Tapping Scan should load CameraFragment",
                    current instanceof CameraFragment
            );
        });
    }

    @Test
    public void testTapBrowse_loadsEventListFragment() {
        // Navigate away first, then come back
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.nav_browse)).perform(click());

        activityRule.getScenario().onActivity(activity -> {
            Fragment current = activity.getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            assertTrue(
                    "Tapping Browse should load EventListFragment",
                    current instanceof EventListFragment
            );
        });
    }

    @Test
    public void testTapNotifications_loadsNotificationFragment() {
        onView(withId(R.id.nav_notifications)).perform(click());

        activityRule.getScenario().onActivity(activity -> {
            Fragment current = activity.getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            assertTrue(
                    "Tapping Notifications should load NotificationFragment",
                    current instanceof NotificationFragment
            );
        });
    }

    @Test
    public void testTapProfile_loadsProfileFragment() {
        onView(withId(R.id.nav_profile)).perform(click());

        activityRule.getScenario().onActivity(activity -> {
            Fragment current = activity.getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            assertTrue(
                    "Tapping Profile should load ProfileFragment",
                    current instanceof ProfileFragment
            );
        });
    }


    // Tab switching: ensure only one fragment active at a time ------------------------------------

    @Test
    public void testSwitchingTabs_replacesFragment() {
        onView(withId(R.id.nav_scan)).perform(click());
        onView(withId(R.id.nav_profile)).perform(click());

        activityRule.getScenario().onActivity(activity -> {
            // There should only be one fragment in the container
            int backStackCount = activity.getSupportFragmentManager()
                    .getBackStackEntryCount();
            assertEquals("Back stack should be empty (using replace not add)", 0, backStackCount);

            Fragment current = activity.getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            assertTrue(
                    "Last tapped tab fragment should be active",
                    current instanceof ProfileFragment
            );
        });
    }

    @Test
    public void testTappingSameTab_doesNotStackFragments() {
        onView(withId(R.id.nav_browse)).perform(click());
        onView(withId(R.id.nav_browse)).perform(click());
        onView(withId(R.id.nav_browse)).perform(click());

        activityRule.getScenario().onActivity(activity -> {
            int backStackCount = activity.getSupportFragmentManager()
                    .getBackStackEntryCount();
            assertEquals("Repeated taps should not stack fragments", 0, backStackCount);
        });
    }

    // Active width adjustment: ensure only active tap has expanded width --------------------------

    @Test
    public void testActiveTab_hasExpandedWidth() {
        onView(withId(R.id.nav_notifications)).perform(click());

        activityRule.getScenario().onActivity(activity -> {
            android.widget.ImageButton notifBtn = activity.findViewById(R.id.nav_notifications);
            float widthDp = notifBtn.getLayoutParams().width
                    / activity.getResources().getDisplayMetrics().density;
            assertEquals("Active tab should be 88dp wide", 88f, widthDp, 1.5f);
        });
    }

    @Test
    public void testInactiveTab_hasCollapsedWidth() {
        activityRule.getScenario().onActivity(activity -> {
            android.widget.ImageButton scanBtn = activity.findViewById(R.id.nav_scan);
            float widthDp = scanBtn.getLayoutParams().width
                    / activity.getResources().getDisplayMetrics().density;
            assertEquals("Inactive tab should be 52dp wide", 52f, widthDp, 1.5f);
        });
    }
}
