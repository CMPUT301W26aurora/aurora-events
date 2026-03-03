package com.example.auroraevents;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

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
}
