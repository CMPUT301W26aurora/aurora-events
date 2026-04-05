package com.example.auroraevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.view.EventFragment;

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
        Event event = new Event(
                "organizer-xyz",
                "Sports Event",
                "Explore your favourite sport",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Rec Centre", false,0,0);
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
        Event event1 = new Event(
                "organizer-xyz",
                "Sports Event",
                "Explore your favourite sport",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Rec Centre", false,0,0);
        event1.setEventId("test-event-1");
        eventList.add(event1);

        Event event2 = new Event(
                "organizer-xyz",
                "Sports Event",
                "Explore your favourite sport",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Rec Centre", false,0,0);
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
        Event event1 = new Event(
                "organizer-xyz",
                "Sports Event",
                "Explore your favourite sport",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Rec Centre", false,0,0);
        event1.setEventId("test-event-1");
        eventList.add(event1);

        // get the position of the event tapped by the user
        Event tappedEvent = eventList.get(0);
        assertEquals("test-event-1", tappedEvent.getEventId());
    }

    /**
     * Tests if search keyword matches event name
     */
    @Test
    public void testEventSearchName() {
        EventFragment eventFragment = new EventFragment();
        ArrayList<Event> allEventsList = new ArrayList<>();

        Event event1 = new Event(
                "organizer-xyz",
                "Sports Event",
                "Explore your favourite sport",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Rec Centre", false,0,0);
        event1.setEventId("test-event-1");
        allEventsList.add(event1);

        ArrayList<Event> searchResult = eventFragment.filterKeywordEvents("singing", allEventsList);
        assertEquals(1, searchResult.size());
        assertEquals("test-event-1", searchResult.get(0).getEventId());
    }

    /**
     * Tests if search keyword matched event description
     */
    @Test
    public void testEventSearchDescription() {
        EventFragment eventFragment = new EventFragment();
        ArrayList<Event> allEventsList = new ArrayList<>();

        Event event1 = new Event(
                "organizer-xyz",
                "Sports Event",
                "Explore your favourite sport",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Rec Centre", false,0,0);
        event1.setEventId("test-event-1");
        allEventsList.add(event1);

        ArrayList<Event> result = eventFragment.filterKeywordEvents("talent", allEventsList);
        assertEquals(1, result.size());
        assertEquals("test-event-1", result.get(0).getEventId());
    }

    /**
     * Tests lower case conversion
     */
    @Test
    public void testLowerCaseConversion() {
        EventFragment eventFragment = new EventFragment();
        ArrayList<Event> allEventsList = new ArrayList<>();

        Event event1 = new Event(
                "organizer-xyz",
                "Sports Event",
                "Explore your favourite sport",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Rec Centre", false,0,0);
        event1.setEventId("test-event-1");
        allEventsList.add(event1);

        ArrayList<Event> result = eventFragment.filterKeywordEvents("SPORT", allEventsList);
        assertEquals(1, result.size());
    }

    /**
     * Tests if empty list is returned when there are no search results
     */
    @Test
    public void testSearchNoResults() {
        EventFragment eventFragment = new EventFragment();
        ArrayList<Event> allEventsList = new ArrayList<>();

        Event event1 = new Event(
                "organizer-xyz",
                "Sports Event",
                "Explore your favourite sport",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Rec Centre", false,0,0);
        event1.setEventId("test-event-1");
        allEventsList.add(event1);

        ArrayList<Event> searchResult = eventFragment.filterKeywordEvents("Music", allEventsList);
        assertTrue(searchResult.isEmpty());
    }

    /**
     * Tests if all events are displayed when search bar is cleared
     */
    @Test
    public void testClearedSearch() {
        EventFragment eventFragment = new EventFragment();
        ArrayList<Event> allEventsList = new ArrayList<>();

        Event event1 = new Event(
                "organizer-xyz",
                "Sports Event",
                "Explore your favourite sport",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Rec Centre", false,0,0);
        event1.setEventId("test-event-1");

        Event event2 =new Event(
                "organizer-xyz",
                "Sports Event",
                "Explore your favourite sport",
                "free",
                LocalDateTime.of(2026, 6, 4, 18, 0),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Rec Centre", false,0,0);
        event2.setEventId("test-event-2");

        allEventsList.add(event1);
        allEventsList.add(event2);

        // search to match keyword
        ArrayList<Event> filteredEvents = eventFragment.filterKeywordEvents("sport", allEventsList);
        assertEquals(1, filteredEvents.size());

        // clear search bar
        ArrayList<Event> originalEvents = eventFragment.filterKeywordEvents("", allEventsList);
        assertEquals(2, originalEvents.size());
    }

    /**
     * Tests that private events are not displayed during keyword search
     */
    @Test
    public void testPrivateEventNotShownInResults() {
        EventFragment eventFragment = new EventFragment();
        ArrayList<Event> allEventsList = new ArrayList<>();

        Event publicEvent = new Event(
                "organizer-xyz",
                "Sports event",
                "Explore your favourite sport",
                "free",
                LocalDateTime.of(2026, 3, 15, 17, 0),
                LocalDateTime.of(2026, 3, 1, 9, 0),
                LocalDateTime.of(2026, 3, 10, 23, 59),
                "Rec Centre",
                false,
                -1,
                20,
                null);
        publicEvent.setEventId("test-event-1");
        publicEvent.setPrivate(false);

        Event privateEvent = new Event(
                "organizer-def",
                "Art event",
                "Display your work",
                "free",
                LocalDateTime.of(2026, 3, 15, 17, 0),
                LocalDateTime.of(2026, 3, 1, 9, 0),
                LocalDateTime.of(2026, 3, 10, 23, 59),
                "Arts Centre",
                false,
                -1,
                50,
                null);
        privateEvent.setEventId("test-event-2");
        privateEvent.setPrivate(true);

        allEventsList.add(publicEvent);
        allEventsList.add(privateEvent);

        ArrayList<Event> searchResult = eventFragment.filterKeywordEvents("", allEventsList);
        assertEquals(1, searchResult.size());
        assertEquals("test-event-1", searchResult.get(0).getEventId());
    }

    /**
     * Test if keyword search matches the correct event location
     */
    @Test
    public void testEventSearchLocation() {
        EventFragment eventFragment = new EventFragment();
        ArrayList<Event> allEventsList = new ArrayList<>();

        Event event1 = new Event(
                "organizer-abc",
                "Singing event",
                "Showcase your talent",
                "free",
                LocalDateTime.of(2026, 3, 15, 17, 0),
                LocalDateTime.of(2026, 3, 1, 9, 0),
                LocalDateTime.of(2026, 3, 10, 23, 59),
                "Edmonton Community Centre",
                false,
                -1,
                40,
                null);
        event1.setEventId("test-event-1");
        allEventsList.add(event1);

        // check if event location matches the search location
        ArrayList<Event> searchResult = eventFragment.filterKeywordEvents("edmonton", allEventsList);
        assertEquals(1, searchResult.size());
        assertEquals("test-event-1", searchResult.get(0).getEventId());
    }

    /**
     * Tests if keyword search matches multiple events with same keywords
     */
    @Test
    public void testSearchMultipleEvents() {
        EventFragment eventFragment = new EventFragment();
        ArrayList<Event> allEventsList = new ArrayList<>();

        Event event1 = new Event(
                "organizer-abc",
                "Sports event",
                "Explore your favourite sport",
                "free",
                LocalDateTime.of(2026, 3, 15, 17, 0),
                LocalDateTime.of(2026, 3, 1, 9, 0),
                LocalDateTime.of(2026, 3, 10, 23, 59),
                "Rec Centre",
                false,
                -1,
                40,
                null);
        event1.setEventId("test-event-1");

        Event event2 = new Event(
                "organizer-def",
                "Sports gala",
                "Annual sports event",
                "free",
                LocalDateTime.of(2026, 4, 10, 17, 0),
                LocalDateTime.of(2026, 3, 1, 9, 0),
                LocalDateTime.of(2026, 4, 5, 23, 59),
                "Community Centre",
                false,
                -1,
                50,
                null);
        event2.setEventId("test-event-2");

        allEventsList.add(event1);
        allEventsList.add(event2);

        // check if search keyword displays both events that have the keyword sports
        ArrayList<Event> result = eventFragment.filterKeywordEvents("sport", allEventsList);
        assertEquals(2, result.size());
    }
}