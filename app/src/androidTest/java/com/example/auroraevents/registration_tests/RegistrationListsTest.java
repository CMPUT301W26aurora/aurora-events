package com.example.auroraevents.registration_tests;

import static com.example.auroraevents.registration_tests.RegistrationListTestsSupport.setUpAllLists;
import static com.example.auroraevents.TestsSupport.setUpEvent;
import static com.example.auroraevents.TestsSupport.signIn;
import static com.example.auroraevents.TestsSupport.takeDownEvent;
import static org.junit.Assert.assertEquals;

import com.example.auroraevents.model.Event;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

public class RegistrationListsTest {
    Event event;

    @BeforeClass
    public static void prepare() {
        signIn();
    }

    @Before
    public void before() {
        event = new Event(
                "test device",
                "registration test",
                "event for general registration list test",
                "free",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                "testing environment",
                false,
                0,
                null);
        setUpEvent(event);
    }

    @After
    public void after() {
        takeDownEvent(event);
    }

    /**
     * Tests that the constructor makes entrant lists properly
     */
    @Test
    public void constructorTest() {
        assertEquals(0, event.registrationList.getWaitingList().size());
        assertEquals(0, event.registrationList.getSelectedList().size());
        assertEquals(0, event.registrationList.getAttendingList().size());
        assertEquals(0, event.registrationList.getDeclinedList().size());
        assertEquals(0, event.registrationList.getCancelledList().size());
        assertEquals(0, event.registrationList.getRemovedList().size());
        assertEquals(0, event.registrationList.getAllEntrantsList().size());
    }

    /**
     * Tests that the {@code removeFromAllLists()} method works on an entrant in the waiting list
     * @author Jared Strandlund
     */
    @Test
    public void deleteAllWaitingListTest() {
        // Set up
        String waitingEntrant = "waiting user";
        String selectedEntrant = "selected user";
        String attendingEntrant = "attending user";
        String declinedEntrant = "declined user";
        String cancelledEntrant = "cancelled user";
        String removedEntrant = "removed user";
        setUpAllLists(event.registrationList, waitingEntrant, selectedEntrant, attendingEntrant, declinedEntrant, cancelledEntrant, removedEntrant);

        // Test
        List<Integer> statuses;
        statuses = event.registrationList.removeFromAllLists(waitingEntrant);
        // Assert proper statuses
        assertEquals(0, (long) statuses.get(0));  // max
        assertEquals(0, (long) statuses.get(1));  // waiting list
        assertEquals(-1, (long) statuses.get(2)); // selected list
        assertEquals(-1, (long) statuses.get(3)); // attending list
        assertEquals(-1, (long) statuses.get(4)); // declined list
        assertEquals(-1, (long) statuses.get(5)); // cancelled list
        assertEquals(-1, (long) statuses.get(6)); // removed list
        // Assert proper lists
        assertEquals(0, event.registrationList.getWaitingList().size());
        assertEquals(1, event.registrationList.getSelectedList().size());
        assertEquals(1, event.registrationList.getAttendingList().size());
        assertEquals(1, event.registrationList.getDeclinedList().size());
        assertEquals(1, event.registrationList.getCancelledList().size());
        assertEquals(1, event.registrationList.getRemovedList().size());
    }

    /**
     * Tests that the {@code removeFromAllLists()} method works on an entrant in the selected list
     * @author Jared Strandlund
     */
    @Test
    public void deleteAllSelectedListTest() {
        // Set up
        String waitingEntrant = "waiting user";
        String selectedEntrant = "selected user";
        String attendingEntrant = "attending user";
        String declinedEntrant = "declined user";
        String cancelledEntrant = "cancelled user";
        String removedEntrant = "removed user";
        setUpAllLists(event.registrationList, waitingEntrant, selectedEntrant, attendingEntrant, declinedEntrant, cancelledEntrant, removedEntrant);

        // Test
        List<Integer> statuses;
        statuses = event.registrationList.removeFromAllLists(selectedEntrant);
        // Assert proper statuses
        assertEquals(0, (long) statuses.get(0));  // max
        assertEquals(-1, (long) statuses.get(1)); // waiting list
        assertEquals(0, (long) statuses.get(2));  // selected list
        assertEquals(-1, (long) statuses.get(3)); // attending list
        assertEquals(-1, (long) statuses.get(4)); // declined list
        assertEquals(-1, (long) statuses.get(5)); // cancelled list
        assertEquals(-1, (long) statuses.get(6)); // removed list
        // Assert proper lists
        assertEquals(1, event.registrationList.getWaitingList().size());
        assertEquals(0, event.registrationList.getSelectedList().size());
        assertEquals(1, event.registrationList.getAttendingList().size());
        assertEquals(1, event.registrationList.getDeclinedList().size());
        assertEquals(1, event.registrationList.getCancelledList().size());
        assertEquals(1, event.registrationList.getRemovedList().size());
    }

    /**
     * Tests that the {@code removeFromAllLists()} method works on an entrant in the attending list
     * @author Jared Strandlund
     */
    @Test
    public void deleteAllAttendingListTest() {
        // Set up
        String waitingEntrant = "waiting user";
        String selectedEntrant = "selected user";
        String attendingEntrant = "attending user";
        String declinedEntrant = "declined user";
        String cancelledEntrant = "cancelled user";
        String removedEntrant = "removed user";
        setUpAllLists(event.registrationList, waitingEntrant, selectedEntrant, attendingEntrant, declinedEntrant, cancelledEntrant, removedEntrant);

        // Test
        List<Integer> statuses;
        statuses = event.registrationList.removeFromAllLists(attendingEntrant);
        // Assert proper statuses
        assertEquals(0, (long) statuses.get(0));  // max
        assertEquals(-1, (long) statuses.get(1)); // waiting list
        assertEquals(-1, (long) statuses.get(2)); // selected list
        assertEquals(0, (long) statuses.get(3));  // attending list
        assertEquals(-1, (long) statuses.get(4)); // declined list
        assertEquals(-1, (long) statuses.get(5)); // cancelled list
        assertEquals(-1, (long) statuses.get(6)); // removed list
        // Assert proper lists
        assertEquals(1, event.registrationList.getWaitingList().size());
        assertEquals(1, event.registrationList.getSelectedList().size());
        assertEquals(0, event.registrationList.getAttendingList().size());
        assertEquals(1, event.registrationList.getDeclinedList().size());
        assertEquals(1, event.registrationList.getCancelledList().size());
        assertEquals(1, event.registrationList.getRemovedList().size());
    }

