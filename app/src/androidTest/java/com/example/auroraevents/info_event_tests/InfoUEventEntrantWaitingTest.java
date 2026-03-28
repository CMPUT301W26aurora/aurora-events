package com.example.auroraevents.info_event_tests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.example.auroraevents.TestsSupport.setUpEvent;
import static com.example.auroraevents.TestsSupport.signIn;
import static com.example.auroraevents.TestsSupport.takeDownEvent;
import static com.example.auroraevents.info_event_tests.InfoUEventTestsSupport.bottomBarShowTest;

import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.registration_tests.RegistrationListTestsSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;

public class InfoUEventEntrantWaitingTest {
    Event event;
    String deviceId;

    @BeforeClass
    public static void prepare() {
        signIn();
    }

    @Before
    public void before() {
        event = new Event(
                "test device",
                "event info screen test",
                "event for info screen waiting test",
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
        //TODO: get deviceId
        RegistrationListTestsSupport.setUpWaitingList(event.registrationList, deviceId);

        //TODO: open InfoUEventFragment
    }

    @After
    public void after() {
        takeDownEvent(event);
    }

    /**
     * - shows:
     *     - event_deadline
     *     - waiting_list_count
     *     - attendees_count
     *     - leave_button
     *     - lottery_info_button
     * @author Jared Strandlund
     */
    @Test
    public void showsElements() {
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
     * Tests if the leave button works
     * @author Jared Strandlund
     */
    @Test
    public void leaveButton() {
        onView(withId(R.id.leave_button)).perform(click());
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
