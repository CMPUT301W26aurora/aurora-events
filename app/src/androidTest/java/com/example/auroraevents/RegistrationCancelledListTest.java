package com.example.auroraevents;

import static com.example.auroraevents.RegistrationListTestsSupport.checkNone;
import static com.example.auroraevents.RegistrationListTestsSupport.checkSingle;
import static com.example.auroraevents.TestsSupport.setUpEvent;
import static com.example.auroraevents.RegistrationListTestsSupport.setUpAttendingList;
import static com.example.auroraevents.RegistrationListTestsSupport.setUpCancelledList;
import static com.example.auroraevents.RegistrationListTestsSupport.setUpDeclinedList;
import static com.example.auroraevents.RegistrationListTestsSupport.setUpRemovedList;
import static com.example.auroraevents.RegistrationListTestsSupport.setUpSelectedList;
import static com.example.auroraevents.RegistrationListTestsSupport.setUpWaitingList;
import static com.example.auroraevents.TestsSupport.signIn;
import static com.example.auroraevents.TestsSupport.takeDownEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Date;

public class RegistrationCancelledListTest {
    Event event;
    RegistrationList list;
    String entrantID;

    @BeforeClass
    public static void prepare() {
        signIn();
    }

    @Before
    public void before() {
        event = new Event(
                "test device",
                "registration test",
                "event for registration cancelled list test",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                "testing environment",
                0);
        setUpEvent(event);
        list = event.registrationList;
        entrantID = "aurora";
    }

    @After
    public void after() {
        takeDownEvent(event);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that isn't on any entrant lists
     * @author Jared Strandlund
     */
    @Test
    public void noneToCancelledTest() {
        assertEquals(1, list.addToCancelledList(entrantID));
        assertEquals(0, list.getCancelledList().size());
        assertFalse(list.getCancelledList().contains(entrantID));

        checkNone(list, entrantID);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that is on the waiting list
     * @author Jared Strandlund
     */
    @Test
    public void waitingToCancelledTest() {
        setUpWaitingList(list, entrantID);

        assertEquals(0, list.addToCancelledList(entrantID));
        assertEquals(1, list.getCancelledList().size());
        assertTrue(list.getCancelledList().contains(entrantID));
        assertEquals(0, list.getWaitingList().size());
        assertFalse(list.getWaitingList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that is on the selected list
     * @author Jared Strandlund
     */
    @Test
    public void selectedToCancelledTest() {
        setUpSelectedList(list, entrantID);

        assertEquals(1, list.addToCancelledList(entrantID));
        assertEquals(0, list.getCancelledList().size());
        assertFalse(list.getCancelledList().contains(entrantID));
        assertEquals(1, list.getSelectedList().size());
        assertTrue(list.getSelectedList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that is on the attending list
     * @author Jared Strandlund
     */
    @Test
    public void attendingToCancelledTest() {
        setUpAttendingList(list, entrantID);

        assertEquals(1, list.addToCancelledList(entrantID));
        assertEquals(0, list.getCancelledList().size());
        assertFalse(list.getCancelledList().contains(entrantID));
        assertEquals(1, list.getAttendingList().size());
        assertTrue(list.getAttendingList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that is on the declined list
     * @author Jared Strandlund
     */
    @Test
    public void declinedToCancelledTest() {
        setUpDeclinedList(list, entrantID);

        assertEquals(1, list.addToCancelledList(entrantID));
        assertEquals(0, list.getCancelledList().size());
        assertFalse(list.getCancelledList().contains(entrantID));
        assertEquals(1, list.getDeclinedList().size());
        assertTrue(list.getDeclinedList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that is on the cancelled list
     * @author Jared Strandlund
     */
    @Test
    public void cancelledToCancelledTest() {
        setUpCancelledList(list, entrantID);

        assertEquals(-1, list.addToCancelledList(entrantID));
        assertEquals(1, list.getCancelledList().size());
        assertTrue(list.getCancelledList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that is on the removed list
     * @author Jared Strandlund
     */
    @Test
    public void removedToCancelledTest() {
        setUpRemovedList(list, entrantID);

        assertEquals(1, list.addToCancelledList(entrantID));
        assertEquals(0, list.getCancelledList().size());
        assertFalse(list.getCancelledList().contains(entrantID));
        assertEquals(1, list.getRemovedList().size());
        assertTrue(list.getRemovedList().contains(entrantID));

        checkSingle(list, entrantID);
    }
}
