package com.example.auroraevents;

import java.util.ArrayList;
import java.util.List;

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


    // Notification history (stored as notification IDs or message strings)
    private List<String> notificationHistory;

    // Tags associated with this user
    private List<String> tags;


    /** Required no-arg constructor for Firestore deserialization */
    public User() {
        notificationHistory  = new ArrayList<>();
        tags                 = new ArrayList<>();
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

    public List<String> getNotificationHistory()                                   { return notificationHistory; }
    public void         setNotificationHistory(List<String> notificationHistory)   { this.notificationHistory = notificationHistory; }

    public List<String> getTags()                      { return tags; }
    public void         setTags(List<String> tags)     { this.tags = tags; }

}
