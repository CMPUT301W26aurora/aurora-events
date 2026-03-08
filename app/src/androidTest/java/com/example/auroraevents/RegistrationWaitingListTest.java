package com.example.auroraevents;

import static com.example.auroraevents.RegistrationListTestsSupport.setUpEvent;
import static com.example.auroraevents.TestsSupport.signIn;
import static com.example.auroraevents.RegistrationListTestsSupport.takeDownEvent;
import static com.example.auroraevents.RegistrationListTestsSupport.checkSingle;
import static com.example.auroraevents.RegistrationListTestsSupport.setUpAttendingList;
import static com.example.auroraevents.RegistrationListTestsSupport.setUpCancelledList;
import static com.example.auroraevents.RegistrationListTestsSupport.setUpDeclinedList;
import static com.example.auroraevents.RegistrationListTestsSupport.setUpRemovedList;
import static com.example.auroraevents.RegistrationListTestsSupport.setUpSelectedList;
import static com.example.auroraevents.RegistrationListTestsSupport.setUpWaitingList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

/**
 * Event.waitingList local unit test, which will execute on the development machine (host).
 * @author Jared Strandlund
 * @see RegistrationList
 */
public class RegistrationWaitingListTest {
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
                "event for registration test",
                new Date(),
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
     * Tests {@code addToWaitingList()} on an entrant that isn't on any entrant lists
     * @author Jared Strandlund
     */
    @Test
    public void noneToWaitingTest() {
        assertEquals(0, list.addToWaitingList(entrantID));
        assertEquals(1, list.getWaitingList().size());
        assertTrue(list.getWaitingList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToWaitingList()} on an entrant that is on the waiting list
     * @author Jared Strandlund
     */
    @Test
    public void waitingToWaitingTest() {
        setUpWaitingList(list, entrantID);

        assertEquals(-1, list.addToWaitingList(entrantID));
        assertEquals(1, list.getWaitingList().size());
        assertTrue(list.getWaitingList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToWaitingList()} on an entrant that is on the selected list
     * @author Jared Strandlund
     */
    @Test
    public void selectedToWaitingTest() {
        setUpSelectedList(list, entrantID);

        assertEquals(1, list.addToWaitingList(entrantID));
        assertEquals(0, list.getWaitingList().size());
        assertFalse(list.getWaitingList().contains(entrantID));
        assertEquals(1, list.getSelectedList().size());
        assertTrue(list.getSelectedList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToWaitingList()} on an entrant that is on the attending list
     * @author Jared Strandlund
     */
    @Test
    public void attendingToWaitingTest() {
        setUpAttendingList(list, entrantID);

        assertEquals(1, list.addToWaitingList(entrantID));
        assertEquals(0, list.getWaitingList().size());
        assertFalse(list.getWaitingList().contains(entrantID));
        assertEquals(1, list.getAttendingList().size());
        assertTrue(list.getAttendingList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToWaitingList()} on an entrant that is on the declined list
     * @author Jared Strandlund
     */
    @Test
    public void declinedToWaitingTest() {
        setUpDeclinedList(list, entrantID);

        assertEquals(1, list.addToWaitingList(entrantID));
        assertEquals(0, list.getWaitingList().size());
        assertFalse(list.getWaitingList().contains(entrantID));
        assertEquals(1, list.getDeclinedList().size());
        assertTrue(list.getDeclinedList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToWaitingList()} on an entrant that is on the cancelled list
     * @author Jared Strandlund
     */
    @Test
    public void cancelledToWaitingTest() {
        setUpCancelledList(list, entrantID);

        assertEquals(0, list.addToWaitingList(entrantID));
        assertEquals(1, list.getWaitingList().size());
        assertTrue(list.getWaitingList().contains(entrantID));
        assertEquals(0, list.getCancelledList().size());
        assertFalse(list.getCancelledList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToWaitingList()} on an entrant that is on the removed list
     * @author Jared Strandlund
     */
    @Test
    public void removedToWaitingTest() {
        setUpRemovedList(list, entrantID);

        assertEquals(1, list.addToWaitingList(entrantID));
        assertEquals(0, list.getWaitingList().size());
        assertFalse(list.getWaitingList().contains(entrantID));
        assertEquals(1, list.getRemovedList().size());
        assertTrue(list.getRemovedList().contains(entrantID));

        checkSingle(list, entrantID);
    }
}