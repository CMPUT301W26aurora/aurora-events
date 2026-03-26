package com.example.auroraevents.registration_tests;

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
        int initAllSize = list.getAllEntrantsList().size();
        boolean onList = list.getAllEntrantsList().contains(entrantID);
        int initWaitingSize = list.getWaitingList().size();
        assertEquals(0, list.addToWaitingList(entrantID));
        assertEquals(initWaitingSize+1, list.getWaitingList().size());
        assertTrue(list.getWaitingList().contains(entrantID));
        if (onList)
            assertEquals(initAllSize, list.getAllEntrantsList().size());
        else
            assertEquals(initAllSize+1, list.getAllEntrantsList().size());
    }

    public static void setUpSelectedList(RegistrationList list, String entrantID) {
        setUpWaitingList(list, entrantID);
        int initAllSize = list.getAllEntrantsList().size();
        int initWaitingSize = list.getWaitingList().size();
        int initSelectedSize = list.getSelectedList().size();
        assertEquals(0, list.addToSelectedList(entrantID));
        assertEquals(initSelectedSize+1, list.getSelectedList().size());
        assertTrue(list.getSelectedList().contains(entrantID));
        assertEquals(initWaitingSize-1, list.getWaitingList().size());
        assertFalse(list.getWaitingList().contains(entrantID));
        assertEquals(initAllSize, list.getAllEntrantsList().size());
    }

    public static void setUpAttendingList(RegistrationList list, String entrantID) {
        setUpSelectedList(list, entrantID);
        int initAllSize = list.getAllEntrantsList().size();
        int initSelectedSize = list.getSelectedList().size();
        int initAttendingSize = list.getAttendingList().size();
        assertEquals(0, list.addToAttendingList(entrantID));
        assertEquals(initAttendingSize+1, list.getAttendingList().size());
        assertTrue(list.getAttendingList().contains(entrantID));
        assertEquals(initSelectedSize-1, list.getSelectedList().size());
        assertFalse(list.getSelectedList().contains(entrantID));
        assertEquals(initAllSize, list.getAllEntrantsList().size());
    }

    public static void setUpDeclinedList(RegistrationList list, String entrantID) {
        setUpSelectedList(list, entrantID);
        int initAllSize = list.getAllEntrantsList().size();
        int initSelectedSize = list.getSelectedList().size();
        int initDeclinedSize = list.getDeclinedList().size();
        assertEquals(0, list.addToDeclinedList(entrantID));
        assertEquals(initDeclinedSize+1, list.getDeclinedList().size());
        assertTrue(list.getDeclinedList().contains(entrantID));
        assertEquals(initSelectedSize-1, list.getSelectedList().size());
        assertFalse(list.getSelectedList().contains(entrantID));
        assertEquals(initAllSize, list.getAllEntrantsList().size());
    }

    public static void setUpCancelledList(RegistrationList list, String entrantID) {
        setUpWaitingList(list, entrantID);
        int initAllSize = list.getAllEntrantsList().size();
        int initWaitingSize = list.getWaitingList().size();
        int initCancelledSize = list.getCancelledList().size();
        assertEquals(0, list.addToCancelledList(entrantID));
        assertEquals(initCancelledSize+1, list.getCancelledList().size());
        assertTrue(list.getCancelledList().contains(entrantID));
        assertEquals(initWaitingSize-1, list.getWaitingList().size());
        assertFalse(list.getWaitingList().contains(entrantID));
        assertEquals(initAllSize, list.getAllEntrantsList().size());
    }

    public static void setUpRemovedList(RegistrationList list, String entrantID) {
        int initAllSize = list.getAllEntrantsList().size();
        boolean onList = list.getAllEntrantsList().contains(entrantID);
        int initRemovedSize = list.getRemovedList().size();
        assertEquals(0, list.addToRemovedList(entrantID));
        assertEquals(initRemovedSize+1, list.getRemovedList().size());
        assertTrue(list.getRemovedList().contains(entrantID));
        if (onList)
            assertEquals(initAllSize, list.getAllEntrantsList().size());
        else
            assertEquals(initAllSize+1, list.getAllEntrantsList().size());
    }

    public static void setUpAllLists(
            RegistrationList registrationList,
            String waitingEntrant,
            String selectedEntrant,
            String attendingEntrant,
            String declinedEntrant,
            String cancelledEntrant,
            String removedEntrant
    ) {
        setUpAttendingList(registrationList, attendingEntrant);
        setUpDeclinedList(registrationList, declinedEntrant);
        setUpSelectedList(registrationList, selectedEntrant);
        setUpCancelledList(registrationList, cancelledEntrant);
        setUpWaitingList(registrationList, waitingEntrant);
        setUpRemovedList(registrationList, removedEntrant);
    }
}
