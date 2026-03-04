package com.example.auroraevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
        Event event = new Event("organizer-xyz", "Sports Event", "Explore your favourite sport", new Date(), "Rec Centre", 20);
        assertEquals("organizer-xyz", event.getOrganizerDeviceId());
    }

    /**
     * Tests if the constructor sets the event name correctly
     */
    @Test
    public void testConstructorEventName() {
        // create an event
        Event event = new Event("organizer-xyz", "Sports Event", "Explore your favourite sport", new Date(), "Rec Centre", 20);
        assertEquals("Sports Event", event.getName());
    }

    /**
     * Tests if the constructor sets the event description correctly
     */
    @Test
    public void testConstructorEventDescription() {
        // create an event
        Event event = new Event("organizer-xyz", "Sports Event", "Explore your favourite sport", new Date(), "Rec Centre", 20);
        assertEquals("Explore your favourite sport", event.getDescription());
    }

    /**
     * Tests if the constructor sets the event date and time correctly
     */
    @Test
    public void testConstructorEventDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd / MM / yy");
        Calendar c = Calendar.getInstance();
        c.set(2026, 5, 6, 13, 30, 0);
        Date testDate = c.getTime();

        // create an event
        Event event = new Event("organizer-xyz", "Sports Event", "Explore your favourite sport", testDate, "Rec Centre", 20);
        assertEquals(sdf.format(testDate), sdf.format(event.getDateTime()));
    }

    /**
     * Tests if the constructor sets the event location correctly
     */
    @Test
    public void testConstructorEventLocation() {
        // create an event
        Event event = new Event("organizer-xyz", "Sports Event", "Explore your favourite sport", new Date(), "Rec Centre", 20);
        assertEquals("Rec Centre", event.getLocation());
    }
    /**
     * Tests if the constructor sets the event capacity correctly
     */
    @Test
    public void testConstructorEventCapacity() {
        // create an event
        Event event = new Event("organizer-xyz", "Sports Event", "Explore your favourite sport", new Date(), "Rec Centre", 20);
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
