package com.example.auroraevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EventEntrantListsSupport {
    public static void checkSingle(Event event, String entrantID) {
        assertEquals(1, event.getAllEntrantsList().size());
        assertTrue(event.getAllEntrantsList().contains(entrantID));
    }

    public static void setUpWaitingList(Event event, String entrantID) {
        assertTrue(event.addToWaitingList(entrantID));
        assertEquals(1, event.getWaitingList().size());
        assertTrue(event.getWaitingList().contains(entrantID));
        checkSingle(event, entrantID);
    }

    public static void setUpSelectedList(Event event, String entrantID) {
        setUpWaitingList(event, entrantID);
        assertTrue(event.addToSelectedList(entrantID));
        assertEquals(1, event.getSelectedList().size());
        assertTrue(event.getSelectedList().contains(entrantID));
        assertEquals(0, event.getWaitingList().size());
        assertFalse(event.getWaitingList().contains(entrantID));
        checkSingle(event, entrantID);
    }

    public static void setUpAttendingList(Event event, String entrantID) {
        setUpSelectedList(event, entrantID);
        assertTrue(event.addToAttendingList(entrantID));
        assertEquals(1, event.getAttendingList().size());
        assertTrue(event.getAttendingList().contains(entrantID));
        assertEquals(0, event.getSelectedList().size());
        assertFalse(event.getSelectedList().contains(entrantID));
        checkSingle(event, entrantID);
    }

    public static void setUpDeclinedList(Event event, String entrantID) {
        setUpSelectedList(event, entrantID);
        assertTrue(event.addToDeclinedList(entrantID));
        assertEquals(1, event.getDeclinedList().size());
        assertTrue(event.getDeclinedList().contains(entrantID));
        assertEquals(0, event.getSelectedList().size());
        assertFalse(event.getSelectedList().contains(entrantID));
        checkSingle(event, entrantID);
    }

    public static void setUpCancelledList(Event event, String entrantID) {
        setUpWaitingList(event, entrantID);
        assertTrue(event.addToCancelledList(entrantID));
        assertEquals(1, event.getCancelledList().size());
        assertTrue(event.getCancelledList().contains(entrantID));
        assertEquals(0, event.getWaitingList().size());
        assertFalse(event.getWaitingList().contains(entrantID));
        checkSingle(event, entrantID);
    }

    public static void setUpRemovedList(Event event, String entrantID) {
        assertTrue(event.addToRemovedList(entrantID));
        assertEquals(1, event.getRemovedList().size());
        assertTrue(event.getRemovedList().contains(entrantID));
        checkSingle(event, entrantID);
    }
}
