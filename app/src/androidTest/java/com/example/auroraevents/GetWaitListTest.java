package com.example.auroraevents;

import static com.example.auroraevents.RegistrationListTestsSupport.setUpEvent;
import static com.example.auroraevents.RegistrationListTestsSupport.signIn;
import static com.example.auroraevents.RegistrationListTestsSupport.takeDownEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GetWaitListTest {
    private Organizer organizer;

    Event myEvent;
    RegistrationList list;
    String entrantID;

    User user;

    @BeforeClass
    public static void prepare() {
        signIn();
    }

    @Before
    public void before() {
        myEvent = new Event(
                "test device",
                "registration test",
                "event for registration test",
                new Date(),
                "testing environment",
                0);
        myEvent.setEventId("test event");
        setUpEvent(myEvent);
        list = myEvent.registrationList;
        entrantID = "aurora";
        user = new User();
        user.setDeviceId("TestID");
        UserDb.getInstance().addUser(user,
                () -> {
                    Log.d("Main", "Successfully added user");
                },
                e -> {
                    Log.e("Main", "Error while adding user", e);
                });
    }

    @After
    public void after() {
        takeDownEvent(myEvent);
        UserDb.getInstance().deleteUser(user.getDeviceId(),
                () -> {
                    Log.d("Main", "Successfully deleted user");
                },
                e -> {
                    Log.e("Main", "Error while deleting user", e);
                });
    }

    @Test
    public void getWaitListTest() {
        // Initialize objects
        Organizer organizer = new Organizer();
        ArrayList<Event> myEvents = new ArrayList<>();
        myEvents.add(myEvent);
        organizer.setMyEvents(myEvents);

        // Test that the waitlist is empty
        assertEquals(0, organizer.getEventWaitList(myEvent).size());

        // Add a user to waitlist
        myEvent.registrationList.addToWaitingList(user.getDeviceId());

        // Test that the waitlist is of length 1
        assertEquals(1, organizer.getEventWaitList(myEvent).size());
        // Test that the waitlist returns the one user in waitlist
        assertEquals(organizer.getEventWaitList(myEvent).get(0), user.getDeviceId());

        // Clear all waitlist
        myEvent.registrationList.addAllToRemovedList(myEvent.registrationList.getWaitingList());

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
