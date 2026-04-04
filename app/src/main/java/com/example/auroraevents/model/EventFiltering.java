// Resources Used:
// https://www.baeldung.com/java-list-filter-by-any-matching-field
// https://www.baeldung.com/java-filter-collection-by-list
// https://stackoverflow.com/questions/63778403/how-to-implement-search-filter-with-recyclerview

package com.example.auroraevents.model;

import java.util.ArrayList;

/**
 * Helper class to implement event filtering and keyword search logic
 * Filters events based on:
 * keyword search that includes event name, description, location
 * location, date, and capacity filters
 * Only displays public events when filters are applied
 */
public class EventFiltering {
    /**
     * Filters events to display public events that match the searched keywords
     * Matches keywords against event name, location, and description
     * @param searchKeyword keyword entered by user to search for
     * @param eventList list of all events to search through
     * @return list of public events that match the searched keywords
     */
    public ArrayList<Event> filterKeywordEvents(String searchKeyword, ArrayList<com.example.auroraevents.model.Event> eventList) {
        String searchedQuery = searchKeyword.toLowerCase();
        ArrayList<com.example.auroraevents.model.Event> filteredEventsList = new ArrayList<>();

        if (searchedQuery.isEmpty()) {
            // display all public events when search is cleared
            for (com.example.auroraevents.model.Event event : eventList) {
                if (!event.isPrivate()) {
                    filteredEventsList.add(event);
                }
            }
        } else {
            for (com.example.auroraevents.model.Event event : eventList) {
                // don't include private events for keyword search
                if (event.isPrivate()) continue;
                // convert event name to lower case
                String searchedEventName = event.getName();
                if (searchedEventName != null) {
                    searchedEventName = searchedEventName.toLowerCase();
                } else {
                    searchedEventName = "";
                }
                // convert event description to lower case
                String searchedEventDescription = event.getDescription();
                if (searchedEventDescription != null) {
                    searchedEventDescription = searchedEventDescription.toLowerCase();
                } else {
                    searchedEventDescription = "";
                }
                String searchedEventLocation = event.getLocation();
                if (searchedEventLocation != null) {
                    searchedEventLocation = searchedEventLocation.toLowerCase();
                } else {
                    searchedEventLocation = "";
                }
                // add event to filtered list if either of the keyword matches
                if (searchedEventName.contains(searchedQuery) || searchedEventDescription.contains(searchedQuery) || searchedEventLocation.contains(searchedQuery)) {
                    filteredEventsList.add(event);
                }
            }
        }
        return filteredEventsList;
    }

    /**
     * Filter events to display all public events that match the required location
     * @param eventList list of all events to filter through
     * @param locationFilter location keyword required to match against location of all events
     * @return filtered list containing matching location events
     */
    public ArrayList<Event> applyLocationFilter(ArrayList<com.example.auroraevents.model.Event> eventList, String locationFilter) {
        if (locationFilter == null || locationFilter.trim().isEmpty()) {
            ArrayList<Event> allEventsList = new ArrayList<>(eventList);
            return allEventsList;
        }
        String locationQuery = locationFilter.trim().toLowerCase();
        ArrayList<Event> locationFilterResults = new ArrayList<>();

        for (Event event : eventList) {
            if (event.isPrivate()) continue;

            String eventLocation;
            if (event.getLocation() != null) {
                eventLocation = event.getLocation().toLowerCase();
            } else {
                eventLocation = "";
            }

            if (eventLocation.contains(locationQuery)) {
                locationFilterResults.add(event);
            }
        }
        return locationFilterResults;
    }

    /**
     * Filters events to display all public events that match the given date range
     * @param eventList list of all events to filter through
     * @param eventStartDate earliest event date
     * @param eventEndDate latest event date
     * @return filtered list containing events within the required date range
     */
    public ArrayList<Event> applyDateFilter(ArrayList<com.example.auroraevents.model.Event> eventList, java.time.LocalDate eventStartDate, java.time.LocalDate eventEndDate) {
        if (eventStartDate == null && eventEndDate == null) {
            ArrayList<Event> allEventsList = new ArrayList<>(eventList);
            return allEventsList;
        }
        ArrayList<Event> dateFilterResults = new ArrayList<>();

        for (Event event : eventList) {
            if (event.isPrivate()) continue;

            java.time.LocalDate eventDate = event.getDateTimeAsLocalDate();

            if (eventStartDate != null && eventDate.isBefore(eventStartDate)) {
                continue;
            }

            if (eventEndDate != null && eventDate.isAfter(eventEndDate)) {
                continue;
            }
            dateFilterResults.add(event);
        }
        return dateFilterResults;
    }

    /**
     * Filter events to display all public events whose waiting list capacity matches the given maximum capacity
     * Returns all events when max capacity is 0 i.e. no filter applied
     * @param eventList list of all events to filter through
     * @param maxEventCapacity maximum waiting list capacity to display
     * @return filtered list of events within the maximum waiting capacity
     */
    public ArrayList<Event> applyCapacityFilter(ArrayList<com.example.auroraevents.model.Event> eventList, int maxEventCapacity) {
        if (maxEventCapacity == 0) {
            ArrayList<Event> allEventsList = new ArrayList<>(eventList);
            return allEventsList;
        }
        ArrayList<Event> capacityFilterResults = new ArrayList<>();

        for (Event event : eventList) {
            if (event.isPrivate()) {
                continue;
            }
            int eventWaitingListCapacity = event.registrationList.getWaitingCapacity();
            if (eventWaitingListCapacity == 0 || eventWaitingListCapacity <= maxEventCapacity) {
                capacityFilterResults.add(event);
            }
        }
        return capacityFilterResults;
    }

    /**
     * Filters events to display public events that match when all the filters are applied at once
     * @param eventList list of all events to filter through
     * @param searchKeyword keyword to search matching events
     * @param locationFilter required location to match events
     * @param eventStartDate required earliest date to match events
     * @param eventEndDate required latest date to match events
     * @param maxEventCapacity required max waiting list capacity to match events
     * @return all public events that match all the applied filters
     */
    public ArrayList<Event> applyAllFilters(ArrayList<com.example.auroraevents.model.Event> eventList, String searchKeyword, String locationFilter, java.time.LocalDate eventStartDate, java.time.LocalDate eventEndDate, int maxEventCapacity) {
        ArrayList<Event> allFilteredEvents = filterKeywordEvents(searchKeyword, eventList);
        allFilteredEvents = applyLocationFilter(allFilteredEvents, locationFilter);
        allFilteredEvents = applyDateFilter(allFilteredEvents, eventStartDate, eventEndDate);
        allFilteredEvents = applyCapacityFilter(allFilteredEvents, maxEventCapacity);
        return allFilteredEvents;
    }
}