package com.example.auroraevents;

import static com.example.auroraevents.RegistrationListTestsSupport.checkNone;
import static com.example.auroraevents.RegistrationListTestsSupport.checkSingle;
import static com.example.auroraevents.RegistrationListTestsSupport.setUpEvent;
import static com.example.auroraevents.RegistrationListTestsSupport.setUpAttendingList;
import static com.example.auroraevents.RegistrationListTestsSupport.setUpCancelledList;
import static com.example.auroraevents.RegistrationListTestsSupport.setUpDeclinedList;
import static com.example.auroraevents.RegistrationListTestsSupport.setUpRemovedList;
import static com.example.auroraevents.RegistrationListTestsSupport.setUpSelectedList;
import static com.example.auroraevents.RegistrationListTestsSupport.setUpWaitingList;
import static com.example.auroraevents.RegistrationListTestsSupport.takeDownEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class RegistrationDeclinedListTest {
    Event event;
    RegistrationList list;
    String entrantID;

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
     * Tests {@code addToDeclinedList()} on an entrant that isn't on any entrant lists
     * @author Jared Strandlund
     */
    @Test
    public void noneToDeclinedTest() {
        assertEquals(1, list.addToDeclinedList(entrantID));
        assertEquals(0, list.getDeclinedList().size());
        assertFalse(list.getDeclinedList().contains(entrantID));

        checkNone(list, entrantID);
    }

    /**
     * Tests {@code addToDeclinedList()} on an entrant that is on the waiting list
     * @author Jared Strandlund
     */
    @Test
    public void waitingToDeclinedTest() {
        setUpWaitingList(list, entrantID);

        assertEquals(1, list.addToDeclinedList(entrantID));
        assertEquals(0, list.getDeclinedList().size());
        assertFalse(list.getDeclinedList().contains(entrantID));
        assertEquals(1, list.getWaitingList().size());
        assertTrue(list.getWaitingList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToDeclinedList()} on an entrant that is on the selected list
     * @author Jared Strandlund
     */
    @Test
    public void selectedToDeclinedTest() {
        setUpSelectedList(list, entrantID);

        assertEquals(0, list.addToDeclinedList(entrantID));
        assertEquals(1, list.getDeclinedList().size());
        assertTrue(list.getDeclinedList().contains(entrantID));
        assertEquals(0, list.getSelectedList().size());
        assertFalse(list.getSelectedList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToDeclinedList()} on an entrant that is on the attending list
     * @author Jared Strandlund
     */
    @Test
    public void attendingToDeclinedTest() {
        setUpAttendingList(list, entrantID);

        assertEquals(0, list.addToDeclinedList(entrantID));
        assertEquals(0, list.getDeclinedList().size());
        assertFalse(list.getDeclinedList().contains(entrantID));
        assertEquals(1, list.getAttendingList().size());
        assertTrue(list.getAttendingList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToDeclinedList()} on an entrant that is on the declined list
     * @author Jared Strandlund
     */
    @Test
    public void declinedToDeclinedTest() {
        setUpDeclinedList(list, entrantID);

        assertEquals(-1, list.addToDeclinedList(entrantID));
        assertEquals(1, list.getDeclinedList().size());
        assertTrue(list.getDeclinedList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToDeclinedList()} on an entrant that is on the cancelled list
     * @author Jared Strandlund
     */
    @Test
    public void cancelledToDeclinedTest() {
        setUpCancelledList(list, entrantID);

        assertEquals(1, list.addToDeclinedList(entrantID));
        assertEquals(0, list.getDeclinedList().size());
        assertFalse(list.getDeclinedList().contains(entrantID));
        assertEquals(1, list.getCancelledList().size());
        assertTrue(list.getCancelledList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToDeclinedList()} on an entrant that is on the removed list
     * @author Jared Strandlund
     */
    @Test
    public void removedToDeclinedTest() {
        setUpRemovedList(list, entrantID);

        assertEquals(1, list.addToDeclinedList(entrantID));
        assertEquals(0, list.getDeclinedList().size());
        assertFalse(list.getDeclinedList().contains(entrantID));
        assertEquals(1, list.getRemovedList().size());
        assertTrue(list.getRemovedList().contains(entrantID));

        checkSingle(list, entrantID);
    }
}
