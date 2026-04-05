// Resources Used:
// https://www.baeldung.com/java-list-filter-by-any-matching-field
// https://www.baeldung.com/java-filter-collection-by-list
// https://stackoverflow.com/questions/63778403/how-to-implement-search-filter-with-recyclerview

package com.example.auroraevents.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Helper class to implement event filtering and keyword search logic
 * Filters public events based on:
 * Keyword search that includes matching against event name, description, and location
 * Applies filters for location, date range, and waiting list capacity
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
     * Checks if an event matches the searched query against event name, description, and location
     * If keyword search is empty, all events are returned
     * @param event the event to check for matching keyword
     * @param keywordSearchQuery the keyword that needs to be searched
     * @return true if keyword found or if the keyword search is empty
     */
    private boolean compareKeywordToMatch(Event event, String keywordSearchQuery) {
        // all events to be included when keyword search is empty
        if (keywordSearchQuery.isEmpty()) {
            return true;
        }
            String eventName = event.getName();
            if (eventName != null) {
                eventName = eventName.toLowerCase();
            } else {
                eventName = "";
            }

            String eventDescription = event.getDescription();
            if (eventDescription != null) {
                eventDescription = eventDescription.toLowerCase();
            } else {
                eventDescription = "";
            }

            String eventLocation = event.getLocation();
            if (eventLocation != null) {
                eventLocation = eventLocation.toLowerCase();
            } else {
                eventLocation = "";
            }
            // return true if any keyword matches
            return (eventName.contains(keywordSearchQuery) || eventDescription.contains(keywordSearchQuery) || eventLocation.contains(keywordSearchQuery));
        }

    /**
     * Checks if an event's location matches the required location query.
     * @param event the event to check for matching location
     * @param locationQuery the location filter that needs to be checked for
     * @return true if matching location is found or if no location filter is applied
     */
        private boolean checkMatchingLocation(Event event, String locationQuery) {
        if (locationQuery.isEmpty()) {
            return true;
        }
        String eventLocation = event.getLocation();
        if (eventLocation != null) {
            eventLocation = eventLocation.toLowerCase();
        } else {
            eventLocation = "";
        }
        return eventLocation.contains(locationQuery);
        }

    /**
     * Checks if an event's date matches the required date range query.
     * @param event event to check for matching dates
     * @param lowestEventDate earliest date to check for
     * @param highestEventDate latest date to check for
     * @return true if event date matches date range or if no date filter is applied
     */
        private boolean checkMatchingDates(Event event, LocalDate lowestEventDate, LocalDate highestEventDate) {
        if (lowestEventDate == null && highestEventDate == null) {
            return true;
        } else {
            LocalDate eventDate = event.getDateTimeAsLocalDate();

            if (lowestEventDate != null && eventDate.isBefore(lowestEventDate)) {
                return false;
            }
            if (highestEventDate != null && eventDate.isAfter(highestEventDate)) {
                return false;
            }
        }
        return true;
        }

    /**
     * Check if an event's waiting list capacity matched the required capacity query.
     * @param event event to check capacity for
     * @param maxEventCapacity maximum waiting list capacity to check for
     * @return true is capacity is within limit or if event has unlimited capacity or if capacity filter is not applied
     */
        private boolean checkMatchingCapacity(Event event, int maxEventCapacity) {
        if (maxEventCapacity == 0) {
            return true;
        }
        int maxWaitingListCapacity = event.registrationList.getWaitingCapacity();
        if (maxWaitingListCapacity == 0) {
            return true;
        }
        if (maxWaitingListCapacity <= maxEventCapacity) {
            return true;
        }
        return false;
        }

    /**
     * Checks entire event list against all filters.
     * Only displays public events after filtering.
     * @param eventList list containing all events
     * @param searchKeyword keyword to search for
     * @param locationFilter location to filter for
     * @param eventStartDate earliest date to filter for
     * @param eventEndDate latest date to filter for
     * @param maxEventCapacity maximum waititng list capacity to check for
     * @return list of all public events that match against any of the filters
     * or all public events when no filters are applied
     */
        public ArrayList<Event> applyAllFilters(ArrayList<com.example.auroraevents.model.Event> eventList, String searchKeyword, String locationFilter, java.time.LocalDate eventStartDate, java.time.LocalDate eventEndDate, int maxEventCapacity) {
        String keywordSearchQuery;
        if (searchKeyword != null) {
            keywordSearchQuery = searchKeyword.trim().toLowerCase();
        } else {
            keywordSearchQuery = "";
        }
        String locationQuery;
        if (locationFilter != null) {
            locationQuery = locationFilter.trim().toLowerCase();
        } else {
            locationQuery = "";
        }

        ArrayList<Event> allFilteredEvents = new ArrayList<>();

        for (Event event : eventList) {
            if (event.isPrivate()) {
                continue;
            }
            if (compareKeywordToMatch(event, keywordSearchQuery) && checkMatchingLocation(event, locationQuery) && checkMatchingDates(event, eventStartDate, eventEndDate) && checkMatchingCapacity(event, maxEventCapacity)) {
                allFilteredEvents.add(event);
            }
        }
        return allFilteredEvents;
    }
}