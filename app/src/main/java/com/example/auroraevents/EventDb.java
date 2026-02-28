package com.example.auroraevents;


import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.List;

/**
 * Singleton class for all Firestore operations on the "Events" collection.
 *
 * Usage:
 *   EventDb.getInstance().addEvent(event, id -> { ... }, e -> { ... });
 */
public class EventDb {

    private static final String TAG             = "EventDb";
    private static final String COLLECTION_NAME = "Events";

    // Participant list field names — use these constants everywhere
    public static final String LIST_ATTENDING  = "attendingList";
    public static final String LIST_SELECTED   = "selectedList";
    public static final String LIST_WAITING    = "waitingList";
    public static final String LIST_CANCELLED  = "cancelledList";

    private static EventDb instance;
    private final  FirebaseFirestore db;

    // ── Callbacks ──────────────────────────────────────────────────────────

    public interface OnSuccessCallback       { void onSuccess(); }
    public interface OnFailureCallback       { void onFailure(Exception e); }
    public interface OnEventCreatedCallback  { void onCreated(String eventId); }
    public interface OnEventFetchedCallback  { void onFetched(Event event); }
    public interface OnEventListFetchedCallback { void onFetched(List<Event> events); }

    // ── Singleton ──────────────────────────────────────────────────────────

    private EventDb() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized EventDb getInstance() {
        if (instance == null) {
            instance = new EventDb();
        }
        return instance;
    }

    // ── CREATE ─────────────────────────────────────────────────────────────

