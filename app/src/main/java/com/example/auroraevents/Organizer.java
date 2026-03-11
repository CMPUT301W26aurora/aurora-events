package com.example.auroraevents;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import android.util.Log;
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
     * @param date               The date of the event, format: yyyy-MM-dd
     * @param time               The time of the event, format: HH:mm:ss
     * @param startTime          The start of the registration period, format: yyyy-MM-dd HH:mm:ss
     * @param endTime            The end of the registration period, format: yyyy-MM-dd HH:mm:ss
     * @param location           The event location
     * @param capacity           The event capacity
     */
    public void CreateEvent(String organizerDeviceId, String title, String description, String date,
                            String time, String startTime, String endTime, String location, int capacity) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Parse from formatter
        LocalDate eventDate = LocalDate.parse(date);
        LocalTime parsedTime = LocalTime.parse(time);
        LocalDateTime eventDateTime = parsedTime.atDate(eventDate);

        LocalDateTime eventRegistrationStart = LocalDateTime.parse(startTime, formatter);
        LocalDateTime eventRegistrationEnd   = LocalDateTime.parse(endTime, formatter);

        // Create event from parameters
        Event event = new Event(organizerDeviceId, title, description,
                eventDateTime,
                eventRegistrationStart,
                eventRegistrationEnd,
                location, capacity);

        // Add event
        EventDb.addEvent(event,
                eventId -> Log.d("Organizer", "Event successfully created with ID: " + eventId),
                e      -> Log.e("Organizer", "Failed to create event: " + e.getMessage())
        );
    }

    /**
     * Randomly samples users in the waiting list of the specified event
     * @param event
     * Event that the organizer wants to sample in
     */
    public void sampleWaitList(Event event) {
        if (!(myEvents.contains(event))) { // Check if the organizer created the specified event
            throw new IllegalArgumentException("Event not found");
        }
        else {
            event.randomSampling();
        }
    }

    /**
     * Gets the list of users that are in the waiting list of the specified event
     * @param event
     * The event the organizer wants to get the waiting list of
     * @return
     * Return the waiting list of users in the specified event
     */
    public ArrayList<User> getEventWaitList(Event event) {
        if (myEvents.contains(event)) {
            //Return list of users in that are in the waiting list
            return event.getWaitingListOfUsers();
        }
        else { // Cannot get waitlist of an event that the organizer did not create
            throw new IllegalArgumentException("Event not found");
        }
    }
}
