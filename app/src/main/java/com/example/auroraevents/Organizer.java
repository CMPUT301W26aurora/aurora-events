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
     * @param amount
     * The amount of users the organizer wants to sample
     */
    public void sampleWaitList(Event event, int amount) {
        int emptySlots = event.getEmptySlotAmount();
        int waitListSize = event.getWaitingList().size();
        if (!(myEvents.contains(event))) { // Check if the organizer created the specified event
            throw new IllegalArgumentException("Event not found");
        }
        // Validate the amount input
        else if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be bigger than than 0");
        }
        else if (amount > emptySlots) {
            throw new IllegalArgumentException("Amount too big: Not enough empty slots for selection.");
        }
        else if (amount > waitListSize) {
            throw new IllegalArgumentException("Amount too big: Not enough users in the waiting list.");
        }
        // Inputs validated, randomly sample the waitlist in the event
        else {
            event.randomSampling(amount);
        }
    }

    /**
     * Gets the list of users that are in the waiting list of the specified event
     * @param event
     * The event the organizer wants to get the waiting list of
     * @return
     * Return the waiting list of users in the specified event
     */

    public List<String> getEventWaitList(Event event) {
        if (myEvents.contains(event)) { // Check if the organizer created the specified event
            return event.getWaitingList();
        }
        else { // Cannot get waitlist of an event that the organizer did not create
            throw new IllegalArgumentException("Event not found");
        }
    }
}
