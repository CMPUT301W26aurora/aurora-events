package com.example.auroraevents;

import static com.example.auroraevents.EntrantListTestsSupport.checkSingle;
import static com.example.auroraevents.EntrantListTestsSupport.setUpAttendingList;
import static com.example.auroraevents.EntrantListTestsSupport.setUpCancelledList;
import static com.example.auroraevents.EntrantListTestsSupport.setUpDeclinedList;
import static com.example.auroraevents.EntrantListTestsSupport.setUpRemovedList;
import static com.example.auroraevents.EntrantListTestsSupport.setUpSelectedList;
import static com.example.auroraevents.EntrantListTestsSupport.setUpWaitingList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class EntrantRemovedListTest {
    EntrantList list;
    String entrantID;

    @Before
    public void before() {
        list = new EntrantList();
        entrantID = "aurora";
    }

    /**
     * Tests {@code addToRemovedList()} on an entrant that isn't on any entrant lists
     * @author Jared Strandlund
     */
    @Test
    public void noneToRemovedTest() {
        assertTrue(list.addToRemovedList(entrantID));
        assertEquals(1, list.getRemovedList().size());
        assertTrue(list.getRemovedList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToRemovedList()} on an entrant that is on the waiting list
     * @author Jared Strandlund
     */
    @Test
    public void waitingToRemovedTest() {
        setUpWaitingList(list, entrantID);

        assertTrue(list.addToRemovedList(entrantID));
        assertEquals(1, list.getRemovedList().size());
        assertTrue(list.getRemovedList().contains(entrantID));
        assertEquals(0, list.getWaitingList().size());
        assertFalse(list.getWaitingList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToRemovedList()} on an entrant that is on the selected list
     * @author Jared Strandlund
     */
    @Test
    public void selectedToRemovedTest() {
        setUpSelectedList(list, entrantID);

        assertTrue(list.addToRemovedList(entrantID));
        assertEquals(1, list.getRemovedList().size());
        assertTrue(list.getRemovedList().contains(entrantID));
        assertEquals(0, list.getSelectedList().size());
        assertFalse(list.getSelectedList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToRemovedList()} on an entrant that is on the attending list
     * @author Jared Strandlund
     */
    @Test
    public void attendingToRemovedTest() {
        setUpAttendingList(list, entrantID);

        assertTrue(list.addToRemovedList(entrantID));
        assertEquals(1, list.getRemovedList().size());
        assertTrue(list.getRemovedList().contains(entrantID));
        assertEquals(0, list.getAttendingList().size());
        assertFalse(list.getAttendingList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToRemovedList()} on an entrant that is on the declined list
     * @author Jared Strandlund
     */
    @Test
    public void declinedToRemovedTest() {
        setUpDeclinedList(list, entrantID);

        assertTrue(list.addToRemovedList(entrantID));
        assertEquals(1, list.getRemovedList().size());
        assertTrue(list.getRemovedList().contains(entrantID));
        assertEquals(0, list.getDeclinedList().size());
        assertFalse(list.getDeclinedList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToRemovedList()} on an entrant that is on the cancelled list
     * @author Jared Strandlund
     */
    @Test
    public void cancelledToRemovedTest() {
        setUpCancelledList(list, entrantID);

        assertTrue(list.addToRemovedList(entrantID));
        assertEquals(1, list.getRemovedList().size());
        assertTrue(list.getRemovedList().contains(entrantID));
        assertEquals(0, list.getCancelledList().size());
        assertFalse(list.getCancelledList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToRemovedList()} on an entrant that is on the removed list
     * @author Jared Strandlund
     */
    @Test
    public void removedToRemovedTest() {
        setUpRemovedList(list, entrantID);

        assertTrue(list.addToRemovedList(entrantID));
        assertEquals(1, list.getRemovedList().size());
        assertTrue(list.getRemovedList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code removeFromRemovedList}
     * @author Jared Strandlund
     */
    @Test
    public void removeFromRemovedTest() {
        setUpRemovedList(list, entrantID);

        assertTrue(list.removeFromRemovedList(entrantID));
        assertEquals(0, list.getRemovedList().size());

        assertFalse(list.removeFromRemovedList(entrantID));
        assertEquals(0, list.getRemovedList().size());
    }
}