    /**
     * Adds a new event document to Firestore with an auto-generated ID.
     * After creation, the generated ID is written back into event.eventId.
     *
     * @param event     The Event object to persist.
     * @param onCreated Called with the new auto-generated document ID.
     * @param onFailure Called with the exception if the write fails.
     */
    public void addEvent(Event event, OnEventCreatedCallback onCreated, OnFailureCallback onFailure) {
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();

        // Write back the ID so the caller's object is up-to-date
        event.setEventId(docRef.getId());

        docRef.set(event)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Event created: " + docRef.getId());
                    onCreated.onCreated(docRef.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create event", e);
                    onFailure.onFailure(e);
                });
    }

    // ── READ ───────────────────────────────────────────────────────────────

    /**
     * Fetches a single event by its Firestore document ID.
     *
     * @param eventId   The document ID to fetch.
     * @param onFetched Called with the Event object, or null if not found.
     * @param onFailure Called with the exception if the read fails.
     */
    public void getEvent(String eventId, OnEventFetchedCallback onFetched, OnFailureCallback onFailure) {
        db.collection(COLLECTION_NAME)
                .document(eventId)
                .get()
                .addOnSuccessListener((DocumentSnapshot snapshot) -> {
                    if (snapshot.exists()) {
                        Event event = snapshot.toObject(Event.class);
                        onFetched.onFetched(event);
                    } else {
                        Log.d(TAG, "No event found for id: " + eventId);
                        onFetched.onFetched(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch event: " + eventId, e);
                    onFailure.onFailure(e);
                });
    }

    /**
     * Fetches all events in the collection.
     *
     * @param onFetched Called with the full list of Event objects.
     * @param onFailure Called with the exception if the read fails.
     */
    public void getAllEvents(OnEventListFetchedCallback onFetched, OnFailureCallback onFailure) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> events = querySnapshot.toObjects(Event.class);
                    onFetched.onFetched(events);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch all events", e);
                    onFailure.onFailure(e);
                });
    }

    /**
     * Fetches all events created by a specific organizer.
     *
     * @param organizerDeviceId The organizer's device ID to filter by.
     * @param onFetched         Called with the matching Event list.
     * @param onFailure         Called with the exception if the read fails.
     */
    public void getEventsByOrganizer(String organizerDeviceId,
                                     OnEventListFetchedCallback onFetched,
                                     OnFailureCallback onFailure) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("organizerDeviceId", organizerDeviceId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> events = querySnapshot.toObjects(Event.class);
                    onFetched.onFetched(events);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch events for organizer: " + organizerDeviceId, e);
                    onFailure.onFailure(e);
                });
    }

    /**
     * Fetches all events where the given user appears in a specific participant list.
     * Use the LIST_* constants for the fieldName.
     *
     * Example — get all events a user is waiting on:
     *   EventDb.getInstance().getEventsForUser(deviceId, EventDb.LIST_WAITING, ...);
     *
     * @param deviceId  The user's device ID to search for.
     * @param fieldName One of LIST_ATTENDING, LIST_SELECTED, LIST_WAITING, LIST_CANCELLED.
     * @param onFetched Called with the matching Event list.
     * @param onFailure Called with the exception if the read fails.
     */
    public void getEventsForUser(String deviceId, String fieldName,
                                 OnEventListFetchedCallback onFetched,
                                 OnFailureCallback onFailure) {
        db.collection(COLLECTION_NAME)
                .whereArrayContains(fieldName, deviceId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> events = querySnapshot.toObjects(Event.class);
                    onFetched.onFetched(events);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch events for user: " + deviceId, e);
                    onFailure.onFailure(e);
                });
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────

    /**
     * Updates an existing event document.
     * Uses SetOptions.merge() so only provided fields are overwritten.
     *
     * @param event     The Event object with updated values. eventId must be set.
     * @param onSuccess Called when the update succeeds.
     * @param onFailure Called with the exception if the update fails.
     */
    public void updateEvent(Event event, OnSuccessCallback onSuccess, OnFailureCallback onFailure) {
        if (event.getEventId() == null || event.getEventId().isEmpty()) {
            onFailure.onFailure(new IllegalArgumentException("Event eventId must not be null or empty"));
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(event.getEventId())
                .set(event, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Event updated: " + event.getEventId());
                    onSuccess.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update event: " + event.getEventId(), e);
                    onFailure.onFailure(e);
                });
    }

    /**
     * Adds a user (by deviceId) to one of the event's participant lists.
     * Use the LIST_* constants defined in this class for the fieldName.
     *
     * Example:
     *   EventDb.getInstance().addUserToList(eventId, EventDb.LIST_WAITING, deviceId, ...);
     *
     * @param eventId   The event document ID.
     * @param fieldName One of LIST_ATTENDING, LIST_SELECTED, LIST_WAITING, LIST_CANCELLED.
     * @param deviceId  The user's device ID to add.
     * @param onSuccess Called when the update succeeds.
     * @param onFailure Called with the exception if the update fails.
     */
    public void addUserToList(String eventId, String fieldName, String deviceId,
                              OnSuccessCallback onSuccess, OnFailureCallback onFailure) {
        db.collection(COLLECTION_NAME)
                .document(eventId)
                .update(fieldName, FieldValue.arrayUnion(deviceId))
                .addOnSuccessListener(unused -> onSuccess.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add user to event list. Event: " + eventId + " Field: " + fieldName, e);
                    onFailure.onFailure(e);
                });
    }

    /**
     * Removes a user (by deviceId) from one of the event's participant lists.
     *
     * @param eventId   The event document ID.
     * @param fieldName One of LIST_ATTENDING, LIST_SELECTED, LIST_WAITING, LIST_CANCELLED.
     * @param deviceId  The user's device ID to remove.
     * @param onSuccess Called when the update succeeds.
     * @param onFailure Called with the exception if the update fails.
     */
    public void removeUserFromList(String eventId, String fieldName, String deviceId,
                                   OnSuccessCallback onSuccess, OnFailureCallback onFailure) {
        db.collection(COLLECTION_NAME)
                .document(eventId)
                .update(fieldName, FieldValue.arrayRemove(deviceId))
                .addOnSuccessListener(unused -> onSuccess.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to remove user from event list. Event: " + eventId + " Field: " + fieldName, e);
                    onFailure.onFailure(e);
                });
    }

    /**
     * Moves a user from one participant list to another atomically using a Firestore batch write.
     * Useful for lottery flows: e.g. move from waitingList → selectedList.
     *
     * @param eventId       The event document ID.
     * @param fromFieldName The list to remove the user from.
     * @param toFieldName   The list to add the user to.
     * @param deviceId      The user's device ID to move.
     * @param onSuccess     Called when the batch succeeds.
     * @param onFailure     Called with the exception if the batch fails.
     */
    public void moveUserBetweenLists(String eventId, String fromFieldName, String toFieldName,
                                     String deviceId,
                                     OnSuccessCallback onSuccess, OnFailureCallback onFailure) {
        DocumentReference eventRef = db.collection(COLLECTION_NAME).document(eventId);

        db.runBatch(batch -> {
                    batch.update(eventRef, fromFieldName, FieldValue.arrayRemove(deviceId));
                    batch.update(eventRef, toFieldName,   FieldValue.arrayUnion(deviceId));
                })
                .addOnSuccessListener(unused -> onSuccess.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to move user between lists. Event: " + eventId, e);
                    onFailure.onFailure(e);
                });
    }

    /**
     * Stores the QR code data string on the event document.
     *
     * @param eventId    The event document ID.
     * @param qrCodeData The QR code payload string.
     * @param onSuccess  Called when the update succeeds.
     * @param onFailure  Called with the exception if the update fails.
     */
    public void setQrCode(String eventId, String qrCodeData,
                          OnSuccessCallback onSuccess, OnFailureCallback onFailure) {
        db.collection(COLLECTION_NAME)
                .document(eventId)
                .update("qrCodeData", qrCodeData)
                .addOnSuccessListener(unused -> onSuccess.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to set QR code for event: " + eventId, e);
                    onFailure.onFailure(e);
                });
    }

    // ── DELETE ─────────────────────────────────────────────────────────────

    /**
     * Deletes an event document from Firestore.
     *
     * @param eventId   The document ID of the event to delete.
     * @param onSuccess Called when the deletion succeeds.
     * @param onFailure Called with the exception if the deletion fails.
     */
    public void deleteEvent(String eventId, OnSuccessCallback onSuccess, OnFailureCallback onFailure) {
        db.collection(COLLECTION_NAME)
                .document(eventId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Event deleted: " + eventId);
                    onSuccess.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete event: " + eventId, e);
                    onFailure.onFailure(e);
                });
    }
}
