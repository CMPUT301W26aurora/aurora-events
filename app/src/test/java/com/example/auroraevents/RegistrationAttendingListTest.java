package com.example.auroraevents;

import static com.example.auroraevents.RegistrationListTestsSupport.checkNone;
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

import org.junit.Before;
import org.junit.Test;

public class RegistrationAttendingListTest { //TODO: work with new returns
    RegistrationList list;
    String entrantID;

    @Before
    public void before() {
        list = new RegistrationList();
        entrantID = "aurora";
    }

    /**
     * Tests {@code addToAttendingList()} on an entrant that isn't on any entrant lists
     * @author Jared Strandlund
     */
    @Test
    public void noneToAttendingTest() {
        assertFalse(list.addToAttendingList(entrantID));
        assertEquals(0, list.getAttendingList().size());
        assertFalse(list.getAttendingList().contains(entrantID));

        checkNone(list, entrantID);
    }

    /**
     * Tests {@code addToAttendingList()} on an entrant that is on the waiting list
     * @author Jared Strandlund
     */
    @Test
    public void waitingToAttendingTest() {
        setUpWaitingList(list, entrantID);

        assertFalse(list.addToAttendingList(entrantID));
        assertEquals(0, list.getAttendingList().size());
        assertFalse(list.getAttendingList().contains(entrantID));
        assertEquals(1, list.getWaitingList().size());
        assertTrue(list.getWaitingList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToAttendingList()} on an entrant that is on the selected list
     * @author Jared Strandlund
     */
    @Test
    public void selectedToAttendingTest() {
        setUpSelectedList(list, entrantID);

        assertTrue(list.addToAttendingList(entrantID));
        assertEquals(1, list.getAttendingList().size());
        assertTrue(list.getAttendingList().contains(entrantID));
        assertEquals(0, list.getSelectedList().size());
        assertFalse(list.getSelectedList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToAttendingList()} on an entrant that is on the attending list
     * @author Jared Strandlund
     */
    @Test
    public void attendingToAttendingTest() {
        setUpAttendingList(list, entrantID);

        assertTrue(list.addToAttendingList(entrantID));
        assertEquals(1, list.getAttendingList().size());
        assertTrue(list.getAttendingList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToAttendingList()} on an entrant that is on the declined list
     * @author Jared Strandlund
     */
    @Test
    public void declinedToAttendingTest() {
        setUpDeclinedList(list, entrantID);

        assertFalse(list.addToAttendingList(entrantID));
        assertEquals(0, list.getAttendingList().size());
        assertFalse(list.getAttendingList().contains(entrantID));
        assertEquals(1, list.getDeclinedList().size());
        assertTrue(list.getDeclinedList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToAttendingList()} on an entrant that is on the cancelled list
     * @author Jared Strandlund
     */
    @Test
    public void cancelledToAttendingTest() {
        setUpCancelledList(list, entrantID);

        assertFalse(list.addToAttendingList(entrantID));
        assertEquals(0, list.getAttendingList().size());
        assertFalse(list.getAttendingList().contains(entrantID));
        assertEquals(1, list.getCancelledList().size());
        assertTrue(list.getCancelledList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToAttendingList()} on an entrant that is on the removed list
     * @author Jared Strandlund
     */
    @Test
    public void removedToAttendingTest() {
        setUpRemovedList(list, entrantID);

        assertFalse(list.addToAttendingList(entrantID));
        assertEquals(0, list.getAttendingList().size());
        assertFalse(list.getAttendingList().contains(entrantID));
        assertEquals(1, list.getRemovedList().size());
        assertTrue(list.getRemovedList().contains(entrantID));

        checkSingle(list, entrantID);
    }
}
