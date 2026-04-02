package com.example.auroraevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.assertTrue;

import android.os.Build;

import androidx.fragment.app.Fragment;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import com.example.auroraevents.view.AdminCommentFragment;
import com.example.auroraevents.view.AdminEventFragment;
import com.example.auroraevents.view.AdminImageFragment;
import com.example.auroraevents.view.AdminOrganizerFragment;
import com.example.auroraevents.view.AdminProfileFragment;
import com.example.auroraevents.view.CameraFragment;
import com.example.auroraevents.view.ProfileFragment;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AdminBarTest {
    @Rule
    public GrantPermissionRule notificationPermissionRule = Build.VERSION.SDK_INT >= 33
            ? GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)
            : GrantPermissionRule.grant();
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);
    @Before
    public void before() {
        //will add new tester function with mockito; for now, if running this test
        //please ensure you set isAdmin = True before running otherwise these will fail...
        onView(withId(R.id.nav_browse)).check(matches(isDisplayed()));
        onView(withId(R.id.nav_profile)).perform(click());
        //Use of thread sleep is REALLY bad, but since using the live server for now
        //It will have to be included due to server delay, even then it acts strangly
        //So just trust me I guess lol
        try {Thread.sleep(2000);} catch (InterruptedException e) {}

    }

    @Test
    public void seeVisibility(){
        onView(withId(R.id.switch_to_admin_button)).check(matches(isDisplayed()));
    }

    @Test
    public void toggleAdminOn(){
        onView(withId(R.id.switch_to_admin_button)).perform(click());
        onView(withId(R.id.nav_bar_admin)).check(matches(isDisplayed()));
        onView(withId(R.id.nav_bar)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }
    @Test
    public void toggleAdminOff(){
        onView(withId(R.id.switch_to_admin_button)).perform(click());
        onView(withId(R.id.switch_to_admin_button)).perform(click());
        onView(withId(R.id.nav_bar)).check(matches(isDisplayed()));
        onView(withId(R.id.nav_bar_admin)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    private void testNavButtonLoadsFragment(int buttonId, Class<? extends Fragment> fragmentClass) {
        onView(withId(R.id.switch_to_admin_button)).perform(click());
        onView(withId(buttonId)).perform(click());

        activityRule.getScenario().onActivity(activity -> {
            Fragment current = activity.getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            assertTrue(
                    "Expected fragment: " + fragmentClass.getSimpleName(),
                    fragmentClass.isInstance(current)
            );
        });
    }

    @Test
    public void testAdminBrowseProfileLoads() {
        testNavButtonLoadsFragment(R.id.nav_admin_browse_profiles, AdminProfileFragment.class);
    }

    @Test
    public void testAdminOrganizerLoads() {
        testNavButtonLoadsFragment(R.id.nav_admin_organizer, AdminOrganizerFragment.class);
    }

    @Test
    public void testAdminImageLoads() {
        testNavButtonLoadsFragment(R.id.nav_admin_image, AdminImageFragment.class);
    }

    @Test
    public void testAdminEventLoads() {
        testNavButtonLoadsFragment(R.id.nav_admin_event, AdminEventFragment.class);
    }

    @Test
    public void testAdminCommentLoads() {
        testNavButtonLoadsFragment(R.id.nav_admin_comments, AdminCommentFragment.class);
    }

    @Test
    public void testAdminProfileLoads() {
        testNavButtonLoadsFragment(R.id.nav_profile_admin, ProfileFragment.class);
    }


}
