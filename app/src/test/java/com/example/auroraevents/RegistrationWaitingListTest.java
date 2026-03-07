package com.example.auroraevents;

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

/**
 * Event.waitingList local unit test, which will execute on the development machine (host).
 * @author Jared Strandlund
 * @see Event
 */
public class RegistrationWaitingListTest { //TODO: work with new returns
    RegistrationList list;
    String entrantID;

    @Before
    public void before() {
        list = new RegistrationList();
        entrantID = "aurora";
    }

    /**
     * Tests {@code addToWaitingList()} on an entrant that isn't on any entrant lists
     * @author Jared Strandlund
     */
    @Test
    public void noneToWaitingTest() {
        assertTrue(list.addToWaitingList(entrantID));
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

        assertTrue(list.addToWaitingList(entrantID));
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

        assertFalse(list.addToWaitingList(entrantID));
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

        assertFalse(list.addToWaitingList(entrantID));
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

        assertFalse(list.addToWaitingList(entrantID));
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

        assertTrue(list.addToWaitingList(entrantID));
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

        assertFalse(list.addToWaitingList(entrantID));
        assertEquals(0, list.getWaitingList().size());
        assertFalse(list.getWaitingList().contains(entrantID));
        assertEquals(1, list.getRemovedList().size());
        assertTrue(list.getRemovedList().contains(entrantID));

        checkSingle(list, entrantID);
    }
}