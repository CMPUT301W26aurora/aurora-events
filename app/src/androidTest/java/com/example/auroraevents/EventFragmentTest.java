package com.example.auroraevents;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import com.example.auroraevents.model.Event;

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
     * Tests if an event is successfully added to the event list
     */
    @Test
    public void testAddEventToList() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event = new Event("organizer-xyz", "Sports Event", "Explore your favourite sport",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Rec Centre", 20);
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
        Event event1 = new Event("organizer-abc", "Singing event", "Showcase your talent",
                LocalDateTime.of(2026, 3, 15, 17, 0),
                LocalDateTime.of(2026, 3, 1, 9, 0),
                LocalDateTime.of(2026, 3, 10, 23, 59),
                "Community Centre", 40);
        event1.setEventId("test-event-1");
        eventList.add(event1);

        Event event2 = new Event("organizer-def", "Art Event", "Display your work",
                LocalDateTime.of(2026, 3, 15, 17, 0),
                LocalDateTime.of(2026, 3, 1, 9, 0),
                LocalDateTime.of(2026, 3, 10, 23, 59),
                "Arts Centre", 50);
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
        Event event1 = new Event("organizer-abc", "Singing event", "Showcase your talent",
                LocalDateTime.of(2026, 3, 15, 17, 0),
                LocalDateTime.of(2026, 3, 1, 9, 0),
                LocalDateTime.of(2026, 3, 10, 23, 59),
                "Community Centre", 40);
        event1.setEventId("test-event-1");
        eventList.add(event1);

        // get the position of the event tapped by the user
        Event tappedEvent = eventList.get(0);
        assertEquals("test-event-1", tappedEvent.getEventId());
    }
}