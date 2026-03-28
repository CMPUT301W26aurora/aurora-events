package com.example.auroraevents.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.auroraevents.server.EventDb;

import java.util.ArrayList;
import java.util.List;

public class Organizer extends User {
    private ArrayList<Event> myEvents;

    public Organizer() {
        super();
        setRole(User.ROLE_ORGANIZER);
        myEvents = new ArrayList<>();
    }

    public ArrayList<Event> getMyEvents() {
        return myEvents;
    }

    public void setMyEvents(ArrayList<Event> myEvents) {
        this.myEvents = myEvents;
    }

    /**
     * @param organizerDeviceId   The organizer's device ID
     * @param title               The title of the event
     * @param description         The event description
     * @param price               The event's price
     * @param date                The date of the event, format: yyyy-MM-dd HH:mm:ss
     * @param startTime           The start of the registration period, format: yyyy-MM-dd HH:mm:ss
     * @param endTime             The end of the registration period, format: yyyy-MM-dd HH:mm:ss
     * @param location            The event location
     * @param geolocationRequired Whether entrants need to be in the location to sign up
     * @param waitingCapacity     The total number of entrants that could attend the event
     * @param attendingCapacity   The total number of entrants that can join the waiting list
     * @param poster              A pretty picture for the event info screen
     */
    public void CreateEvent(
            String organizerDeviceId,
            String title,
            String description,
            String price,
            String date,
            String startTime,
            String endTime,
            String location,
            boolean geolocationRequired,
            int waitingCapacity,
            int attendingCapacity,
            Bitmap poster) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime eventDateTime          = LocalDateTime.parse(date, formatter);
        LocalDateTime eventRegistrationStart = LocalDateTime.parse(startTime, formatter);
        LocalDateTime eventRegistrationEnd   = LocalDateTime.parse(endTime, formatter);

        // Create event from parameters
        Event event = new Event(
                organizerDeviceId,
                title,
                description,
                price,
                eventDateTime,
                eventRegistrationStart,
                eventRegistrationEnd,
                location,
                geolocationRequired,
                waitingCapacity,
                attendingCapacity,
                poster);

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
     */
    public void sampleWaitList(Event event) {
        if (!(myEvents.contains(event))) {
            throw new IllegalArgumentException("Event not found");
        } else {
            event.registrationList.randomSampling();
        }
    }

    /**
     * Gets the list of users that are in the waiting list of the specified event
     * @param event
     * The event the organizer wants to get the waiting list of
     * @return
     * Return the waiting list of users in the specified event
     */
    public List<User> getEventWaitList(Event event) {
        if (myEvents.contains(event)) {
            return event.registrationList.getUsersFromDB(event.registrationList.getWaitingList());
        } else {
            throw new IllegalArgumentException("Event not found");
        }
    }
}