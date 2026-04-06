package com.example.auroraevents.model;

import android.util.Log;

import com.example.auroraevents.server.EventDb;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private String deviceId;      // Firestore document ID
    private String name;
    private String email;
    private String phoneNumber;
    private String role;
    private final String TAG = "User";
    private Boolean isAdmin;
    private Map<String, String> eventsSigned;
    @ServerTimestamp
    private Timestamp accountAge;
    // Notification history (stored as notification IDs or message strings)
    private List<String> notificationHistory;

    // Tags associated with this user
    private List<String> tags;
    /** Required no-arg constructor for Firestore deserialization */
    public User() {
        this.name = "";
        this.email = "";
        this.phoneNumber = "";
        this.role = ROLE_ENTRANT;
        this.isAdmin = false;
        this.notificationHistory  = new ArrayList<>();
        this.tags                 = new ArrayList<>();
        this.eventsSigned = new HashMap<>();
    }
    public User(String deviceId, String name, String email, String phoneNumber, String role, Boolean isAdmin) {
        this();
        this.deviceId        = deviceId;
        this.name            = name;
        this.email           = email;
        this.phoneNumber     = phoneNumber;
        this.role            = role;
        this.isAdmin = isAdmin;
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
    public Boolean getIsAdmin()                          {return isAdmin;}
    public void setIsAdmin(Boolean admin)                {isAdmin = admin;}
    public List<String> getNotificationHistory()                                   { return notificationHistory; }
    public void         setNotificationHistory(List<String> notificationHistory)   { this.notificationHistory = notificationHistory; }

    public List<String> getTags()                      { return tags; }
    public void         setTags(List<String> tags)     { this.tags = tags; }
    public Timestamp getAccountAge(){return accountAge;}
    public void setAccountAge(Timestamp accountAge){this.accountAge=accountAge;}
    public Map<String, String> getEventsSigned() {
        return eventsSigned != null ? eventsSigned : new HashMap<>();
    }
    public void setEventsSigned (Map<String,String> eventsSigned) {this.eventsSigned=eventsSigned;}
    @Exclude
    public String getFormattedAccountAge() {
        if (accountAge == null) return "Unknown";
        long diffInMs = System.currentTimeMillis() - accountAge.toDate().getTime();
        long days = TimeUnit.MILLISECONDS.toDays(diffInMs);

        if (days < 1) return "Joined Today";
        return days + " days ago";
    }
}