    /**
     * Tests that the {@code removeFromAllLists()} method works on an entrant in the declined list
     * @author Jared Strandlund
     */
    @Test
    public void deleteAllDeclinedListTest() {
        // Set up
        String waitingEntrant = "waiting user";
        String selectedEntrant = "selected user";
        String attendingEntrant = "attending user";
        String declinedEntrant = "declined user";
        String cancelledEntrant = "cancelled user";
        String removedEntrant = "removed user";
        setUpAllLists(event.registrationList, waitingEntrant, selectedEntrant, attendingEntrant, declinedEntrant, cancelledEntrant, removedEntrant);

        // Test
        List<Integer> statuses;
        statuses = event.registrationList.removeFromAllLists(declinedEntrant);
        // Assert proper statuses
        assertEquals(0, (long) statuses.get(0));  // max
        assertEquals(-1, (long) statuses.get(1)); // waiting list
        assertEquals(-1, (long) statuses.get(2)); // selected list
        assertEquals(-1, (long) statuses.get(3)); // attending list
        assertEquals(0, (long) statuses.get(4));  // declined list
        assertEquals(-1, (long) statuses.get(5)); // cancelled list
        assertEquals(-1, (long) statuses.get(6)); // removed list
        // Assert proper lists
        assertEquals(1, event.registrationList.getWaitingList().size());
        assertEquals(1, event.registrationList.getSelectedList().size());
        assertEquals(1, event.registrationList.getAttendingList().size());
        assertEquals(0, event.registrationList.getDeclinedList().size());
        assertEquals(1, event.registrationList.getCancelledList().size());
        assertEquals(1, event.registrationList.getRemovedList().size());
    }

    /**
     * Tests that the {@code removeFromAllLists()} method works on an entrant in the cancelled list
     * @author Jared Strandlund
     */
    @Test
    public void deleteAllCancelledListTest() {
        // Set up
        String waitingEntrant = "waiting user";
        String selectedEntrant = "selected user";
        String attendingEntrant = "attending user";
        String declinedEntrant = "declined user";
        String cancelledEntrant = "cancelled user";
        String removedEntrant = "removed user";
        setUpAllLists(event.registrationList, waitingEntrant, selectedEntrant, attendingEntrant, declinedEntrant, cancelledEntrant, removedEntrant);

        // Test
        List<Integer> statuses;
        statuses = event.registrationList.removeFromAllLists(cancelledEntrant);
        // Assert proper statuses
        assertEquals(0, (long) statuses.get(0));  // max
        assertEquals(-1, (long) statuses.get(1)); // waiting list
        assertEquals(-1, (long) statuses.get(2)); // selected list
        assertEquals(-1, (long) statuses.get(3)); // attending list
        assertEquals(-1, (long) statuses.get(4)); // declined list
        assertEquals(0, (long) statuses.get(5));  // cancelled list
        assertEquals(-1, (long) statuses.get(6)); // removed list
        // Assert proper lists
        assertEquals(1, event.registrationList.getWaitingList().size());
        assertEquals(1, event.registrationList.getSelectedList().size());
        assertEquals(1, event.registrationList.getAttendingList().size());
        assertEquals(1, event.registrationList.getDeclinedList().size());
        assertEquals(0, event.registrationList.getCancelledList().size());
        assertEquals(1, event.registrationList.getRemovedList().size());
    }

    /**
     * Tests that the {@code removeFromAllLists()} method works on an entrant in the removed list
     * @author Jared Strandlund
     */
    @Test
    public void deleteAllRemovedListTest() {
        // Set up
        String waitingEntrant = "waiting user";
        String selectedEntrant = "selected user";
        String attendingEntrant = "attending user";
        String declinedEntrant = "declined user";
        String cancelledEntrant = "cancelled user";
        String removedEntrant = "removed user";
        setUpAllLists(event.registrationList, waitingEntrant, selectedEntrant, attendingEntrant, declinedEntrant, cancelledEntrant, removedEntrant);

        // Test
        List<Integer> statuses;
        statuses = event.registrationList.removeFromAllLists(removedEntrant);
        // Assert proper statuses
        assertEquals(0, (long) statuses.get(0));  // max
        assertEquals(-1, (long) statuses.get(1)); // waiting list
        assertEquals(-1, (long) statuses.get(2)); // selected list
        assertEquals(-1, (long) statuses.get(3)); // attending list
        assertEquals(-1, (long) statuses.get(4)); // declined list
        assertEquals(-1, (long) statuses.get(5)); // cancelled list
        assertEquals(0, (long) statuses.get(6));  // removed list
        // Assert proper lists
        assertEquals(1, event.registrationList.getWaitingList().size());
        assertEquals(1, event.registrationList.getSelectedList().size());
        assertEquals(1, event.registrationList.getAttendingList().size());
        assertEquals(1, event.registrationList.getDeclinedList().size());
        assertEquals(1, event.registrationList.getCancelledList().size());
        assertEquals(0, event.registrationList.getRemovedList().size());
    }
}
