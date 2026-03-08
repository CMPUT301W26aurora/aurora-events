package com.example.auroraevents;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Organizer extends User {
    private ArrayList<Event> myEvents;

    public Organizer() {
        myEvents = new ArrayList<Event>();
    }

    public ArrayList<Event> getMyEvents() {
        return myEvents;
    }

    public void setMyEvents(ArrayList<Event> myEvents) {
        this.myEvents = myEvents;
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
        ArrayList<User> userInfoList = new ArrayList<User>();
        List<String> waitList = event.registrationList.getWaitingList();

        if (myEvents.contains(event)) {
            //Fetch user information from Firestore database
            var ref = new Object() {
                User returnedUser;
            };

            for (String userId : waitList) {
                UserDb.getInstance().getUser(userId,
                        user -> {
                            ref.returnedUser = user;
                        },
                        e -> {
                            Log.e("Main", "Error fetching user", e);
                        }
                );
                if (ref.returnedUser != null) {
                    userInfoList.add(ref.returnedUser);
                }
            }
        }
        else { // Cannot get waitlist of an event that the organizer did not create
            throw new IllegalArgumentException("Event not found");
        }
        return userInfoList;
    }
}
