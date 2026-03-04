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

public class EventAttendingListTest {
    Event event;
    String entrantID;

    @Before
    public void before() {
        event = new Event();
        entrantID = "aurora";
    }

    /**
     * Tests {@code addToAttendingList()} on an entrant that isn't on any entrant lists
     * @author Jared Strandlund
     */
    @Test
    public void noneToAttendingTest() {
        assertFalse(event.addToAttendingList(entrantID));
        assertEquals(0, event.getAttendingList().size());
        assertFalse(event.getAttendingList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToAttendingList()} on an entrant that is on the waiting list
     * @author Jared Strandlund
     */
    @Test
    public void waitingToAttendingTest() {
        setUpWaitingList(event, entrantID);

        assertFalse(event.addToAttendingList(entrantID));
        assertEquals(0, event.getAttendingList().size());
        assertFalse(event.getAttendingList().contains(entrantID));
        assertEquals(1, event.getWaitingList().size());
        assertTrue(event.getWaitingList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToAttendingList()} on an entrant that is on the selected list
     * @author Jared Strandlund
     */
    @Test
    public void selectedToAttendingTest() {
        setUpSelectedList(event, entrantID);

        assertTrue(event.addToAttendingList(entrantID));
        assertEquals(1, event.getAttendingList().size());
        assertTrue(event.getAttendingList().contains(entrantID));
        assertEquals(0, event.getSelectedList().size());
        assertFalse(event.getSelectedList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToAttendingList()} on an entrant that is on the attending list
     * @author Jared Strandlund
     */
    @Test
    public void attendingToAttendingTest() {
        setUpAttendingList(event, entrantID);

        assertFalse(event.addToAttendingList(entrantID));
        assertEquals(1, event.getAttendingList().size());
        assertTrue(event.getAttendingList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToAttendingList()} on an entrant that is on the declined list
     * @author Jared Strandlund
     */
    @Test
    public void declinedToAttendingTest() {
        setUpDeclinedList(event, entrantID);

        assertFalse(event.addToAttendingList(entrantID));
        assertEquals(0, event.getAttendingList().size());
        assertFalse(event.getAttendingList().contains(entrantID));
        assertEquals(1, event.getDeclinedList().size());
        assertTrue(event.getDeclinedList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToAttendingList()} on an entrant that is on the cancelled list
     * @author Jared Strandlund
     */
    @Test
    public void cancelledToAttendingTest() {
        setUpCancelledList(event, entrantID);

        assertFalse(event.addToAttendingList(entrantID));
        assertEquals(0, event.getAttendingList().size());
        assertFalse(event.getAttendingList().contains(entrantID));
        assertEquals(1, event.getCancelledList().size());
        assertTrue(event.getCancelledList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToAttendingList()} on an entrant that is on the removed list
     * @author Jared Strandlund
     */
    @Test
    public void removedToAttendingTest() {
        setUpRemovedList(event, entrantID);

        assertFalse(event.addToAttendingList(entrantID));
        assertEquals(0, event.getAttendingList().size());
        assertFalse(event.getAttendingList().contains(entrantID));
        assertEquals(1, event.getRemovedList().size());
        assertTrue(event.getRemovedList().contains(entrantID));

        checkSingle(event, entrantID);
    }
}
