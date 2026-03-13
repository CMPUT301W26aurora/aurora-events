package com.example.auroraevents.model;

import android.util.Log;

import com.example.auroraevents.server.UserDb;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
    private Integer databaseTimeout = 10;
    private TimeUnit timeoutUnit = TimeUnit.SECONDS;


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
    public boolean pull() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<User> user = new AtomicReference<>(null);
        UserDb.getInstance().getUser(deviceId,
                user::set,
                e -> {
                    latch.countDown();
                }
        );

        try {
            if (!latch.await(databaseTimeout, timeoutUnit)) {
                Log.w("RegistrationList", "user update timed out");
                return false;
            }
        } catch (InterruptedException e) {
            Log.w("RegistrationList", "user update interrupted");
            return false;
        }

        if (user.get() == null) {
            return false;
        } else {
            setName(user.get().getName());
            setEmail(user.get().getEmail());
            setPhoneNumber(user.get().getPhoneNumber());
            setRole(user.get().getRole());
            return true;
        }
    }

    public boolean push() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Boolean> status = new AtomicReference<>(true);
        UserDb.getInstance().updateUser(this,
                latch::countDown,
                e -> {
                    status.set(false);
                    latch.countDown();
                }
        );

        try {
            if (!latch.await(databaseTimeout, timeoutUnit)) {
                Log.w("User", "user update timed out");
                return false;
            }
        } catch (InterruptedException e) {
            Log.w("User", "user update interrupted");
            return false;
        }

        return status.get();
    }

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

}
