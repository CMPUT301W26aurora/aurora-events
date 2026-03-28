package com.example.auroraevents.registration_tests;

import static com.example.auroraevents.registration_tests.RegistrationListTestsSupport.checkNone;
import static com.example.auroraevents.registration_tests.RegistrationListTestsSupport.checkSingle;
import static com.example.auroraevents.registration_tests.RegistrationListTestsSupport.setUpAllLists;
import static com.example.auroraevents.TestsSupport.setUpEvent;
import static com.example.auroraevents.registration_tests.RegistrationListTestsSupport.setUpAttendingList;
import static com.example.auroraevents.registration_tests.RegistrationListTestsSupport.setUpCancelledList;
import static com.example.auroraevents.registration_tests.RegistrationListTestsSupport.setUpDeclinedList;
import static com.example.auroraevents.registration_tests.RegistrationListTestsSupport.setUpRemovedList;
import static com.example.auroraevents.registration_tests.RegistrationListTestsSupport.setUpSelectedList;
import static com.example.auroraevents.registration_tests.RegistrationListTestsSupport.setUpWaitingList;
import static com.example.auroraevents.TestsSupport.signIn;
import static com.example.auroraevents.TestsSupport.takeDownEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.RegistrationList;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegistrationCancelledListTest {
    Event event;
    RegistrationList list;
    String entrantID;

    @BeforeClass
    public static void prepare() {
        signIn();
    }

    @Before
    public void before() {
        event = new Event(
                "test device",
                "registration test",
                "event for registration cancelled list test",
                "free",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                "testing environment",
                false,
                -1,
                -1,
                null);
        setUpEvent(event);
        list = event.registrationList;
        entrantID = "aurora";
    }

    @After
    public void after() {
        takeDownEvent(event);
    }

    /**
     * Tests {@code addToCancelledList()} on an entrant that isn't on any entrant lists
     * @author Jared Strandlund
     */
    @Test
    public void noneToCancelledTest() {
        assertEquals(1, list.addToCancelledList(entrantID));
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
        checkSingle(list, entrantID);

        assertEquals(0, list.addToCancelledList(entrantID));
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
        checkSingle(list, entrantID);

        assertEquals(1, list.addToCancelledList(entrantID));
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
        checkSingle(list, entrantID);

        assertEquals(1, list.addToCancelledList(entrantID));
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
        checkSingle(list, entrantID);

        assertEquals(1, list.addToCancelledList(entrantID));
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
        checkSingle(list, entrantID);

        assertEquals(-1, list.addToCancelledList(entrantID));
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
        checkSingle(list, entrantID);

        assertEquals(1, list.addToCancelledList(entrantID));
        assertEquals(0, list.getCancelledList().size());
        assertFalse(list.getCancelledList().contains(entrantID));
        assertEquals(1, list.getRemovedList().size());
        assertTrue(list.getRemovedList().contains(entrantID));

        checkSingle(list, entrantID);
    }

    /**
     * Tests {@code addAllToCancelledList()} on entrants on each of the lists
     */
    @Test
    public void allToCancelledList() {
        // Set up
        String noneEntrant = "none user";
        String waitingEntrant = "waiting user";
        String selectedEntrant = "selected user";
        String attendingEntrant = "attending user";
        String declinedEntrant = "declined user";
        String cancelledEntrant = "cancelled user";
        String removedEntrant = "removed user";
        setUpAllLists(event.registrationList, waitingEntrant, selectedEntrant, attendingEntrant, declinedEntrant, cancelledEntrant, removedEntrant);

        List<String> entrants = new ArrayList<>();
        entrants.add(noneEntrant);
        entrants.add(waitingEntrant);
        entrants.add(selectedEntrant);
        entrants.add(attendingEntrant);
        entrants.add(declinedEntrant);
        entrants.add(cancelledEntrant);
        entrants.add(removedEntrant);

        // Test
        List<Integer> statuses;
        statuses = event.registrationList.addAllToCancelledList(entrants);
        // Check statuses
        assertEquals(1, (long) statuses.get(0));  // none
        assertEquals(0, (long) statuses.get(1));  // waiting
        assertEquals(1, (long) statuses.get(2));  // selected
        assertEquals(1, (long) statuses.get(3));  // attending
        assertEquals(1, (long) statuses.get(4));  // declined
        assertEquals(-1, (long) statuses.get(5)); // cancelled
        assertEquals(1, (long) statuses.get(6));  // removed
        // Check list sizes
        assertEquals(0, event.registrationList.getWaitingList().size());
        assertEquals(1, event.registrationList.getSelectedList().size());
        assertEquals(1, event.registrationList.getAttendingList().size());
        assertEquals(1, event.registrationList.getDeclinedList().size());
        assertEquals(2, event.registrationList.getCancelledList().size());
        assertEquals(1, event.registrationList.getRemovedList().size());
        // Check list content
        List<String> expectedList = new ArrayList<>(Arrays.asList(waitingEntrant, cancelledEntrant));
        List<String> actualList = event.registrationList.getCancelledList();
        assertTrue(actualList.containsAll(expectedList));
        assertTrue(expectedList.containsAll(actualList));
    }
}
