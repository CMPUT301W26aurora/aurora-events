package com.example.auroraevents;

import static org.junit.Assert.assertEquals;

import android.os.Bundle;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

public class EventFragmentTest {
    /**
     * Tests if event list is empty at start
     */
    @Test
    public void testEmptyEventList() {
        ArrayList<Event> eventList = new ArrayList<>();
        assertEquals(0, eventList.size());
    }

    /**
     * Tests is an event is successfully added to the event list
     */
    @Test
    public void testAddEventToList() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event = new Event("organizer-xyz", "Sports Event", "Explore your favourite sport", new Date(2026 - 1900, 5, 4), "Rec Centre", 20);
        event.setEventId("test-event-1");
        eventList.add(event);
        assertEquals(1, eventList.size());
    }

    /**
     * Tests if more than one event is successfully added to the event list
     */
    @Test
    public void testAddMoreEventsToList() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event1 = new Event("organizer-abc", "Singing event", "Showcase your talent", new Date(2026 - 1900, 2, 15), "Community Centre", 40);
        event1.setEventId("test-event-1");
        eventList.add(event1);

        Event event2 = new Event("organizer-def", "Art Event", "Display your work", new Date(2026 - 1900, 3, 10), "Arts Centre", 50);
        event2.setEventId("test-event-2");
        eventList.add(event2);

        assertEquals(2, eventList.size());
    }

    /**
     * Tests if correct position is returned for the tapped event
     */
    @Test
    public void testEventPosition() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event1 = new Event("organizer-abc", "Singing event", "Showcase your talent", new Date(2026 - 1900, 2, 15), "Community Centre", 40);
        event1.setEventId("test-event-1");
        eventList.add(event1);

        // get the position of the event tapped by the user
        Event tappedEvent = eventList.get(0);
        assertEquals("test-event-1", tappedEvent.getEventId());
    }
}