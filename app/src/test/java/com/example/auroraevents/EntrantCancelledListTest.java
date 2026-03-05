package com.example.auroraevents;

import static com.example.auroraevents.EntrantListTestsSupport.checkNone;
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

public class EntrantCancelledListTest {
    EntrantList list;
    String entrantID;

    @Before
    public void before() {
        list = new EntrantList();
        entrantID = "aurora";
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that isn't on any entrant lists
     * @author Jared Strandlund
     */
    @Test
    public void noneToCancelledTest() {
        assertFalse(list.addToCancelledList(entrantID));
        assertEquals(0, list.getCancelledList().size());
        assertFalse(list.getCancelledList().contains(entrantID));

        checkNone(list, entrantID);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that is on the waiting list
     * @author Jared Strandlund
     */
    @Test
    public void waitingToCancelledTest() {
        setUpWaitingList(list, entrantID);

        assertTrue(list.addToCancelledList(entrantID));
        assertEquals(1, list.getCancelledList().size());
        assertTrue(list.getCancelledList().contains(entrantID));
        assertEquals(0, list.getWaitingList().size());
        assertFalse(list.getWaitingList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that is on the selected list
     * @author Jared Strandlund
     */
    @Test
    public void selectedToCancelledTest() {
        setUpSelectedList(list, entrantID);

        assertFalse(list.addToCancelledList(entrantID));
        assertEquals(0, list.getCancelledList().size());
        assertFalse(list.getCancelledList().contains(entrantID));
        assertEquals(1, list.getSelectedList().size());
        assertTrue(list.getSelectedList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that is on the attending list
     * @author Jared Strandlund
     */
    @Test
    public void attendingToCancelledTest() {
        setUpAttendingList(list, entrantID);

        assertFalse(list.addToCancelledList(entrantID));
        assertEquals(0, list.getCancelledList().size());
        assertFalse(list.getCancelledList().contains(entrantID));
        assertEquals(1, list.getAttendingList().size());
        assertTrue(list.getAttendingList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that is on the declined list
     * @author Jared Strandlund
     */
    @Test
    public void declinedToCancelledTest() {
        setUpDeclinedList(list, entrantID);

        assertFalse(list.addToCancelledList(entrantID));
        assertEquals(0, list.getCancelledList().size());
        assertFalse(list.getCancelledList().contains(entrantID));
        assertEquals(1, list.getDeclinedList().size());
        assertTrue(list.getDeclinedList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that is on the cancelled list
     * @author Jared Strandlund
     */
    @Test
    public void cancelledToCancelledTest() {
        setUpCancelledList(list, entrantID);

        assertTrue(list.addToCancelledList(entrantID));
        assertEquals(1, list.getCancelledList().size());
        assertTrue(list.getCancelledList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that is on the removed list
     * @author Jared Strandlund
     */
    @Test
    public void removedToCancelledTest() {
        setUpRemovedList(list, entrantID);

        assertFalse(list.addToCancelledList(entrantID));
        assertEquals(0, list.getCancelledList().size());
        assertFalse(list.getCancelledList().contains(entrantID));
        assertEquals(1, list.getRemovedList().size());
        assertTrue(list.getRemovedList().contains(entrantID));

        checkSingle(list, entrantID);
    }
}
