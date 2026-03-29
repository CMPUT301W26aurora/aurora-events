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
import static com.example.auroraevents.info_event_tests.InfoUEventTestsSupport.bottomBarShowTest;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;

public class InfoUEventEntrantCannotAttendTest {
    Event event;

    @BeforeClass
    public static void prepare() {
        signIn();
    }

    @Before
    public void before() {

    }

    @After
    public void after() {
        takeDownEvent(event);
    }

    /**
     * - shows:
     *     - cannot_attend_label
     * @author Jared Strandlund
     */
    @Test
    public void onRemovedList() {
        // Set up
        event = new Event(
                "dummy",
                "event info screen test",
                "event for info screen attending test",
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

        onView(withId(R.id.nav_browse)).perform(click());
        onData(is(equalTo(event))).inAdapterView(withId(R.id.events_list)).perform(click());
        onView(withText(event.getName())).check(matches(isDisplayed()));

        // Test
        bottomBarShowTest(
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                true,
                false
        );
    }

    /**
     * - shows:
     *     - attendees_count
     *     - cannot_attend_label
     *     - lottery_info_button
     * @author Jared Strandlund
     */
    @Test
    public void noSlots() {
        // Set up
        event = new Event(
                "dummy",
                "event info screen test",
                "event for info screen attending test",
                "free",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                "testing environment",
                false,
                -1,
                0,
                null);
        setUpEvent(event);

        onView(withId(R.id.nav_browse)).perform(click());
        onData(is(equalTo(event))).inAdapterView(withId(R.id.events_list)).perform(click());
        onView(withText(event.getName())).check(matches(isDisplayed()));

        // Test
        bottomBarShowTest(
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                true,
                true
        );
    }

    /**
     * - shows:
     *     - waiting_list_count
     *     - attendees_count
     *     - cannot_attend_label
     *     - lottery_info_button
     * @author Jared Strandlund
     */
    @Test
    public void noWaitingRoom() {
        // Set up
        event = new Event(
                "dummy",
                "event info screen test",
                "event for info screen attending test",
                "free",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                "testing environment",
                false,
                0,
                -1,
                null);
        setUpEvent(event);

        onView(withId(R.id.nav_browse)).perform(click());
        onData(is(equalTo(event))).inAdapterView(withId(R.id.events_list)).perform(click());
        onView(withText(event.getName())).check(matches(isDisplayed()));

        // Test
        bottomBarShowTest(
                false,
                true,
                true,
                false,
                false,
                false,
                false,
                true,
                true
        );
    }

    /**
     * - shows:
     *     - event_deadline
     *     - cannot_attend_label
     *     - lottery_info_button
     * @author Jared Strandlund
     */
    @Test
    public void registrationClosed() {
        // Set up
        event = new Event(
                "dummy",
                "event info screen test",
                "event for info screen attending test",
                "free",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                "testing environment",
                false,
                -1,
                -1,
                null);
        setUpEvent(event);

        onView(withId(R.id.nav_browse)).perform(click());
        onData(is(equalTo(event))).inAdapterView(withId(R.id.events_list)).perform(click());
        onView(withText(event.getName())).check(matches(isDisplayed()));

        // Test
        bottomBarShowTest(
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                true,
                true
        );
    }
}
