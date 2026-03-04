package com.example.auroraevents;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * This class contains tests for the Organizer functionality
 */
public class OrganizerTests {
    Organizer organizer;

    /**
     * Tests event creation
     */
    @Test
    public void testCreateEvent() {
        organizer = new Organizer();

        String deviceId = "dev_123";
        String title = "Tech Conference";
        String description = "A cool dev event";
        String date = "2026-05-20";
        String time = "10:00:00";
        String startTime = "2026-05-01 08:00:00";
        String endTime = "2026-05-19 17:00:00";
        String location = "Edmonton Convention Centre";
        int capacity = 100;

        organizer.CreateEvent(deviceId, title, description, date,
                time, startTime, endTime, location, capacity);

        ArrayList<Event> events = organizer.getMyEvents();

        assertEquals("Event list should have 1 item", 1, events.size());

        Event createdEvent = events.get(0);
        assertEquals(title, createdEvent.getName());
        assertEquals(LocalDate.parse(date), createdEvent.getDateTime());
        assertEquals(capacity, createdEvent.getCapacity());
    }
}