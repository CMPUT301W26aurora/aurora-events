package com.example.auroraevents.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import android.util.Log;

import com.example.auroraevents.server.EventDb;

import java.util.ArrayList;

public class Organizer extends User {
    private ArrayList<Event> myEvents;

    public Organizer() {
        super();
        setRole(User.ROLE_ORGANIZER);
        myEvents = new ArrayList<Event>();
    }

    public ArrayList<Event> getMyEvents() {
        return myEvents;
    }

    public void setMyEvents(ArrayList<Event> myEvents) {
        this.myEvents = myEvents;
    }

    /**
     * @param organizerDeviceId  The organizer's device ID
     * @param title              The title of the event
     * @param description        The event description
     * @param date               The date of the event, format: yyyy-MM-dd HH:mm:ss
     * @param startTime          The start of the registration period, format: yyyy-MM-dd HH:mm:ss
     * @param endTime            The end of the registration period, format: yyyy-MM-dd HH:mm:ss
     * @param location           The event location
     * @param capacity           The event capacity
     */
    public void CreateEvent(String organizerDeviceId, String title, String description, String date,
                            String startTime, String endTime, String location, int capacity) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime eventDateTime          = LocalDateTime.parse(date, formatter);
        LocalDateTime eventRegistrationStart = LocalDateTime.parse(startTime, formatter);
        LocalDateTime eventRegistrationEnd   = LocalDateTime.parse(endTime, formatter);

        // Create event from parameters
        Event event = new Event(organizerDeviceId, title, description,
                eventDateTime,
                eventRegistrationStart,
                eventRegistrationEnd,
                location, capacity);

        // Bug 3 fix: only add to local list after Firestore confirms success
        EventDb.addEvent(event,
                eventId -> {
                    Log.d("Organizer", "Event successfully created with ID: " + eventId);
                    myEvents.add(event);
                },
                e -> Log.e("Organizer", "Failed to create event: " + e.getMessage())
        );
    }

    /**
     * Randomly samples users in the waiting list of the specified event
     * @param event
     * Event that the organizer wants to sample in
     * @author Won Koh
     */
    public void sampleWaitList(Event event) {
        if (!(myEvents.contains(event))) {
            throw new IllegalArgumentException("Event not found");
        } else {
            event.randomSampling();
        }
    }

    /**
     * Gets the list of users that are in the waiting list of the specified event
     * @param event
     * The event the organizer wants to get the waiting list of
     * @return
     * Return the waiting list of users in the specified event
     * @author Won Koh
     */
    public ArrayList<User> getEventWaitList(Event event) {
        if (myEvents.contains(event)) {
            return event.getWaitingListOfUsers();
        } else {
            throw new IllegalArgumentException("Event not found");
        }
    }

    /**
     * Gets the list of users that were selected in the specified event
     * @param event
     * The event the organizer wants to get the selected list of
     * @return
     * Return the selected list of users in the specified event
     * @author Won Koh
     */
    public ArrayList<User> getEventSelectedList(Event event) {
        if (myEvents.contains(event)) {
            return event.getSelectedListOfUsers();
        } else {
            throw new IllegalArgumentException("Event not found");
        }
    }

    /**
     * Gets the list of users that are in the attending the specified event
     * @param event
     * The event the organizer wants to get the attending list of
     * @return
     * Return the attending list of users in the specified event
     * @author Won Koh
     */
    public ArrayList<User> getEventAttendingList(Event event) {
        if (myEvents.contains(event)) {
            return event.getAttendingListOfUsers();
        } else {
            throw new IllegalArgumentException("Event not found");
        }
    }

    /**
     * Gets the list of users that cancelled the invitation to the specified event
     * @param event
     * The event the organizer wants to get the cancelled list of
     * @return
     * Return the cancelled list of users in the specified event
     * @author Won Koh
     */
    public ArrayList<User> getCancelledWaitList(Event event) {
        if (myEvents.contains(event)) {
            return event.getWaitingListOfUsers();
        } else {
            throw new IllegalArgumentException("Event not found");
        }
    }
}