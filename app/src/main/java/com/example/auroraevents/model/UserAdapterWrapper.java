package com.example.auroraevents.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * wrapper class for User Adapter
 * Allows the user Adapter to accept multiple fields without
 * crowding the function print
 * @author Sean Ross
 */
public class UserAdapterWrapper {
    private User user;
    private String status;
    private Date date;
    private Map<String,String> eventDataList;
    private List<String> ids;
    private Map<String, String> lookup;
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
