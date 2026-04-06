package com.example.auroraevents.model;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.auroraevents.server.EventDb;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Organizer extends User {
    private ArrayList<Event> myEvents;
    private String deviceID;
    private String name;
    private String email;
    private String phoneNumber;
    private String role;
    private boolean isAdmin;

    public Organizer() {
        super();
        setRole(User.ROLE_ORGANIZER);
        myEvents = new ArrayList<>();
    }

    public Organizer(String deviceID, String name, String email, String phoneNumber, String role, boolean isAdmin) {
        super(deviceID, name, email, phoneNumber, role, isAdmin);
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
            boolean isPrivate,
            Bitmap poster,
            EventDb.OnEventCreatedCallback onCreated) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime eventDateTime          = LocalDateTime.parse(date, formatter);
        LocalDateTime eventRegistrationStart = (startTime != null) ? LocalDateTime.parse(startTime, formatter) : null;
        LocalDateTime eventRegistrationEnd   = (endTime != null)   ? LocalDateTime.parse(endTime, formatter)   : null;

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
                attendingCapacity);
        event.setPrivate(isPrivate);

        EventDb.addEvent(event,
                eventId -> {
                    Log.d("Organizer", "Event successfully created with ID: " + eventId);
                    myEvents.add(event);
                    onCreated.onCreated(eventId);
                },
                e -> Log.e("Organizer", "Failed to create event: " + e.getMessage())
        );
    }
}