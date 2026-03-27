package com.example.auroraevents.model;

import android.util.Log;

import com.example.auroraevents.server.EventDb;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a user in the application.
 * The deviceId serves as the Firestore document ID in the "Users" collection.
 */
public class User {

    // Roles
    public static final String ROLE_ENTRANT   = "entrant";
    public static final String ROLE_ORGANIZER = "organizer";
    public static final String ROLE_ADMIN     = "admin";

    private String deviceId;      // Firestore document ID
    private String name;
    private String email;
    private String phoneNumber;
    private String role;
    private Integer databaseTimeout;
    private TimeUnit timeoutUnit;
    private final String TAG = "User";


    // Notification history (stored as notification IDs or message strings)
    private List<String> notificationHistory;

    // Tags associated with this user
    private List<String> tags;

    // Additional information for users in a specific event; these must change dynamically depending on the event
    // Status of this user in a specific event,
    private String status;
    // For cancelled users: reason behind cancellation

    /** Required no-arg constructor for Firestore deserialization */
    public User() {
        notificationHistory  = new ArrayList<>();
        tags                 = new ArrayList<>();
        databaseTimeout = 10;
        timeoutUnit = TimeUnit.SECONDS;
    }

    public User(String deviceId, String name, String email, String phoneNumber, String role) {
        this();
        this.deviceId        = deviceId;
        this.name            = name;
        this.email           = email;
        this.phoneNumber     = phoneNumber;
        this.role            = role;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public String getDeviceId()                        { return deviceId; }
    public void   setDeviceId(String deviceId)         { this.deviceId = deviceId; }

    public String getName()                            { return name; }
    public void   setName(String name)                 { this.name = name; }

    public String getEmail()                           { return email; }
    public void   setEmail(String email)               { this.email = email; }

    public String getPhoneNumber()                     { return phoneNumber; }
    public void   setPhoneNumber(String phoneNumber)   { this.phoneNumber = phoneNumber; }

    public String getRole()                            { return role; }
    public void   setRole(String role)                 { this.role = role; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public List<String> getNotificationHistory()                                   { return notificationHistory; }
    public void         setNotificationHistory(List<String> notificationHistory)   { this.notificationHistory = notificationHistory; }
    public void         loadNotificationHistory()                                  {
        FirebaseFirestore.getInstance()
                .collection("Notifications")
                .whereEqualTo("deviceId", deviceId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    notificationHistory.clear();
                    for (QueryDocumentSnapshot doc : snapshot)
                        notificationHistory.add(doc.getId());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load notifications", e));
    }

    public List<String> getTags()                      { return tags; }
    public void         setTags(List<String> tags)     { this.tags = tags; }

    public Integer getDatabaseTimeout() {
        return databaseTimeout;
    }
    public void setDatabaseTimeout(Integer databaseTimeout) {
        this.databaseTimeout = databaseTimeout;
    }

    public TimeUnit getTimeoutUnit() {
        return timeoutUnit;
    }
    public void setTimeoutUnit(TimeUnit unit) {
        timeoutUnit = unit;
    }

    /**
     * Deletes the user
     * @author Jared Strandlund
     * @return
     *     {@code 0} when successfully deleted
     *     {@code 1} when code error
     *     {@code 2} when database error
     */
    public int deleteUser() {
        AtomicInteger status = new AtomicInteger(0);

        // Delete from events
        for (String listType : EventDb.ALL_LISTS)
            EventDb.getInstance().getEventsForUser(deviceId, listType,
                    events -> {
                        for (Event event : events) {
                            Log.d(TAG, "Deleting user " + deviceId);
                            event.registrationList.setEventId(event.getEventId());
                            event.registrationList.removeFromAllLists(deviceId);
                        }
                        status.set(0);
                    },
                    e -> status.set(1)
            );

        // Delete notifications
        for (String notification : notificationHistory)
            if (status.get() <= 0)
                FirebaseFirestore.getInstance()
                        .collection("Notifications")
                        .document(notification)
                        .delete()
                        .addOnSuccessListener(unused -> {
                            Log.d(TAG, "Notification deleted: " + notification);
                            status.set(0);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to delete notification " + notification, e);
                            status.set(1);
                        });

        // Remove properties
        if (status.get() <= 0) {
            name = "";
            email = "";
            phoneNumber = "";
            role = ROLE_ENTRANT;
            databaseTimeout = 10;
            timeoutUnit = TimeUnit.SECONDS;
            notificationHistory = new ArrayList<>();
            tags = new ArrayList<>();
            status.set(0);
        }

        return status.get();
    }

}
