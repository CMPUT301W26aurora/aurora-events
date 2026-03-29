package com.example.auroraevents.info_event_tests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
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

public class InfoUEventEntrantSelectedTest {
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
                "event for info screen selected entrant test",
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

        //TODO: add to selected list
        fail("TODO: add to selected list");

        openEvent(scenario, event);
    }

    @After
    public void after() {
        takeDownEvent(event);
    }

    /**
     * Tests if the accept button goes to accepted
     * @author Jared Strandlund
     */
    @Test
    public void acceptButton() {
        bottomBarShowTest(
                true,
                true,
                true,
                false,
                false,
                true,
                false,
                false,
                true
        );
        onView(withId(R.id.accept_button)).perform(click());
        bottomBarShowTest(
                true,
                true,
                true,
                false,
                false,
                false,
                true,
                false,
                false
        );
    }

    /**
     * Tests if the decline button goes back to join button
     * @author Jared Strandlund
     */
    @Test
    public void declineButton() {
        bottomBarShowTest(
                true,
                true,
                true,
                false,
                false,
                true,
                false,
                false,
                true
        );
        onView(withId(R.id.decline_button)).perform(click());
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
    }
}
