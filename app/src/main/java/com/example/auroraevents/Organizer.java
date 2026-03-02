package com.example.auroraevents;

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

    public void sampleWaitList(Event event, int amount) {
        int emptySlots = event.getEmptySlotAmount();
        int waitListSize = event.getWaitingList().size();
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be bigger than than 0");
        }
        else if (amount > emptySlots) {
            throw new IllegalArgumentException("Amount too big: Not enough empty slots for selection.");
        }
        else if (amount > waitListSize) {
            throw new IllegalArgumentException("Amount too big: Not enough users in the waiting list.");
        }
        else {
            event.randomSampling(amount);
        }
    }

    public List<String> getEventWaitList(Event event) {
        if (myEvents.contains(event)) {
            return event.getWaitingList();
        }
        else {
            throw new IllegalArgumentException("Event not found");
        }
    }
}
