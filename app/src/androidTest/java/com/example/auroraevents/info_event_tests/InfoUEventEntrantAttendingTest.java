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

public class InfoUEventEntrantAttendingTest {
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
        RegistrationListTestsSupport.setUpAttendingList(event.registrationList, deviceId);

        //TODO: open InfoUEventFragment
    }

    @After
    public void after() {
        takeDownEvent(event);
    }

    /**
     * - shows:
     *     - attendees_count
     *     - attending_label
     * @author Jared Strandlund
     */
    @Test
    public void showsElements() {
        bottomBarShowTest(
                false,
                false,
                true,
                false,
                false,
                false,
                true,
                false,
                false
        );
    }
}
