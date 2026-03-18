package com.example.auroraevents;

import static com.example.auroraevents.TestsSupport.setUpEvent;
import static com.example.auroraevents.TestsSupport.setUpUser;
import static com.example.auroraevents.TestsSupport.signIn;
import static com.example.auroraevents.TestsSupport.takeDownEvent;
import static com.example.auroraevents.TestsSupport.takeDownUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.Organizer;
import com.example.auroraevents.model.User;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WaitListSamplingTest {
    private Organizer organizer;

    Event myEvent;
    Event myEvent2;
    Event myEvent3;
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
                "event for wait list sampling test\"",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                "testing environment",
                3);
        myEvent.setEventId("test event");
        setUpEvent(myEvent, 10, TimeUnit.SECONDS);

        myEvent2 = new Event(
                "test device 2",
                "wait list sampling test (Redo random sampling to test randomness)",
                "event for testing if random sampling is actually random",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                "testing environment",
                3);
        myEvent2.setEventId("test event");
        setUpEvent(myEvent2, 10, TimeUnit.SECONDS);

        myEvent3 = new Event(
                "test device 3",
                "wait list sampling test (waitlist less than empty slots)",
                "event for testing if waitlist simply selects everyone if there are more empty slots",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                "testing environment",
                10);
        myEvent3.setEventId("test event");
        setUpEvent(myEvent3, 10, TimeUnit.SECONDS);

        // These 4 users will be used for the tests
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
        takeDownEvent(myEvent2);
        takeDownEvent(myEvent3);
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
        myEvents.add(myEvent2);
        organizer.setMyEvents(myEvents);

        // Add multiple users to waitlist
        List<String> waitlistWithID = new ArrayList<String>();
        waitlistWithID.add(user1.getDeviceId());
        waitlistWithID.add(user2.getDeviceId());
        waitlistWithID.add(user3.getDeviceId());
        waitlistWithID.add(user4.getDeviceId());

        myEvent.registrationList.addAllToWaitingList(waitlistWithID);
        // Randomly sample waiting list
        organizer.sampleWaitList(myEvent);
        // Check if empty slots is 0
        assertEquals(0, myEvent.getEmptySlotAmount());
        // Check if the waitlist shrunk to 1
        assertEquals(1, organizer.getEventWaitList(myEvent).size());
        // Check if the selected list increased by 3
        assertEquals(3, myEvent.getSelectedListOfUsers().size());
        // Check if the users from waitlist were selected
        // Also store current waiting list and the current selected list for the next section
        List<String> checkList = new ArrayList<String>();
        List<String> previousWaitListWithID;
        List<String> previousSelectedListWithID;
        for (User user: organizer.getEventWaitList(myEvent)) {
            checkList.add(user.getDeviceId());
        }
        previousWaitListWithID = myEvent.registrationList.getWaitingList();
        for (User user: myEvent.getSelectedListOfUsers()) {
            checkList.add(user.getDeviceId());
        }
        previousSelectedListWithID = myEvent.registrationList.getSelectedList();
        assertTrue(checkList.containsAll(waitlistWithID));

        // Redo the same test but with another event
        // Test if another event with the same users selects some other users
        myEvent2.registrationList.addAllToWaitingList(waitlistWithID);
        organizer.sampleWaitList(myEvent2);

        assertFalse(previousWaitListWithID.containsAll(myEvent2.registrationList.getWaitingList()));
        assertFalse(previousSelectedListWithID.containsAll(myEvent2.registrationList.getSelectedList()));
    }
    @Test
    public void waitListSelectAllTest() {
        // This is when the there are more empty slots than the amount of users in waiting list
        // Initialize objects
        Organizer organizer = new Organizer();
        ArrayList<Event> myEvents = new ArrayList<>();
        myEvents.add(myEvent3);
        organizer.setMyEvents(myEvents);

        // Add all 4 users to waiting list
        List<String> waitlistWithID = new ArrayList<String>();
        waitlistWithID.add(user1.getDeviceId());
        waitlistWithID.add(user2.getDeviceId());
        waitlistWithID.add(user3.getDeviceId());
        waitlistWithID.add(user4.getDeviceId());

        myEvent3.registrationList.addAllToWaitingList(waitlistWithID);

        organizer.sampleWaitList(myEvent3);

        // Check that there are still 6 more empty slots
        assertEquals(6, myEvent3.getEmptySlotAmount());
        // Check the amounts for waiting list and selected list
        assertEquals(0, myEvent3.registrationList.getWaitingList().size());
        assertEquals(4, myEvent3.registrationList.getSelectedList().size());
        // Check that all users from waiting list were selected
        assertTrue(waitlistWithID.containsAll(myEvent3.registrationList.getSelectedList()));
    }
}
