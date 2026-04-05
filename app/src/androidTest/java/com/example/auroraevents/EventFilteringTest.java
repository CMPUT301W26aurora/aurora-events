package com.example.auroraevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.EventFiltering;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class EventFilteringTest {
    private EventFiltering eventFiltering;
    private ArrayList<Event> eventList;

    private Event publicEvent1;
    private Event publicEvent2;
    private Event privateEvent;

    @Before
    public void eventListCreation() {
        eventFiltering = new EventFiltering();
        eventList = new ArrayList<>();

        publicEvent1 = new Event(
                "organizer-abc",
                "Music Event",
                "Music performances",
                "free",
                LocalDateTime.of(2026, 6, 15, 17, 0),
                LocalDateTime.of(2026, 5, 1, 9, 0),
                LocalDateTime.of(2026, 6, 1, 23, 59),
                "Edmonton Community Centre",
                false,
                50,
                100,
                null);
        publicEvent1.setEventId("event-1");
        publicEvent1.setPrivate(false);

        publicEvent2 = new Event(
                "organizer-def",
                "Art Event",
                "Art display",
                "free",
                LocalDateTime.of(2026, 8, 20, 10, 0),
                LocalDateTime.of(2026, 7, 1, 9, 0),
                LocalDateTime.of(2026, 8, 15, 23, 59),
                "Calgary Art Gallery",
                false,
                20,
                40,
                null);
        publicEvent2.setEventId("event-2");
        publicEvent2.setPrivate(false);

        privateEvent = new Event(
                "organizer-xyz",
                "Private Event",
                "Selected people only",
                "free",
                LocalDateTime.of(2026, 7, 10, 14, 0),
                LocalDateTime.of(2026, 6, 1, 9, 0),
                LocalDateTime.of(2026, 7, 5, 23, 59),
                "Community centre",
                false,
                10,
                20,
                null);
        privateEvent.setEventId("event-private");
        privateEvent.setPrivate(true);

        eventList.add(publicEvent1);
        eventList.add(publicEvent2);
        eventList.add(privateEvent);
    }

    /**
     * Tests if filter keyword matches correct event name
     */
    @Test
    public void testFilterKeywordMatchesName() {
        ArrayList<Event> result = eventFiltering.filterKeywordEvents("music", eventList);
        assertEquals(1, result.size());
        assertEquals("event-1", result.get(0).getEventId());
    }

    /**
     * Tests if filter keyword matches correct event description
     */
    @Test
    public void testFilterKeywordMatchesDescription() {
        ArrayList<Event> result = eventFiltering.filterKeywordEvents("art", eventList);
        assertEquals(1, result.size());
        assertEquals("event-2", result.get(0).getEventId());
    }

    /**
     * Tests if filter keyword matches correct event location
     */
    @Test
    public void testFilterKeywordMatchesLocation() {
        ArrayList<Event> result = eventFiltering.filterKeywordEvents("edmonton", eventList);
        assertEquals(1, result.size());
        assertEquals("event-1", result.get(0).getEventId());
    }

    /**
     * Tests if filter keyword displays all public events
     */
    @Test
    public void testFilterKeywordDisplaysPublicEvents() {
        ArrayList<Event> result = eventFiltering.filterKeywordEvents("", eventList);
        assertEquals(2, result.size());
    }

    /**
     * Tests if private events are not displayed when keyword matches their name
     */
    @Test
    public void testFilterKeywordExcludesPrivateEvents() {
        ArrayList<Event> result = eventFiltering.filterKeywordEvents("private", eventList);
        assertTrue(result.isEmpty());
    }

    /**
     * Tests if no results are displayed when keyword does not match any event
     */
    @Test
    public void testFilterKeywordNoResults() {
        ArrayList<Event> result = eventFiltering.filterKeywordEvents("basketball", eventList);
        assertTrue(result.isEmpty());
    }

    /**
     * Tests location filter returns matching events
     */
    @Test
    public void testApplyLocationFilterMatchesLocation() {
        ArrayList<Event> result = eventFiltering.applyAllFilters(eventList, "", "Edmonton", null, null, 0);
        assertEquals(1, result.size());
        assertEquals("event-1", result.get(0).getEventId());
    }

    /**
     * Tests that empty location filter returns all public events
     */
    @Test
    public void testApplyLocationFilterReturnsAllEvents() {
        ArrayList<Event> result = eventFiltering.applyAllFilters(eventList, "", "", null, null, 0);
        assertEquals(2, result.size());
    }

    /**
     * Tests that location filter returns no events when nothing matches
     */
    @Test
    public void testApplyLocationFilterNoMatchedEvents() {
        ArrayList<Event> result = eventFiltering.applyAllFilters(eventList, "", "Toronto", null, null, 0);
        assertTrue(result.isEmpty());
    }

    /**
     * Tests that date filter returns events within the required range
     */
    @Test
    public void testApplyDateFilter() {
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 7, 1);

        ArrayList<Event> result = eventFiltering.applyAllFilters(eventList, "", "", startDate, endDate, 0);
        assertEquals(1, result.size());
        assertEquals("event-1", result.get(0).getEventId());
    }

    /**
     * Tests that no start date doesn't apply a filter for lower bound event date
     */
    @Test
    public void testApplyDateFilterNoStartDate() {
        LocalDate endDate = LocalDate.of(2026, 7, 1);

        // display all public events on or before July 1 2026
        ArrayList<Event> result = eventFiltering.applyAllFilters(eventList, "", "", null, endDate, 0);
        assertEquals(1, result.size());
        assertEquals("event-1", result.get(0).getEventId());
    }

    /**
     * Tests that no end date doesn't apply a filter for upper bound event dates
     */
    @Test
    public void testApplyDateFilterNoEndDate() {
        LocalDate startDate = LocalDate.of(2026, 7, 1);

        // display all public events on or after July 1 2026
        ArrayList<Event> result = eventFiltering.applyAllFilters(eventList, "", "", startDate, null, 0);
        assertEquals(1, result.size());
        assertEquals("event-2", result.get(0).getEventId());
    }

    /**
     * Tests that events outside the date range are not displayed
     */
    @Test
    public void testApplyDateFilterOutsideRange() {
        LocalDate startDate = LocalDate.of(2026, 9, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);

        ArrayList<Event> result = eventFiltering.applyAllFilters(eventList, "", "", startDate, endDate, 0);
        assertTrue(result.isEmpty());
    }

    /**
     * Tests that capacity filter returns events within the capacity limit
     */
    @Test
    public void testApplyCapacityFilter() {
        ArrayList<Event> result = eventFiltering.applyAllFilters(eventList, "", "", null, null, 25);
        assertEquals(1, result.size());
        assertEquals("event-2", result.get(0).getEventId());
    }

    /**
     * Tests that capacity filter with a capacity of 0 returns all events
     */
    @Test
    public void testApplyCapacityFilterNoCapacity() {
        ArrayList<Event> result = eventFiltering.applyAllFilters(eventList, "", "", null, null, 0);
        assertEquals(2, result.size());
    }

    /**
     * Tests that applyAllFilters with no applied filters displays all public events
     */
    @Test
    public void testApplyAllFilters() {
        ArrayList<Event> result = eventFiltering.applyAllFilters(eventList, "", "", null, null, 0);
        assertEquals(2, result.size());
    }

    /**
     * Tests that multiple filters applied display matching events
     */
    @Test
    public void testApplyAllFiltersMultipleFiltersApplied() {
        ArrayList<Event> result = eventFiltering.applyAllFilters(eventList, "music", "edmonton", null, null, 0);
        assertEquals(1, result.size());
        assertEquals("event-1", result.get(0).getEventId());
    }

    /**
     * Tests that applyAllFilters displays no events when multiple filter applied match no event
     */
    @Test
    public void testApplyAllFiltersNoMatchedEvents() {
        ArrayList<Event> result = eventFiltering.applyAllFilters(eventList, "music", "calgary", null, null, 0);
        assertTrue(result.isEmpty());
    }
}
