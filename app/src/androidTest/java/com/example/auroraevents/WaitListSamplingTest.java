package com.example.auroraevents;

import static com.example.auroraevents.TestsSupport.setUpEvent;
import static com.example.auroraevents.TestsSupport.setUpUser;
import static com.example.auroraevents.TestsSupport.signIn;
import static com.example.auroraevents.TestsSupport.takeDownEvent;
import static com.example.auroraevents.TestsSupport.takeDownUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.util.Log;

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

    User user;
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
                "registration test",
                "event for registration test",
                new Date(),
                "testing environment",
                0);
        myEvent.setEventId("test event");
        setUpEvent(myEvent, 60, TimeUnit.SECONDS);
        list = myEvent.registrationList;
        entrantID = "aurora";
        user = new User();
        user.setDeviceId("TestID");
        setUpUser(user,60, TimeUnit.SECONDS);
        user2 = new User();
        user2.setDeviceId("TestID2");
        setUpUser(user2,60, TimeUnit.SECONDS);
        user3 = new User();
        user3.setDeviceId("TestID3");
        setUpUser(user3,60, TimeUnit.SECONDS);
        user4 = new User();
        user4.setDeviceId("TestID4");
        setUpUser(user4,60, TimeUnit.SECONDS);
    }

    @After
    public void after() {
        takeDownEvent(myEvent);
        takeDownUser(user);
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
        List<String> waitlist = new ArrayList<String>();
        List<User> waitListWithID = new ArrayList<User>();

        waitlist.add(user.getDeviceId());
        waitlist.add(user2.getDeviceId());
        waitlist.add(user3.getDeviceId());
        waitlist.add(user4.getDeviceId());
        waitListWithID.add(user);
        waitListWithID.add(user2);
        waitListWithID.add(user3);
        waitListWithID.add(user4);
        myEvent.registrationList.addAllToAttendingList(waitlist);

        // Randomly select 2
        myEvent.randomSampling();
        // Check if the waitlist shrunk by 2
        assertEquals(2, organizer.getEventWaitList(myEvent).size());
        // Check if the selected list increased by 2
        assertEquals(2, myEvent.registrationList.getSelectedList().size());
        // Check if the users from waitlist were selected
        // Also store current waitlist the next section
        ArrayList<User> checkList = new ArrayList<User>();
        ArrayList<User> previousWaitList = new ArrayList<User>();
        for (User user: organizer.getEventWaitList(myEvent)) {
            checkList.add(user);
            previousWaitList.add(user);
        }
        assertTrue(checkList.containsAll(waitListWithID));

        // Revert selection and try sampling again to see if it selected different users
        // Note, due to the random nature of the sampling function, this test may have a small chance of not passing
        myEvent.registrationList.addToRemovedList(user.getDeviceId());
        myEvent.registrationList.addToRemovedList(user2.getDeviceId());
        myEvent.registrationList.addToRemovedList(user3.getDeviceId());
        myEvent.registrationList.addToRemovedList(user4.getDeviceId());
        myEvent.registrationList.addAllToAttendingList(waitlist);
        organizer.sampleWaitList(myEvent);
        // The previous waitlist should be different from the current waitlist
        assertFalse(previousWaitList.containsAll(organizer.getEventWaitList(myEvent)));
        // Check if the selected list increased by 2
        assertEquals(2, myEvent.registrationList.getSelectedList().size());
    }
}
