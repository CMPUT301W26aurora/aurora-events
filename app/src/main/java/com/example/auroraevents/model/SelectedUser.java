package com.example.auroraevents.model;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;
/**
 * wrapper for selected users
 */
public class SelectedUser {
    private String userId;
    @ServerTimestamp
    private Timestamp selectedAt;
    // Required for Firestore
    public SelectedUser() {}
    public SelectedUser(String userId, Timestamp selectedAt) {
        this.userId = userId;
        this.selectedAt = selectedAt;
    }
    public String getUserId() { return userId; }
    public Timestamp getSelectedAt() { return selectedAt; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setSelectedAt(Timestamp selectedAt) { this.selectedAt = selectedAt; }
}
