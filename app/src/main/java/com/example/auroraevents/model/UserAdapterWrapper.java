package com.example.auroraevents.model;

/**
 * wrapper class for User Adapter
 */
public class UserAdapterWrapper {
    private User user;
    private String status;
    public UserAdapterWrapper(User user, String status) {
        this.user = user;
        this.status = status;
    }
    public String getStatus() { return status; }
    public User getUser() { return user; }
}
