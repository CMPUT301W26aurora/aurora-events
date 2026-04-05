package com.example.auroraevents.model;

import com.google.firebase.Timestamp;

import java.util.Date;

/**
 * wrapper for selected users
 */
public class SelectedUser {
    private String userId;
    private Timestamp selectedAt;

    // Required for Firestore
    public SelectedUser() {}

    public SelectedUser(String userId, Timestamp selectedAt) {
        this.userId = userId;
        this.selectedAt = selectedAt;
    }

    public String getUserId() { return userId; }
    public Timestamp getSelectedAt() { return selectedAt; }



}
