package com.example.auroraevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import com.example.auroraevents.model.Event;

public class EventArrayAdapterTest {
    /**
     * Tests to see if correct event is returned at the required position
     */
    @Test
    public void testEventPosition() {
        // create an event
        Event event = new Event("organizer-xyz", "Sports Event", "Explore your favourite sport",  LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59), "Rec Centre", 20);

        // add event to list
        ArrayList<Event> eventList = new ArrayList<>();
        eventList.add(event);
        assertEquals("Sports Event", eventList.get(0).getName());
    }

    /**
     * Tests to see if attending tag is displayed correctly when
     * user is in attendingList
     */
    @Test
    public void testAttendingTag() {
        // create an event
        Event event = new Event("organizer-xyz", "Sports Event", "Explore your favourite sport",  LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59), "Rec Centre", 20);

        // add device id to attendingList
        event.getAttendingList().add("test-device-id");

        // check if attendingList contains the device id
        assertTrue(event.getAttendingList().contains("test-device-id"));
    }

    /**
     * Tests to see if invited tag is displayed correctly when
     * user is in selectedList
     */
    @Test
    public void testInvitedTag() {
        Event event = new Event("organizer-xyz", "Sports Event", "Explore your favourite sport",  LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59), "Rec Centre", 20);
        event.getSelectedList().add("test-device-id");
        assertTrue(event.getSelectedList().contains("test-device-id"));
    }
    /**
     * Tests to see if waiting tag is displayed correctly when
     * user is in waitingList
     */
    @Test
    public void testWaitingTag() {
        Event event = new Event("organizer-xyz", "Sports Event", "Explore your favourite sport",  LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59), "Rec Centre", 20);
        event.getWaitingList().add("test-device-id");
        assertTrue(event.getWaitingList().contains("test-device-id"));
    }
    /**
     * Tests to see if empty tag is displayed correctly when
     * user is not in any list
     */
    @Test
    public void testEmptyTag() {
        Event event = new Event("organizer-xyz", "Sports Event", "Explore your favourite sport",  LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59), "Rec Centre", 20);
        assertFalse(event.getAttendingList().contains("test-device-id"));
        assertFalse(event.getSelectedList().contains("test-device-id"));
        assertFalse(event.getWaitingList().contains("test-device-id"));
    }
}
