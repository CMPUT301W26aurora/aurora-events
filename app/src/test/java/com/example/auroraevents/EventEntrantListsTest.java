package com.example.auroraevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class EventEntrantListsTest {
    Event event;

    @Before
    public void before() {
        event = new Event();
    }

    /**
     * Tests that the constructor makes entrant lists properly
     */
    @Test
    public void constructorTest() {
        assertEquals(0, event.getWaitingList().size());
        assertEquals(0, event.getSelectedList().size());
        assertEquals(0, event.getAttendingList().size());
        assertEquals(0, event.getDeclinedList().size());
        assertEquals(0, event.getCancelledList().size());
        assertEquals(0, event.getRemovedList().size());
        assertEquals(0, event.getAllEntrantsList().size());
    }

    //TODO: test sets and tidyLists()
    /**
     * Tests the setters for the entrant lists, and then {@code tidyLists()}
     * @author Jared Strandlund
     */
    @Test
    public void setTest() {
        // Set up
        String cancelledEntrant = "Pope Leo";
        String waitingEntrant = "Chappell Roan";
        String selectedEntrant = "Count Dracula";
        String attendingEntrant = "Juno";
        String declinedEntrant = "Athena";
        String removedEntrant = "aurora";

        ArrayList<String> finalList = new ArrayList<>();
        finalList.add(cancelledEntrant);
        finalList.add(waitingEntrant);
        finalList.add(selectedEntrant);
        finalList.add(attendingEntrant);
        finalList.add(declinedEntrant);
        finalList.add(removedEntrant);
        ArrayList<String> cancelledList = new ArrayList<>();
        cancelledList.add(cancelledEntrant);
        cancelledList.add(waitingEntrant);
        cancelledList.add(selectedEntrant);
        cancelledList.add(attendingEntrant);
        cancelledList.add(declinedEntrant);
        cancelledList.add(removedEntrant);
        ArrayList<String> waitingList = new ArrayList<>();
        waitingList.add(waitingEntrant);
        waitingList.add(selectedEntrant);
        waitingList.add(attendingEntrant);
        waitingList.add(declinedEntrant);
        waitingList.add(removedEntrant);
        ArrayList<String> selectedList = new ArrayList<>();
        selectedList.add(selectedEntrant);
        selectedList.add(attendingEntrant);
        selectedList.add(declinedEntrant);
        selectedList.add(removedEntrant);
        ArrayList<String> attendingList = new ArrayList<>();
        attendingList.add(attendingEntrant);
        attendingList.add(declinedEntrant);
        attendingList.add(removedEntrant);
        ArrayList<String> declinedList = new ArrayList<>();
        declinedList.add(declinedEntrant);
        declinedList.add(removedEntrant);
        ArrayList<String> removedList = new ArrayList<>();
        removedList.add(removedEntrant);

        //Testing
        event.setCancelledList(cancelledList);
        assertEquals(cancelledList, event.getCancelledList());
        event.setWaitingList(waitingList);
        assertEquals(waitingList, event.getWaitingList());
        event.setSelectedList(selectedList);
        assertEquals(selectedList, event.getSelectedList());
        event.setAttendingList(attendingList);
        assertEquals(attendingList, event.getAttendingList());
        event.setDeclinedList(declinedList);
        assertEquals(declinedList, event.getDeclinedList());
        event.setRemovedList(removedList);
        assertEquals(removedList, event.getRemovedList());

        event.tidyLists();
        assertEquals(1, event.getCancelledList().size());
        assertTrue(event.getCancelledList().contains(cancelledEntrant));
        assertEquals(1, event.getWaitingList().size());
        assertTrue(event.getWaitingList().contains(waitingEntrant));
        assertEquals(1, event.getSelectedList().size());
        assertTrue(event.getSelectedList().contains(selectedEntrant));
        assertEquals(1, event.getAttendingList().size());
        assertTrue(event.getAttendingList().contains(attendingEntrant));
        assertEquals(1, event.getDeclinedList().size());
        assertTrue(event.getDeclinedList().contains(declinedEntrant));
        assertEquals(1, event.getRemovedList().size());
        assertTrue(event.getRemovedList().contains(removedEntrant));
        assertEquals(6, event.getAllEntrantsList().size());
        assertTrue(event.getAllEntrantsList().containsAll(finalList));
    }
}
