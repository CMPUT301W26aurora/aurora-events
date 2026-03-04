package com.example.auroraevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class GetWaitListTest {
    private Organizer organizer;

    @Test
    public void getWaitListTest() {
        // Initialize objects
        Organizer organizer = new Organizer();
        ArrayList<Event> myEvents = new ArrayList<>();
        Event myEvent = new Event();
        myEvents.add(myEvent);
        organizer.setMyEvents(myEvents);

        // Test that the waitlist is empty
        assertEquals(0, organizer.getEventWaitList(myEvent).size());

        // Add a user to waitlist
        User user = new User();
        user.setDeviceId("TestID");
        List<String> waitlist = new ArrayList<String>();
        waitlist.add(user.getDeviceId());
        myEvent.setWaitingList(waitlist);

        // Test that the waitlist is of length 1
        assertEquals(1, organizer.getEventWaitList(myEvent).size());
        // Test that the waitlist returns the one user in waitlist
        assertEquals(organizer.getEventWaitList(myEvent).get(0), user.getDeviceId());

        // Clear all waitlist
        myEvent.setWaitingList(new ArrayList<String>());

        // Test that the waitlist was updated by checking its length
        assertEquals(0, organizer.getEventWaitList(myEvent).size());
    }

    @Test
    public void getWaitListExceptionTest() {
        // Initialize objects
        Organizer organizer = new Organizer();
        ArrayList<Event> myEvents = new ArrayList<>();
        Event myEvent = new Event();
        myEvents.add(myEvent);
        organizer.setMyEvents(myEvents);

        // Test that the organizer cannot access event that they did not create
        assertThrows(IllegalArgumentException.class, () -> {
            organizer.getEventWaitList(new Event());
        });
    }

}
