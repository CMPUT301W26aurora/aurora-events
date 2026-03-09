package com.example.auroraevents;
import android.os.Build;

import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.LocalTime;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import android.util.Log;

/**
 * This class contains the constructor and methods for the Organizer class, a subclass of the User class
 */
public class Organizer extends User {


    /**
     * Organizer constructor
     */
    public Organizer() {
        super();
        setRole(User.ROLE_ORGANIZER);
    }

    /**
     * This function
     * @param title
     *      The title of the event
     * @param date
     *      The date of the event, format: yyyy-MM-dd
     * @param time
     *      The time of the event, format: yyyy-MM-dd HH:mm:ss
     * @param startTime
     *      The start of the registration period, format: yyyy-MM-dd HH:mm:ss
     * @param endTime
     *      The end of the registration period, format: yyyy-MM-dd HH:mm:ss
     * @param description
     *      The event description
     * @param location
     *      The event location
     * @param capacity
     *      The event capacity
     */
    public void CreateEvent(String organizerDeviceId, String title, String description, String date,
                            String time, String startTime, String endTime, String location, int capacity) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDate eventDate = LocalDate.parse(date);
        LocalDateTime eventRegistrationStart = LocalDateTime.parse(startTime, formatter);
        LocalDateTime eventRegistrationEnd = LocalDateTime.parse(endTime, formatter);

        LocalTime parsedTime = LocalTime.parse(time); // Assume HH:mm:ss
        LocalDateTime eventDateTime = parsedTime.atDate(eventDate);

        Event event = new Event(organizerDeviceId, title, description,
                eventDate, eventRegistrationStart,
                eventRegistrationEnd, location, capacity);

        EventDb.addEvent(event, eventId -> {
            Log.d("Organizer", "Event successfully created with ID: " + eventId);
            },
                e -> {
            Log.e("Organizer", "Failed to create event" + e.getMessage());
        });
    }
}
