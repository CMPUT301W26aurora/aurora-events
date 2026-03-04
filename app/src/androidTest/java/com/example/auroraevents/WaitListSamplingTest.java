package com.example.auroraevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class WaitListSamplingTest {
    private Organizer organizer;

    @Test
    public void waitListSampleTest() {
        // Initialize objects
        Organizer organizer = new Organizer();
        ArrayList<Event> myEvents = new ArrayList<>();
        Event myEvent = new Event();
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
        myEvent.setWaitingList(waitlist);

        // Randomly select 2
        myEvent.randomSampling(2);
        // Check if the waitlist shrunk by 2
        assertEquals(2, myEvent.getWaitingList().size());
        // Check if the selected list increased by 2
        assertEquals(2, myEvent.getSelectedList().size());
        // Check if the selected list and wait list combined still has the original four users
        // Also store current waitlist and selected list for the next section
        List<String> checkList = new ArrayList<String>();
        List<String> previousWaitList = new ArrayList<String>();
        List<String> previousSelectedList = new ArrayList<String>();

        for (String deviceId : myEvent.getWaitingList()) {
            checkList.add(deviceId);
            previousWaitList.add(deviceId);
        }
        for (String deviceId : myEvent.getSelectedList()) {
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
        myEvent.setWaitingList(waitlist);
        organizer.sampleWaitList(myEvent,2);
        assertFalse(previousWaitList.containsAll(myEvent.getWaitingList()));
        assertFalse(previousSelectedList.containsAll(myEvent.getSelectedList()));
    }
}
