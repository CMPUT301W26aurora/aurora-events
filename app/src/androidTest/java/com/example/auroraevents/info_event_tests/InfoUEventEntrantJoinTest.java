package com.example.auroraevents.info_event_tests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.example.auroraevents.TestsSupport.setUpEvent;
import static com.example.auroraevents.TestsSupport.signIn;
import static com.example.auroraevents.TestsSupport.takeDownEvent;
import static com.example.auroraevents.info_event_tests.InfoUEventTestsSupport.bottomBarShowTest;
import static com.example.auroraevents.info_event_tests.InfoUEventTestsSupport.openEvent;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.auroraevents.MainActivity;
import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.time.LocalDateTime;

public class InfoUEventEntrantJoinTest {
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
                "dummy",
                "event info screen test",
                "event for info screen join test",
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
     * Tests if the info button popup works
     * @author Jared Strandlund
     */
    @Test
    public void infoButton() {
        // Set up
        openEvent(scenario, event);

        // Test
        onView(withId(R.id.lottery_info_button)).check(matches(isDisplayed()));
        onView(withId(R.id.lottery_info_button)).perform(click());
        onView(withText(R.string.lottery_info_title)).check(matches(isDisplayed()));
        onView(withText(R.string.lottery_info_description)).check(matches(isDisplayed()));
        onView(withText(R.string.okay_button_text)).perform(click());
        onView(withText(R.string.lottery_info_title)).check(doesNotExist());
        onView(withText(R.string.lottery_info_description)).check(doesNotExist());
    }

    /**
     * Tests if the join button works when the user is on no lists of the event
     * @author Jared Strandlund
     */
    @Test
    public void onNoList() {
        // Set up
        openEvent(scenario, event);

        // Test
        bottomBarShowTest(
                true,
                true,
                true,
                true,
                false,
                false,
                false,
                false,
                true
        );
        onView(withId(R.id.join_button)).perform(click());
        bottomBarShowTest(
                true,
                true,
                true,
                false,
                true,
                false,
                false,
                false,
                true
        );
    }

    /**
     * Tests if the join button works when the user is on the cancelled list
     * @author Jared Strandlund
     */
    @Test
    public void onCancelledList() {
        // Set up
        //TODO: add to cancelled list
        fail("TODO: add to cancelled list");
        openEvent(scenario, event);

        // Test
        bottomBarShowTest(
                true,
                true,
                true,
                true,
                false,
                false,
                false,
                false,
                true
        );
        onView(withId(R.id.join_button)).perform(click());
        bottomBarShowTest(
                true,
                true,
                true,
                false,
                true,
                false,
                false,
                false,
                true
        );
    }

    /**
     * Tests if the join button works when the user is on the declined list
     * @author Jared Strandlund
     */
    @Test
    public void onDeclinedList() {
        // Set up
        //TODO: add to declined list
        fail("TODO: add to declined list");
        openEvent(scenario, event);

        // Test
        bottomBarShowTest(
                true,
                true,
                true,
                true,
                false,
                false,
                false,
                false,
                true
        );
        onView(withId(R.id.join_button)).perform(click());
        bottomBarShowTest(
                true,
                true,
                true,
                false,
                true,
                false,
                false,
                false,
                true
        );
    }
}
