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

        assertTrue(event.registrationList.getWaitingList().isEmpty());
        assertTrue(event.registrationList.getSelectedList().isEmpty());
        assertTrue(event.registrationList.getAttendingList().isEmpty());
        assertTrue(event.registrationList.getDeclinedList().isEmpty());
        assertTrue(event.registrationList.getCancelledList().isEmpty());
        assertTrue(event.registrationList.getRemovedList().isEmpty());
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
        event.registrationList.getWaitingList().add("test-device-id");
        assertEquals(1, event.registrationList.getWaitingList().size());
        assertTrue(event.registrationList.getWaitingList().contains("test-device-id"));
    }

    /**
     * Tests if getSelectedList returns correct list
     */
    @Test
    public void testGetSelectedList() {
        Event event = new Event();
        event.registrationList.getSelectedList().add("test-device-id-2");
        assertEquals(1, event.registrationList.getSelectedList().size());
        assertTrue(event.registrationList.getSelectedList().contains("test-device-id-2"));
    }


    /**
     * Tests if getAttendingList returns correct list
     */
    @Test
    public void testGetAttendingList() {
        Event event = new Event();
        event.registrationList.getAttendingList().add("test-device-id-3");
        assertEquals(1, event.registrationList.getAttendingList().size());
        assertTrue(event.registrationList.getAttendingList().contains("test-device-id-3"));
    }

    /**
     * Tests if getDeclinedList returns correct list
     */
    @Test
    public void testGetDeclinedList() {
        Event event = new Event();
        event.registrationList.getDeclinedList().add("test-device-id-4");
        assertEquals(1, event.registrationList.getDeclinedList().size());
        assertTrue(event.registrationList.getDeclinedList().contains("test-device-id-4"));
    }

    /**
     * Tests if getCancelledList returns correct list
     */
    @Test
    public void testGetCancelledList() {
        Event event = new Event();
        event.registrationList.getCancelledList().add("test-device-id-5");
        assertEquals(1, event.registrationList.getCancelledList().size());
        assertTrue(event.registrationList.getCancelledList().contains("test-device-id-5"));
    }

    /**
     * Tests if getRemovedList returns correct list
     */
    @Test
    public void testGetRemovedList() {
        Event event = new Event();
        event.registrationList.getRemovedList().add("test-device-id-6");
        assertEquals(1, event.registrationList.getRemovedList().size());
        assertTrue(event.registrationList.getRemovedList().contains("test-device-id-6"));
    }
}
