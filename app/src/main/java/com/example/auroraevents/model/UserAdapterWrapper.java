package com.example.auroraevents.model;

import java.util.Date;

/**
 * wrapper class for User Adapter
 */
public class UserAdapterWrapper {
    private User user;
    private String status;
    private Date date;
    private String events;
    private String owned;
    public UserAdapterWrapper(User user, String status, Date date) {
        this.user = user;
        this.status = status;
        this.date = date;
    }
    public String getStatus() { return status; }
    public User getUser() { return user; }
    public Date getDate() {return date;}
}
