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

public class EventRemovedListTest {
    Event event;
    String entrantID;

    @Before
    public void before() {
        event = new Event();
        entrantID = "aurora";
    }

    /**
     * Tests {@code addToRemovedList()} on an entrant that isn't on any entrant lists
     * @author Jared Strandlund
     */
    @Test
    public void noneToRemovedTest() {
        assertTrue(event.addToRemovedList(entrantID));
        assertEquals(1, event.getRemovedList().size());
        assertTrue(event.getRemovedList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToRemovedList()} on an entrant that is on the waiting list
     * @author Jared Strandlund
     */
    @Test
    public void waitingToRemovedTest() {
        setUpWaitingList(event, entrantID);

        assertTrue(event.addToRemovedList(entrantID));
        assertEquals(1, event.getRemovedList().size());
        assertTrue(event.getRemovedList().contains(entrantID));
        assertEquals(0, event.getWaitingList().size());
        assertFalse(event.getWaitingList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToRemovedList()} on an entrant that is on the selected list
     * @author Jared Strandlund
     */
    @Test
    public void selectedToRemovedTest() {
        setUpSelectedList(event, entrantID);

        assertTrue(event.addToRemovedList(entrantID));
        assertEquals(1, event.getRemovedList().size());
        assertTrue(event.getRemovedList().contains(entrantID));
        assertEquals(0, event.getSelectedList().size());
        assertFalse(event.getSelectedList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToRemovedList()} on an entrant that is on the attending list
     * @author Jared Strandlund
     */
    @Test
    public void attendingToRemovedTest() {
        setUpAttendingList(event, entrantID);

        assertTrue(event.addToRemovedList(entrantID));
        assertEquals(1, event.getRemovedList().size());
        assertTrue(event.getRemovedList().contains(entrantID));
        assertEquals(0, event.getAttendingList().size());
        assertFalse(event.getAttendingList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToRemovedList()} on an entrant that is on the declined list
     * @author Jared Strandlund
     */
    @Test
    public void declinedToRemovedTest() {
        setUpDeclinedList(event, entrantID);

        assertTrue(event.addToRemovedList(entrantID));
        assertEquals(1, event.getRemovedList().size());
        assertTrue(event.getRemovedList().contains(entrantID));
        assertEquals(0, event.getDeclinedList().size());
        assertFalse(event.getDeclinedList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToRemovedList()} on an entrant that is on the cancelled list
     * @author Jared Strandlund
     */
    @Test
    public void cancelledToRemovedTest() {
        setUpCancelledList(event, entrantID);

        assertTrue(event.addToRemovedList(entrantID));
        assertEquals(1, event.getRemovedList().size());
        assertTrue(event.getRemovedList().contains(entrantID));
        assertEquals(0, event.getCancelledList().size());
        assertFalse(event.getCancelledList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToRemovedList()} on an entrant that is on the removed list
     * @author Jared Strandlund
     */
    @Test
    public void removedToRemovedTest() {
        setUpRemovedList(event, entrantID);

        assertFalse(event.addToRemovedList(entrantID));
        assertEquals(1, event.getRemovedList().size());
        assertTrue(event.getRemovedList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code removeFromRemovedList}
     * @author Jared Strandlund
     */
    @Test
    public void removeFromRemovedTest() {
        setUpRemovedList(event, entrantID);

        assertTrue(event.removeFromRemovedList(entrantID));
        assertEquals(0, event.getRemovedList().size());

        assertFalse(event.removeFromRemovedList(entrantID));
        assertEquals(0, event.getRemovedList().size());
    }
}
