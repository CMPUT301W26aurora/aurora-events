package com.example.auroraevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

import android.os.Build;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;

/**
 * UI tests for:
 *   - Notification opt in / opt out buttons in NotificationFragment
 */
@LargeTest
public class NotificationUITest {

    @Rule
    public GrantPermissionRule notificationPermissionRule = Build.VERSION.SDK_INT >= 33
            ? GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)
            : GrantPermissionRule.grant();

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    // ─── NotificationFragment: opt in / opt out buttons ───────────────────────

    /**
     * Navigating to the notification tab should show the notification screen.
     */
    @Test
    public void testNotificationScreen_isVisible() {
        onView(withId(R.id.nav_notifications)).perform(click());
        onView(withId(R.id.notifications_list)).check(matches(isDisplayed()));
    }

    /**
     * When notifications are granted (via GrantPermissionRule above),
     * the Opt Out button should be visible and Opt In hidden.
     */
    @Test
    public void testOptOutButton_visibleWhenNotificationsEnabled() {
        onView(withId(R.id.nav_notifications)).perform(click());
        onView(withId(R.id.btn_opt_out)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_opt_in)).check(matches(not(isDisplayed())));
    }

    /**
     * Only one of opt in / opt out should be visible at a time.
     */
    @Test
    public void testOnlyOneToggleButton_visibleAtATime() {
        onView(withId(R.id.nav_notifications)).perform(click());

        activityRule.getScenario().onActivity(activity -> {
            android.view.View optIn  = activity.findViewById(R.id.btn_opt_in);
            android.view.View optOut = activity.findViewById(R.id.btn_opt_out);

            boolean onlyOneVisible =
                    (optIn.getVisibility()  == android.view.View.VISIBLE) !=
                    (optOut.getVisibility() == android.view.View.VISIBLE);

            assertTrue("Only one of opt in / opt out should be visible", onlyOneVisible);
        });
    }
}
