package com.example.auroraevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.example.auroraevents.model.Event;

public class EventTest {
    /**
     * Test to see if constructor initializes empty lists for the
     * six types of lists
     */
    @Test
    public void testEventConstructor() {
        // create an event
        Event event = new Event();

        assertTrue(event.getWaitingList().isEmpty());
        assertTrue(event.getSelectedList().isEmpty());
        assertTrue(event.getAttendingList().isEmpty());
        assertTrue(event.getDeclinedList().isEmpty());
        assertTrue(event.getCancelledList().isEmpty());
        assertTrue(event.getRemovedList().isEmpty());
    }

    /**
     * Tests if the constructor sets the event organizer ID correctly
     */
    @Test
    public void testConstructorEventOrganizerId() {
        // create an event
        Event event = new Event("organizer-xyz", "Sports Event", "Explore your favourite sport",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Rec Centre", 20);
        assertEquals("organizer-xyz", event.getOrganizerDeviceId());
    }

    /**
     * Tests if the constructor sets the event name correctly
     */
    @Test
    public void testConstructorEventName() {
        // create an event
        Event event = new Event("organizer-xyz", "Sports Event", "Explore your favourite sport",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Rec Centre", 20);
        assertEquals("Sports Event", event.getName());
    }

    /**
     * Tests if the constructor sets the event description correctly
     */
    @Test
    public void testConstructorEventDescription() {
        // create an event
        Event event = new Event("organizer-xyz", "Sports Event", "Explore your favourite sport",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Rec Centre", 20);
        assertEquals("Explore your favourite sport", event.getDescription());
    }

    /**
     * Tests LocalDate conversion
     */
    @Test
    public void testGetDateTimeAsLocalDate() {
        Event event = new Event(
                "organizer-xyz",
                "Sports Event",
                "Explore your favourite sport",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Rec Centre",
                20
        );

        assertEquals(LocalDate.of(2026, 6, 4), event.getDateTimeAsLocalDate());
    }

    /**
     * Tests registration start conversion
     */
    @Test
    public void testGetRegistrationTimeStartAsDateTime() {
        Event event = new Event(
                "organizer-xyz",
                "Sports Event",
                "Explore your favourite sport",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Rec Centre",
                20
        );

        assertEquals(LocalDateTime.of(2026, 5, 20, 9, 0),
                event.getRegistrationTimeStartAsDateTime());
    }

    /**
     * Tests registration end conversion
     */
    @Test
    public void testGetRegistrationTimeEndAsDateTime() {
        Event event = new Event(
                "organizer-xyz",
                "Sports Event",
                "Explore your favourite sport",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Rec Centre",
                20
        );

        assertEquals(LocalDateTime.of(2026, 6, 1, 23, 59),
                event.getRegistrationTimeEndAsDateTime());
    }

    /**
     * Tests if the constructor sets the event location correctly
     */
    @Test
    public void testConstructorEventLocation() {
        // create an event
        Event event = new Event("organizer-xyz", "Sports Event", "Explore your favourite sport",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Rec Centre", 20);
        assertEquals("Rec Centre", event.getLocation());
    }
    /**
     * Tests if the constructor sets the event capacity correctly
     */
    @Test
    public void testEventCapacity() {
        // create an event
        Event event = new Event("organizer-xyz", "Sports Event", "Explore your favourite sport",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Rec Centre", 20);
        assertEquals(20, event.getCapacity());
    }

    /**
     * Tests if event ID is set up and fetched correctly
     */
    @Test
    public void testSetAndGetEventId() {
        // create an event
        Event event = new Event();
        event.setEventId("test-event-id");
        assertEquals("test-event-id", event.getEventId());
    }

    /**
     * Tests if getWaitingList returns correct list
     */
    @Test
    public void testGetWaitingList() {
        Event event = new Event();
        event.getWaitingList().add("test-device-id");
        assertEquals(1, event.getWaitingList().size());
        assertTrue(event.getWaitingList().contains("test-device-id"));
    }

    /**
     * Tests if getSelectedList returns correct list
     */
    @Test
    public void testGetSelectedList() {
        Event event = new Event();
        event.getSelectedList().add("test-device-id-2");
        assertEquals(1, event.getSelectedList().size());
        assertTrue(event.getSelectedList().contains("test-device-id-2"));
    }


    /**
     * Tests if getAttendingList returns correct list
     */
    @Test
    public void testGetAttendingList() {
        Event event = new Event();
        event.getAttendingList().add("test-device-id-3");
        assertEquals(1, event.getAttendingList().size());
        assertTrue(event.getAttendingList().contains("test-device-id-3"));
    }

    /**
     * Tests if getDeclinedList returns correct list
     */
    @Test
    public void testGetDeclinedList() {
        Event event = new Event();
        event.getDeclinedList().add("test-device-id-4");
        assertEquals(1, event.getDeclinedList().size());
        assertTrue(event.getDeclinedList().contains("test-device-id-4"));
    }

    /**
     * Tests if getCancelledList returns correct list
     */
    @Test
    public void testGetCancelledList() {
        Event event = new Event();
        event.getCancelledList().add("test-device-id-5");
        assertEquals(1, event.getCancelledList().size());
        assertTrue(event.getCancelledList().contains("test-device-id-5"));
    }

    /**
     * Tests if getRemovedList returns correct list
     */
    @Test
    public void testGetRemovedList() {
        Event event = new Event();
        event.getRemovedList().add("test-device-id-6");
        assertEquals(1, event.getRemovedList().size());
        assertTrue(event.getRemovedList().contains("test-device-id-6"));
    }
}
