package com.example.auroraevents;

import static com.example.auroraevents.TestsSupport.setUpEvent;
import static com.example.auroraevents.TestsSupport.signIn;
import static com.example.auroraevents.TestsSupport.takeDownEvent;
import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;

import com.example.auroraevents.model.Event;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Date;

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
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                "testing environment",
                0);
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

//    /**
//     * Tests the setters for the entrant lists, and then {@code tidyLists()}
//     * @author Jared Strandlund
//     */
//    @Test
//    public void setTest() {  //TO/DO: work with new returns
//        // Set up
//        String cancelledEntrant = "Pope Leo";
//        String waitingEntrant = "Chappell Roan";
//        String selectedEntrant = "Count Dracula";
//        String attendingEntrant = "Juno";
//        String declinedEntrant = "Athena";
//        String removedEntrant = "aurora";
//
//        ArrayList<String> finalList = new ArrayList<>();
//        finalList.add(cancelledEntrant);
//        finalList.add(waitingEntrant);
//        finalList.add(selectedEntrant);
//        finalList.add(attendingEntrant);
//        finalList.add(declinedEntrant);
//        finalList.add(removedEntrant);
//        ArrayList<String> cancelledList = new ArrayList<>();
//        cancelledList.add(cancelledEntrant);
//        cancelledList.add(waitingEntrant);
//        cancelledList.add(selectedEntrant);
//        cancelledList.add(attendingEntrant);
//        cancelledList.add(declinedEntrant);
//        cancelledList.add(removedEntrant);
//        ArrayList<String> waitingList = new ArrayList<>();
//        waitingList.add(waitingEntrant);
//        waitingList.add(selectedEntrant);
//        waitingList.add(attendingEntrant);
//        waitingList.add(declinedEntrant);
//        waitingList.add(removedEntrant);
//        ArrayList<String> selectedList = new ArrayList<>();
//        selectedList.add(selectedEntrant);
//        selectedList.add(attendingEntrant);
//        selectedList.add(declinedEntrant);
//        selectedList.add(removedEntrant);
//        ArrayList<String> attendingList = new ArrayList<>();
//        attendingList.add(attendingEntrant);
//        attendingList.add(declinedEntrant);
//        attendingList.add(removedEntrant);
//        ArrayList<String> declinedList = new ArrayList<>();
//        declinedList.add(declinedEntrant);
//        declinedList.add(removedEntrant);
//        ArrayList<String> removedList = new ArrayList<>();
//        removedList.add(removedEntrant);
//
//        event.entrantList.addAllToCancelledList(cancelledList);
//        event.entrantList.addAllToWaitingList(waitingList);
//        event.entrantList.addAllToSelectedList(selectedList);
//        event.entrantList.addAllToAttendingList(attendingList);
//        event.entrantList.addAllToDeclinedList(declinedList);
//        event.entrantList.addAllToRemovedList(removedList);
//
//        // Testing
//        event.entrantList.tidyLists();
//        assertEquals(1, event.entrantList.getCancelledList().size());
//        assertTrue(event.entrantList.getCancelledList().contains(cancelledEntrant));
//        assertEquals(1, event.entrantList.getWaitingList().size());
//        assertTrue(event.entrantList.getWaitingList().contains(waitingEntrant));
//        assertEquals(1, event.entrantList.getSelectedList().size());
//        assertTrue(event.entrantList.getSelectedList().contains(selectedEntrant));
//        assertEquals(1, event.entrantList.getAttendingList().size());
//        assertTrue(event.entrantList.getAttendingList().contains(attendingEntrant));
//        assertEquals(1, event.entrantList.getDeclinedList().size());
//        assertTrue(event.entrantList.getDeclinedList().contains(declinedEntrant));
//        assertEquals(1, event.entrantList.getRemovedList().size());
//        assertTrue(event.entrantList.getRemovedList().contains(removedEntrant));
//        assertEquals(6, event.entrantList.getAllEntrantsList().size());
//        assertTrue(event.entrantList.getAllEntrantsList().containsAll(finalList));
//    }
}
