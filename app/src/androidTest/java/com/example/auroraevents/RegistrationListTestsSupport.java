package com.example.auroraevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.auroraevents.model.RegistrationList;

public class RegistrationListTestsSupport {
    public static void checkSingle(RegistrationList list, String entrantID) {
        assertEquals(1, list.getAllEntrantsList().size());
        assertTrue(list.getAllEntrantsList().contains(entrantID));
    }

    public static void checkNone(RegistrationList list, String entrantID) {
        assertEquals(0, list.getAllEntrantsList().size());
        assertFalse(list.getAllEntrantsList().contains(entrantID));
    }

    public static void setUpWaitingList(RegistrationList list, String entrantID) {
        assertEquals(0, list.addToWaitingList(entrantID));
        assertEquals(1, list.getWaitingList().size());
        assertTrue(list.getWaitingList().contains(entrantID));
        checkSingle(list, entrantID);
    }

    public static void setUpSelectedList(RegistrationList list, String entrantID) {
        setUpWaitingList(list, entrantID);
        assertEquals(0, list.addToSelectedList(entrantID));
        assertEquals(1, list.getSelectedList().size());
        assertTrue(list.getSelectedList().contains(entrantID));
        assertEquals(0, list.getWaitingList().size());
        assertFalse(list.getWaitingList().contains(entrantID));
        checkSingle(list, entrantID);
    }

    public static void setUpAttendingList(RegistrationList list, String entrantID) {
        setUpSelectedList(list, entrantID);
        assertEquals(0, list.addToAttendingList(entrantID));
        assertEquals(1, list.getAttendingList().size());
        assertTrue(list.getAttendingList().contains(entrantID));
        assertEquals(0, list.getSelectedList().size());
        assertFalse(list.getSelectedList().contains(entrantID));
        checkSingle(list, entrantID);
    }

    public static void setUpDeclinedList(RegistrationList list, String entrantID) {
        setUpSelectedList(list, entrantID);
        assertEquals(0, list.addToDeclinedList(entrantID));
        assertEquals(1, list.getDeclinedList().size());
        assertTrue(list.getDeclinedList().contains(entrantID));
        assertEquals(0, list.getSelectedList().size());
        assertFalse(list.getSelectedList().contains(entrantID));
        checkSingle(list, entrantID);
    }

    public static void setUpCancelledList(RegistrationList list, String entrantID) {
        setUpWaitingList(list, entrantID);
        assertEquals(0, list.addToCancelledList(entrantID));
        assertEquals(1, list.getCancelledList().size());
        assertTrue(list.getCancelledList().contains(entrantID));
        assertEquals(0, list.getWaitingList().size());
        assertFalse(list.getWaitingList().contains(entrantID));
        checkSingle(list, entrantID);
    }

    public static void setUpRemovedList(RegistrationList list, String entrantID) {
        assertEquals(0, list.addToRemovedList(entrantID));
        assertEquals(1, list.getRemovedList().size());
        assertTrue(list.getRemovedList().contains(entrantID));
        checkSingle(list, entrantID);
    }
}
