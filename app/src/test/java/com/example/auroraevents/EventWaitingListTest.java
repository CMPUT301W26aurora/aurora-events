package com.example.auroraevents;

import static com.example.auroraevents.EventEntrantListsSupport.checkSingle;
import static com.example.auroraevents.EventEntrantListsSupport.setUpAttendingList;
import static com.example.auroraevents.EventEntrantListsSupport.setUpCancelledList;
import static com.example.auroraevents.EventEntrantListsSupport.setUpDeclinedList;
import static com.example.auroraevents.EventEntrantListsSupport.setUpRemovedList;
import static com.example.auroraevents.EventEntrantListsSupport.setUpSelectedList;
import static com.example.auroraevents.EventEntrantListsSupport.setUpWaitingList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Event.waitingList local unit test, which will execute on the development machine (host).
 * @author Jared Strandlund
 * @see Event
 */
public class EventWaitingListTest {
    Event event;
    String entrantID;

    @Before
    public void before() {
        event = new Event();
        entrantID = "aurora";
    }

    /**
     * Tests {@code addToWaitingList()} on an entrant that isn't on any entrant lists
     * @author Jared Strandlund
     */
    @Test
    public void noneToWaitingTest() {
        assertTrue(event.addToWaitingList(entrantID));
        assertEquals(1, event.getWaitingList().size());
        assertTrue(event.getWaitingList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToWaitingList()} on an entrant that is on the waiting list
     * @author Jared Strandlund
     */
    @Test
    public void waitingToWaitingTest() {
        setUpWaitingList(event, entrantID);

        assertFalse(event.addToWaitingList(entrantID));
        assertEquals(1, event.getWaitingList().size());
        assertTrue(event.getWaitingList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToWaitingList()} on an entrant that is on the selected list
     * @author Jared Strandlund
     */
    @Test
    public void selectedToWaitingTest() {
        setUpSelectedList(event, entrantID);

        assertFalse(event.addToWaitingList(entrantID));
        assertEquals(0, event.getWaitingList().size());
        assertFalse(event.getWaitingList().contains(entrantID));
        assertEquals(1, event.getSelectedList().size());
        assertTrue(event.getSelectedList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToWaitingList()} on an entrant that is on the attending list
     * @author Jared Strandlund
     */
    @Test
    public void attendingToWaitingTest() {
        setUpAttendingList(event, entrantID);

        assertFalse(event.addToWaitingList(entrantID));
        assertEquals(0, event.getWaitingList().size());
        assertFalse(event.getWaitingList().contains(entrantID));
        assertEquals(1, event.getAttendingList().size());
        assertTrue(event.getAttendingList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToWaitingList()} on an entrant that is on the declined list
     * @author Jared Strandlund
     */
    @Test
    public void declinedToWaitingTest() {
        setUpDeclinedList(event, entrantID);

        assertFalse(event.addToWaitingList(entrantID));
        assertEquals(0, event.getWaitingList().size());
        assertFalse(event.getWaitingList().contains(entrantID));
        assertEquals(1, event.getDeclinedList().size());
        assertTrue(event.getDeclinedList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToWaitingList()} on an entrant that is on the cancelled list
     * @author Jared Strandlund
     */
    @Test
    public void cancelledToWaitingTest() {
        setUpCancelledList(event, entrantID);

        assertTrue(event.addToWaitingList(entrantID));
        assertEquals(1, event.getWaitingList().size());
        assertTrue(event.getWaitingList().contains(entrantID));
        assertEquals(0, event.getCancelledList().size());
        assertFalse(event.getCancelledList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToWaitingList()} on an entrant that is on the removed list
     * @author Jared Strandlund
     */
    @Test
    public void removedToWaitingTest() {
        setUpRemovedList(event, entrantID);

        assertFalse(event.addToWaitingList(entrantID));
        assertEquals(0, event.getWaitingList().size());
        assertFalse(event.getWaitingList().contains(entrantID));
        assertEquals(1, event.getRemovedList().size());
        assertTrue(event.getRemovedList().contains(entrantID));

        checkSingle(event, entrantID);
    }
}