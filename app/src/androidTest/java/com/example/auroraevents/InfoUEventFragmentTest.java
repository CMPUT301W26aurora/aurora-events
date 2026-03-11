package com.example.auroraevents;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

public class InfoUEventFragmentTest {

    /**
     * Tests if correct event name is displayed in event details
     */
    @Test
    public void testEventName() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event1 = new Event("organizer-abc", "Singing event", "Showcase your talent", new Date(2026 - 1900, 2, 15), "Community Centre", 40);
        event1.setEventId("test-event-1");
        assertEquals("Singing event", event1.getName());
    }

    /**
     * Tests if correct event description is displayed in event details
     */
    @Test
    public void testEventDescription() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event1 = new Event("organizer-abc", "Singing event", "Showcase your talent", new Date(2026 - 1900, 2, 15), "Community Centre", 40);
        event1.setEventId("test-event-1");
        assertEquals("Showcase your talent", event1.getDescription());
    }

    /**
     * Tests if correct event location is displayed in event details
     */
    @Test
    public void testEventLocation() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event1 = new Event("organizer-abc", "Singing event", "Showcase your talent", new Date(2026 - 1900, 2, 15), "Community Centre", 40);
        event1.setEventId("test-event-1");
        assertEquals("Community Centre", event1.getLocation());
    }

    /**
     * Tests if correct event capacity is displayed in event details
     */
    @Test
    public void testEventCapacity() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event1 = new Event("organizer-abc", "Singing event", "Showcase your talent", new Date(2026 - 1900, 2, 15), "Community Centre", 40);
        event1.setEventId("test-event-1");
        assertEquals(40, event1.getCapacity());
    }

    /**
     * Tests if waiting list count increases when a user joins
     */
    @Test
    public void testWaitingListAddCount() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event1 = new Event("organizer-abc", "Singing event", "Showcase your talent", new Date(2026 - 1900, 2, 15), "Community Centre", 40);
        event1.setEventId("test-event-1");

        event1.getWaitingList().add("user-abc");
        assertEquals(1, event1.getWaitingList().size());
    }

    /**
     * Tests if waiting list count decreases when a user leaves
     */
    @Test
    public void testWaitingListDeleteCount() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event1 = new Event("organizer-abc", "Singing event", "Showcase your talent", new Date(2026 - 1900, 2, 15), "Community Centre", 40);
        event1.setEventId("test-event-1");

        event1.getWaitingList().add("user-abc");
        event1.getWaitingList().add("user-def");
        event1.getWaitingList().add("user-ghi");

        event1.getWaitingList().remove("user-abc");
        assertEquals(2, event1.getWaitingList().size());
    }
}
