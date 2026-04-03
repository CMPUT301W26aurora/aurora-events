// Resources Used:
// https://www.baeldung.com/java-list-filter-by-any-matching-field
// https://www.baeldung.com/java-filter-collection-by-list
// https://stackoverflow.com/questions/63778403/how-to-implement-search-filter-with-recyclerview

package com.example.auroraevents.model;

import java.util.ArrayList;

public class EventFiltering {
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

    public ArrayList<Event> applyAllFilter(ArrayList<com.example.auroraevents.model.Event> eventList, String searchKeyword, String locationFilter, java.time.LocalDate eventStartDate, java.time.LocalDate eventEndDate, int maxEventCapacity) {
        ArrayList<Event> allFilteredEvents = filterKeywordEvents(searchKeyword, eventList);
        allFilteredEvents = applyLocationFilter(allFilteredEvents, locationFilter);
        allFilteredEvents = applyDateFilter(allFilteredEvents, eventStartDate, eventEndDate);
        allFilteredEvents = applyCapacityFilter(allFilteredEvents, maxEventCapacity);
        return allFilteredEvents;
    }
}