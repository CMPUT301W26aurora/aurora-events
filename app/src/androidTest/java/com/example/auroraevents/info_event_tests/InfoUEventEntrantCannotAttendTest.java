package com.example.auroraevents.info_event_tests;

import static com.example.auroraevents.TestsSupport.setUpEvent;
import static com.example.auroraevents.TestsSupport.signIn;
import static com.example.auroraevents.TestsSupport.takeDownEvent;
import static com.example.auroraevents.info_event_tests.InfoUEventTestsSupport.bottomBarShowTest;

import com.example.auroraevents.model.Event;
import com.example.auroraevents.registration_tests.RegistrationListTestsSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;

public class InfoUEventEntrantCannotAttendTest {
    Event event;
    String deviceId;

    @BeforeClass
    public static void prepare() {
        signIn();
    }

    @Before
    public void before() {
        //TODO: open InfoUEventFragment
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
                "test device",
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
        //TODO: get deviceId
        RegistrationListTestsSupport.setUpRemovedList(event.registrationList, deviceId);

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
                "test device",
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
                "test device",
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
                "test device",
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
