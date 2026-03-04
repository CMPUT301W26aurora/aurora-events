package com.example.auroraevents;

import static com.example.auroraevents.EventEntrantListsSupport.checkNone;
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

public class EventCancelledListTest {
    Event event;
    String entrantID;

    @Before
    public void before() {
        event = new Event();
        entrantID = "aurora";
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that isn't on any entrant lists
     * @author Jared Strandlund
     */
    @Test
    public void noneToCancelledTest() {
        assertFalse(event.addToCancelledList(entrantID));
        assertEquals(0, event.getCancelledList().size());
        assertFalse(event.getCancelledList().contains(entrantID));

        checkNone(event, entrantID);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that is on the waiting list
     * @author Jared Strandlund
     */
    @Test
    public void waitingToCancelledTest() {
        setUpWaitingList(event, entrantID);

        assertTrue(event.addToCancelledList(entrantID));
        assertEquals(1, event.getCancelledList().size());
        assertTrue(event.getCancelledList().contains(entrantID));
        assertEquals(0, event.getWaitingList().size());
        assertFalse(event.getWaitingList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that is on the selected list
     * @author Jared Strandlund
     */
    @Test
    public void selectedToCancelledTest() {
        setUpSelectedList(event, entrantID);

        assertFalse(event.addToCancelledList(entrantID));
        assertEquals(0, event.getCancelledList().size());
        assertFalse(event.getCancelledList().contains(entrantID));
        assertEquals(1, event.getSelectedList().size());
        assertTrue(event.getSelectedList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that is on the attending list
     * @author Jared Strandlund
     */
    @Test
    public void attendingToCancelledTest() {
        setUpAttendingList(event, entrantID);

        assertFalse(event.addToCancelledList(entrantID));
        assertEquals(0, event.getCancelledList().size());
        assertFalse(event.getCancelledList().contains(entrantID));
        assertEquals(1, event.getAttendingList().size());
        assertTrue(event.getAttendingList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that is on the declined list
     * @author Jared Strandlund
     */
    @Test
    public void declinedToCancelledTest() {
        setUpDeclinedList(event, entrantID);

        assertFalse(event.addToCancelledList(entrantID));
        assertEquals(0, event.getCancelledList().size());
        assertFalse(event.getCancelledList().contains(entrantID));
        assertEquals(1, event.getDeclinedList().size());
        assertTrue(event.getDeclinedList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that is on the cancelled list
     * @author Jared Strandlund
     */
    @Test
    public void cancelledToCancelledTest() {
        setUpCancelledList(event, entrantID);

        assertTrue(event.addToCancelledList(entrantID));
        assertEquals(1, event.getCancelledList().size());
        assertTrue(event.getCancelledList().contains(entrantID));

        checkSingle(event, entrantID);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that is on the removed list
     * @author Jared Strandlund
     */
    @Test
    public void removedToCancelledTest() {
        setUpRemovedList(event, entrantID);

        assertFalse(event.addToCancelledList(entrantID));
        assertEquals(0, event.getCancelledList().size());
        assertFalse(event.getCancelledList().contains(entrantID));
        assertEquals(1, event.getRemovedList().size());
        assertTrue(event.getRemovedList().contains(entrantID));

        checkSingle(event, entrantID);
    }
}
