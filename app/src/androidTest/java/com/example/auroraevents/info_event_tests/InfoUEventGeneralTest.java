package com.example.auroraevents.info_event_tests;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.example.auroraevents.TestsSupport.setUpEvent;
import static com.example.auroraevents.TestsSupport.signIn;
import static com.example.auroraevents.TestsSupport.takeDownEvent;
import static com.example.auroraevents.info_event_tests.InfoUEventTestsSupport.openEvent;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;

import android.graphics.Bitmap;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.auroraevents.MainActivity;
import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.view.InfoUEventFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.time.LocalDateTime;

public class InfoUEventGeneralTest {
    Event event;

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void prepare() {
        signIn();
    }

    @Before
    public void before() {
        event = new Event(
                "not a real user",
                "event info screen test",
                "event for info screen entrant general test",
                "free",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                "testing environment",
                false,
                -1,
                -1,
                null);
        setUpEvent(event);
    }

    @After
    public void after() {
        takeDownEvent(event);
    }

    /**
     * Tests if all the right event info is there
     * @author Jared Strandlund
     */
    @Test
    public void eventInfo() {
        // Set up
        openEvent(scenario, event);

        // Test
        onView(withId(R.id.poster_image)).check(matches(not(isDisplayed())));
        onView(withText(event.getName())).check(matches(isDisplayed()));
        onView(withText(event.getDateTime())).check(matches(isDisplayed()));
        onView(withText(ApplicationProvider.getApplicationContext().getResources().getString(R.string.organized_by_text) + event.getOrganizerDeviceId())).check(matches(isDisplayed()));
        onView(withText(event.getPrice())).check(matches(isDisplayed()));
        onView(withText(event.getLocation())).check(matches(isDisplayed()));
        onView(withText(event.getDescription())).check(matches(isDisplayed()));
    }

    /**
     * Tests if the poster displays
     * @author Jared Strandlund
     */
    @Test
    public void posterView() {
        // Set up
        Bitmap poster = Bitmap.createBitmap(300, 200, Bitmap.Config.ARGB_8888);
        event.setPoster(poster);
        openEvent(scenario, event);

        // Test
        onView(withId(R.id.poster_image)).check(matches(isDisplayed()));
        onData(is(equalTo(poster))).check(matches(isDisplayed()));
    }

    /**
     * Tests if the back button works
     * @author Jared Strandlund
     */
    @Test
    public void backButton() {
        // Set up
        openEvent(scenario, event);

        // Test
        onView(withId(R.id.back_button)).check(matches(isDisplayed()));
        onView(withId(R.id.back_button)).perform(click());
        scenario.getScenario().onActivity(activity -> {
            Fragment current = activity.getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            assertFalse(current instanceof InfoUEventFragment);
        });
    }
}
