package com.example.auroraevents;

import static com.example.auroraevents.RegistrationListTestsSupport.setUpEvent;
import static com.example.auroraevents.RegistrationListTestsSupport.signIn;
import static com.example.auroraevents.RegistrationListTestsSupport.takeDownEvent;
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

public class WaitListSamplingTest {
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
    public void waitListSampleTest() {
        // Initialize objects
        Organizer organizer = new Organizer();
        ArrayList<Event> myEvents = new ArrayList<>();
        myEvent.setCapacity(4);
        myEvents.add(myEvent);
        organizer.setMyEvents(myEvents);

        // Add multiple users to waitlist
        List<String> waitlist = new ArrayList<String>();
        User user = new User();
        user.setDeviceId("TestID");
        User user2 = new User();
        user2.setDeviceId("TestID2");
        User user3 = new User();
        user3.setDeviceId("TestID3");
        User user4 = new User();
        user4.setDeviceId("TestID4");
        waitlist.add(user.getDeviceId());
        waitlist.add(user2.getDeviceId());
        waitlist.add(user3.getDeviceId());
        waitlist.add(user4.getDeviceId());
        myEvent.registrationList.addAllToAttendingList(waitlist);

        // Randomly select 2
        myEvent.randomSampling();
        // Check if the waitlist shrunk by 2
        assertEquals(2, myEvent.registrationList.getWaitingList().size());
        // Check if the selected list increased by 2
        assertEquals(2, myEvent.registrationList.getSelectedList().size());
        // Check if the selected list and wait list combined still has the original four users
        // Also store current waitlist and selected list for the next section
        List<String> checkList = new ArrayList<String>();
        List<String> previousWaitList = new ArrayList<String>();
        List<String> previousSelectedList = new ArrayList<String>();

        for (String deviceId : myEvent.registrationList.getWaitingList()) {
            checkList.add(deviceId);
            previousWaitList.add(deviceId);
        }
        for (String deviceId : myEvent.registrationList.getSelectedList()) {
            checkList.add(deviceId);
            previousSelectedList.add(deviceId);
        }
        assertTrue(checkList.containsAll(waitlist));

        // Revert selection and try sampling again to see if it selected different users
        // Note, due to the random nature of the sampling function, this test may not always succeed
        waitlist = new ArrayList<String>();
        waitlist.add(user.getDeviceId());
        waitlist.add(user2.getDeviceId());
        waitlist.add(user3.getDeviceId());
        waitlist.add(user4.getDeviceId());
        myEvent.registrationList.addAllToAttendingList(waitlist);
        organizer.sampleWaitList(myEvent);
        assertFalse(previousWaitList.containsAll(myEvent.registrationList.getWaitingList()));
        assertFalse(previousSelectedList.containsAll(myEvent.registrationList.getSelectedList()));
    }
}
