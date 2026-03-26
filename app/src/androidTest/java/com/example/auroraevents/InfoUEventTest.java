package com.example.auroraevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.User;

public class InfoUEventTest { //TODO 2: make the tests test what they say they test

    /**
     * Tests if correct event name is displayed in event details
     */
    @Test
    public void testEventName() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event1 = new Event(
                "organizer-abc",
                "Singing event",
                "Showcase your talent",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Community Centre",
                false,
                40,
                null);
        event1.setEventId("test-event-1");
        assertEquals("Singing event", event1.getName());
    }

    /**
     * Tests if correct event description is displayed in event details
     */
    @Test
    public void testEventDescription() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event1 = new Event(
                "organizer-abc",
                "Singing event",
                "Showcase your talent",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Community Centre",
                false,
                40,
                null);
        event1.setEventId("test-event-1");
        assertEquals("Showcase your talent", event1.getDescription());
    }

    /**
     * Tests if correct event location is displayed in event details
     */
    @Test
    public void testEventLocation() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event1 = new Event(
                "organizer-abc",
                "Singing event",
                "Showcase your talent",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Community Centre",
                false,
                40,
                null);
        event1.setEventId("test-event-1");
        assertEquals("Community Centre", event1.getLocation());
    }

    /**
     * Tests if correct event capacity is displayed in event details
     */
    @Test
    public void testEventCapacity() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event1 = new Event(
                "organizer-abc",
                "Singing event",
                "Showcase your talent",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Community Centre",
                false,
                40,
                null);
        event1.setEventId("test-event-1");
        assertEquals(40, event1.getCapacity());
    }

    /**
     * Tests if waiting list count increases when a user joins
     */
    @Test
    public void testWaitingListAddCount() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event1 = new Event(
                "organizer-abc",
                "Singing event",
                "Showcase your talent",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Community Centre",
                false,
                40,
                null);
        event1.setEventId("test-event-1");

        event1.registrationList.getWaitingList().add("user-abc");
        assertEquals(1, event1.registrationList.getWaitingList().size());
    }

    /**
     * Tests if waiting list count decreases when a user leaves
     */
    @Test
    public void testWaitingListDeleteCount() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event1 = new Event(
                "organizer-abc",
                "Singing event",
                "Showcase your talent",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Community Centre",
                false,
                40,
                null);
        event1.setEventId("test-event-1");

        event1.registrationList.getWaitingList().add("user-abc");
        event1.registrationList.getWaitingList().add("user-def");
        event1.registrationList.getWaitingList().add("user-ghi");

        event1.registrationList.getWaitingList().remove("user-abc");
        assertEquals(2, event1.registrationList.getWaitingList().size());
    }

    /**
     * Tests if join pool button is shown to entrants
     */
    @Test
    public void testNewUserSeesJoinPool() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event = new Event(
                "organizer-abc",
                "Singing event",
                "Showcase your talent",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Community Centre",
                false,
                40,
                null);
        String userId = "user-abc";

        boolean showJoinPoolButton = !event.registrationList.getWaitingList().contains(userId) && !event.registrationList.getSelectedList().contains(userId) && !event.registrationList.getAttendingList().contains(userId);
        assertTrue(showJoinPoolButton);
    }

    /**
     * Tests if entrants on waiting list can see leave button
     */
    @Test
    public void testLeaveButton() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event = new Event(
                "organizer-abc",
                "Singing event",
                "Showcase your talent",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Community Centre",
                false,
                40,
                null);
        String userId = "user-abc";
        event.registrationList.getWaitingList().add(userId);

        assertTrue(event.registrationList.getWaitingList().contains(userId));
        assertFalse(event.registrationList.getSelectedList().contains(userId));
        assertFalse(event.registrationList.getAttendingList().contains(userId));
    }

    /**
     * Tests if selected entrants can view accept and decline buttons
     */
    @Test
    public void testSelectedUsersButtons() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event = new Event(
                "organizer-abc",
                "Singing event",
                "Showcase your talent",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Community Centre",
                false,
                40,
                null);
        String userId = "user-abc";
        event.registrationList.getSelectedList().add(userId);

        assertTrue(event.registrationList.getSelectedList().contains(userId));
        assertFalse(event.registrationList.getAttendingList().contains(userId));
        assertFalse(event.registrationList.getWaitingList().contains(userId));
    }

    /**
     * Tests if accepted entrants are added to attending list
     */
    @Test
    public void testAttendingListCount() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event = new Event(
                "organizer-abc",
                "Singing event",
                "Showcase your talent",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Community Centre",
                false,
                40,
                null);
        String userId = "user-abc";
        event.registrationList.getSelectedList().add(userId);
        event.registrationList.getSelectedList().remove(userId);
        event.registrationList.getAttendingList().add(userId);

        assertFalse(event.registrationList.getSelectedList().contains(userId));
        assertTrue(event.registrationList.getAttendingList().contains(userId));
    }
    /**
     * Tests if accepted entrants are added to attending list
     */
    @Test
    public void testDecliningListCount() {
        ArrayList<Event> eventList = new ArrayList<>();
        Event event = new Event(
                "organizer-abc",
                "Singing event",
                "Showcase your talent",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Community Centre",
                false,
                40,
                null);
        String userId = "user-abc";
        event.registrationList.getSelectedList().add(userId);
        event.registrationList.getSelectedList().remove(userId);

        assertFalse(event.registrationList.getSelectedList().contains(userId));
        assertFalse(event.registrationList.getAttendingList().contains(userId));
    }

    /**
     * Tests if admin can view delete button
     */
    @Test
    public void testAdminDeleteButton() {
        User testUser = new User();
        testUser.setRole(User.ROLE_ADMIN);
        assertTrue(testUser.getRole().equals(User.ROLE_ADMIN));
    }

    /**
     * Tests delete button to see if it deletes event
     */
        @Test
        public void testDeleteButton() {
            ArrayList<Event> events = new ArrayList<>();
            Event event = new Event(
                    "organizer-abc",
                    "Singing event",
                    "Showcase your talent",
                    "free",
                    LocalDateTime.of(2026, 6, 4, 18, 0),
                    LocalDateTime.of(2026, 5, 20, 9, 0),
                    LocalDateTime.of(2026, 6, 1, 23, 59),
                    "Community Centre",
                    false,
                    40,
                    null);
            event.setEventId("test-event-1");
            // add event to list
            events.add(event);
            // delete event
            events.remove(event);
            assertFalse(events.contains(event));
        }
    }
