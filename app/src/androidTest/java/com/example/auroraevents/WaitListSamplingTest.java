package com.example.auroraevents;

import static com.example.auroraevents.TestsSupport.setUpEvent;
import static com.example.auroraevents.TestsSupport.setUpUser;
import static com.example.auroraevents.TestsSupport.signIn;
import static com.example.auroraevents.TestsSupport.takeDownEvent;
import static com.example.auroraevents.TestsSupport.takeDownUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WaitListSamplingTest {
    private Organizer organizer;

    Event myEvent;
    RegistrationList list;
    String entrantID;

    User user1;
    User user2;
    User user3;
    User user4;

    @BeforeClass
    public static void prepare() {
        signIn();
        RegistrationWaitingListTest.prepare();
    }

    @Before
    public void before() {
        myEvent = new Event(
                "test device",
                "wait list sampling test",
                "event for wait list sampling test",
                new Date(),
                "testing environment",
                3);
        myEvent.setEventId("test event");
        setUpEvent(myEvent, 10, TimeUnit.SECONDS);
        list = myEvent.registrationList;
        entrantID = "aurora";
        user1 = new User("TestID1","user1","email1","phone1","waitListSamplingTest");
        setUpUser(user1,10, TimeUnit.SECONDS);
        user2 = new User("TestID2","user2","email2","phone2","waitListSamplingTest");
        setUpUser(user2,10, TimeUnit.SECONDS);
        user3 = new User("TestID3","user3","email3","phone3","waitListSamplingTest");
        setUpUser(user3,10, TimeUnit.SECONDS);
        user4 = new User("TestID4","user4","email4","phone4","waitListSamplingTest");
        setUpUser(user4,10, TimeUnit.SECONDS);
    }

    @After
    public void after() {
        takeDownEvent(myEvent);
        takeDownUser(user1);
        takeDownUser(user2);
        takeDownUser(user3);
        takeDownUser(user4);
    }

    @Test
    public void waitListSampleTest() {
        // Initialize objects
        Organizer organizer = new Organizer();
        ArrayList<Event> myEvents = new ArrayList<>();
        myEvents.add(myEvent);
        organizer.setMyEvents(myEvents);

        // Add multiple users to waitlist
        List<User> waitlist = new ArrayList<User>();
        List<String> waitlistWithID = new ArrayList<String>();
        waitlist.add(user1);
        waitlist.add(user2);
        waitlist.add(user3);
        waitlist.add(user4);
        waitlistWithID.add(user1.getDeviceId());
        waitlistWithID.add(user2.getDeviceId());
        waitlistWithID.add(user3.getDeviceId());
        waitlistWithID.add(user4.getDeviceId());

        myEvent.registrationList.addToWaitingList(user1.getDeviceId());
        myEvent.registrationList.addToWaitingList(user2.getDeviceId());
        myEvent.registrationList.addToWaitingList(user3.getDeviceId());
        myEvent.registrationList.addToWaitingList(user4.getDeviceId());

        // Randomly sample waiting list
        organizer.sampleWaitList(myEvent);
        // Check if empty slots is 0
        assertEquals(0, myEvent.getEmptySlotAmount());
        // Check if the waitlist shrunk to 1
        assertEquals(1, organizer.getEventWaitList(myEvent).size());
        // Check if the selected list increased by 3
        assertEquals(3, myEvent.getListOfUsersWithStatus("selected").size());
        // Check if the users from waitlist were selected
        // Also store current waiting list and the current selected list for the next section
        ArrayList<String> checkList = new ArrayList<String>();
        for (User user: organizer.getEventWaitList(myEvent)) {
            checkList.add(user.getDeviceId());
        }
        for (User user: myEvent.getListOfUsersWithStatus("selected")) {
            checkList.add(user.getDeviceId());
        }
        assertTrue(checkList.containsAll(waitlistWithID));
    }
}
