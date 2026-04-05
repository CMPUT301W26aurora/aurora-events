package com.example.auroraevents.model;

import java.util.Date;
import java.util.Objects;

/**
 * wrapper for selected users
 */
public class SelectedUser {
    private String userId;
    private Date selectedAt;

    // Required for Firestore
    public SelectedUser() {}

    public SelectedUser(String userId, Date selectedAt) {
        this.userId = userId;
        this.selectedAt = selectedAt;
    }

    public String getUserId() { return userId; }
    public Date getSelectedAt() { return selectedAt; }



}
