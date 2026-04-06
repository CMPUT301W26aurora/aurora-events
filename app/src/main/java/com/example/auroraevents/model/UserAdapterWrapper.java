package com.example.auroraevents.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * wrapper class for User Adapter
 */
public class UserAdapterWrapper {
    private User user;
    private String status;
    private Date date;
    private Map<String,String> eventDataList;
    private List<String> ids;
    private Map<String, String> lookup;
    //you know I never thought I would be here, but here I am
    //its like the world actually hates me, this database will be the end of me
    //for some reason, the event object is stored inside of the organizer class
    //meaning every organizer has a stale copy of their made events in them
    //its a data bomb waiting to be activated, and not only that, this user story has
    //crazy data requirements, I need to saturate the users with a list of the ids of events
    //mapped with a status, which I auto update in the node.js. Then for each user in the adapter I pull this list
    //bro I think im going to lose it, why me LOL. All this to display a tiny piece of text on a recycler view, im going
    //to lose it. So there I was, dude, sitting at the table, and at that point I realized, store the eventId as a key
    //and store the status as a value-pair. Why didn't we do this for the events as well!!!
    //inside of the event, have a map called registrationlist or some other stupid shit, the userId is a key, the status
    //of the user is its pair.
    public UserAdapterWrapper(User user, String status, Date date, Map<String, String> eventDataList, List<String> ids, Map<String, String> lookup) {
        this.user = user;
        this.status = status;
        this.date = date;
        this.eventDataList = eventDataList;
        this.ids = ids;
        this.lookup=lookup;

    }
    public String getStatus() { return status; }
    public User getUser() { return user; }
    public Date getDate() {return date;}
    public Map<String, String> getEventDataList() {
        return eventDataList;
    }
    public void setEventDataList(Map<String, String> eventDataList) {
        this.eventDataList = eventDataList;
    }
    public List<String> getIds() {
        return ids;
    }
    public void setIds(List<String> ids) {
        this.ids = ids;
    }
    public Map<String, String> getLookup() {
        return lookup;
    }
}
