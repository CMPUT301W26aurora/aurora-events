package com.example.auroraevents.server;


import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.auroraevents.model.Comment;
import com.example.auroraevents.model.Event;
import com.google.firebase.Timestamp;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;

import java.util.List;
import java.util.Map;

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
    public static final String LIST_REGISTRATION  = "registrationList";
    public static final String LIST_ATTENDING     = LIST_REGISTRATION + '.' + "attendingList";
    public static final String LIST_SELECTED      = LIST_REGISTRATION + '.' + "selectedList";
    public static final String LIST_WAITING       = LIST_REGISTRATION + '.' + "waitingList";
    public static final String LIST_CANCELLED     = LIST_REGISTRATION + '.' + "cancelledList";
    public static final String LIST_DECLINED      = LIST_REGISTRATION + '.' + "declinedList";
    public static final String LIST_REMOVED       = LIST_REGISTRATION + '.' + "removedList";
    public static final String[] ALL_LISTS        = {LIST_ATTENDING, LIST_SELECTED, LIST_WAITING, LIST_CANCELLED, LIST_DECLINED, LIST_REMOVED};

    // Co-organizer field name
    public static final String LIST_CO_ORGANIZERS = "coOrganizerDeviceIds";


    private static EventDb instance;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ── Callbacks ──────────────────────────────────────────────────────────

    public interface OnSuccessCallback          { void onSuccess(); }
    public interface OnFailureCallback          { void onFailure(Exception e); }
    public interface OnEventCreatedCallback     { void onCreated(String eventId); }
    public interface OnEventFetchedCallback     { void onFetched(Event event); }
    public interface OnEventListFetchedCallback { void onFetched(List<Event> events); }

    // ── Singleton ──────────────────────────────────────────────────────────

    private EventDb() {}

    public static synchronized EventDb getInstance() {
        if (instance == null) {
            instance = new EventDb();
        }
        return instance;
    }

    // ── CREATE ─────────────────────────────────────────────────────────────

    //https://firebase.google.com/docs/storage/android/upload-files
    public void saveUrlToFirestore(String eventId, String url, String field){
        db.collection(COLLECTION_NAME)
                .document(eventId)
                .update(field, url)
                .addOnSuccessListener(unused->{
                    Log.d(TAG, "added image " + field);
                })
                .addOnFailureListener(e->{
                    Log.e(TAG, "Failed upload to " + field + " " +e );
                });
    }

    public void uploadPoster(Uri uri, String eventId) {
        if (uri == null || eventId == null) return;

        StorageReference fileRef = FirebaseStorage.getInstance().getReference()
                .child(eventId + "/" +"poster.jpg");

        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        saveUrlToFirestore(eventId, downloadUri.toString(), "posterUrl");
                    });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Upload failed for poster", e));
    }

    public void uploadQr(Bitmap qr, String eventId){
        if (qr == null || eventId == null) return;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        qr.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();


        StorageReference fileRef = FirebaseStorage.getInstance().getReference()
                .child(eventId + "/" +"qr.png");

        fileRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot->{
                    fileRef.getDownloadUrl().addOnSuccessListener(downloadUri->{
                        saveUrlToFirestore(eventId, downloadUri.toString(), "qrCodeUrl");
                    });
                })
                .addOnFailureListener(e->Log.e(TAG, "Upload failed for url"));
    }



    /**
     * Adds a new event document to Firestore with an auto-generated ID.
     * After creation, the generated ID is written back into event.eventId.
     *
     * @param event     The Event object to persist.
     * @param onCreated Called with the new auto-generated document ID.
     * @param onFailure Called with the exception if the write fails.
     */
    public static void addEvent(Event event, OnEventCreatedCallback onCreated, OnFailureCallback onFailure) {
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
     * @param fieldName One of LIST_ATTENDING, LIST_SELECTED, LIST_WAITING, LIST_CANCELLED, LIST_DECLINED, LIST_REMOVED.
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
     * @param fieldName One of LIST_ATTENDING, LIST_SELECTED, LIST_WAITING, LIST_CANCELLED, LIST_DECLINED, LIST_REMOVED.
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
     * Removes a user (by deviceId) from one of the event's participant lists. currently broken, fix sooner rather than later....
     *
     * @param eventId   The event document ID.
     * @param fieldName One of LIST_ATTENDING, LIST_SELECTED, LIST_WAITING, LIST_CANCELLED, LIST_DECLINED, LIST_REMOVED.
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
    public void moveUserBetweenLists(String eventId, String from, String to, String id,
                               OnSuccessCallback sc, OnFailureCallback fc) {
        moveGroupUsers(eventId, from, to, Collections.singletonList(id), sc, fc);
    }

    /**

     * Moves a user from the Selected list to the Attending list.
     */
    public void userAcceptSelection(String eventId, String userId, OnSuccessCallback onSuccess, OnFailureCallback onFailure) {
        moveUserBetweenLists(eventId, LIST_SELECTED, LIST_ATTENDING, userId, onSuccess, onFailure);
    }

    /**
     * Moves a user from the Selected list to the Declined list.
     */
    public void userDeclineSelection(String eventId, String userId, OnSuccessCallback onSuccess, OnFailureCallback onFailure) {
        moveUserBetweenLists(eventId, LIST_SELECTED, LIST_DECLINED, userId, onSuccess, onFailure);
    }

    /**
     * Adds a user to the waiting list.
     */
    public void joinWaitlist(String eventId, String userId, OnSuccessCallback onSuccess, OnFailureCallback onFailure) {
        addUserToList(eventId, LIST_WAITING, userId, onSuccess, onFailure);
    }

    /**
     * Moves a user from the Waiting list to the canceled list (User clicked 'Leave Pool').
     */
    public void leaveWaitlist(String eventId, String userId, OnSuccessCallback onSuccess, OnFailureCallback onFailure) {
        moveUserBetweenLists(eventId, LIST_WAITING, LIST_CANCELLED, userId, onSuccess, onFailure);
    }

    //https://www.geeksforgeeks.org/firebase/how-to-update-an-array-of-objects-with-firestore/
    /**
     * Batch move users from one list to another
     *
     * @param eventId       The event document ID
     * @param fromFieldName The list moving from
     * @param toFieldName   the list moving to
     * @param ids           the users to batch move
     * @param onSuccess     Called on success
     * @param onFailure     called on failure
     */
    public void moveGroupUsers(String eventId, String fromFieldName, String toFieldName, List<String> ids,
                               OnSuccessCallback onSuccess, OnFailureCallback onFailure){
        DocumentReference eventRef = db.collection(COLLECTION_NAME).document(eventId);
        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) return;
            WriteBatch batch = db.batch();

            if (fromFieldName.equals(LIST_SELECTED)) {
                List<Map<String, Object>> currentSelected = (List<Map<String, Object>>) documentSnapshot.get(fromFieldName);
                if (currentSelected != null) {
                    currentSelected.removeIf(map -> ids.contains(map.get("userId")));
                    batch.update(eventRef, fromFieldName, currentSelected);
                }
            } else {
                batch.update(eventRef, fromFieldName, FieldValue.arrayRemove(ids.toArray()));
            }

            if (toFieldName.equals(LIST_SELECTED)) {
                List<Map<String, Object>> wrappedMaps = new ArrayList<>();
                Timestamp now = Timestamp.now();
                for (String id : ids) {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("userId", id);
                    userMap.put("selectedAt", now);
                    wrappedMaps.add(userMap);
                }
                batch.update(eventRef, toFieldName, FieldValue.arrayUnion(wrappedMaps.toArray()));
            } else {
                batch.update(eventRef, toFieldName, FieldValue.arrayUnion(ids.toArray()));
            }

            batch.commit()
                    .addOnSuccessListener(unused -> {
                        Log.d(TAG, "Move Completed");
                        onSuccess.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG,"Failed to batch commit");
                        onFailure.onFailure(e);
                    });

        }).addOnFailureListener(e->{
            Log.e(TAG, "Failed to pull event");
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

    // ── CO-ORGANIZER ───────────────────────────────────────────────────────

    /**
     * Promotes an existing entrant to co-organizer for the given event.
     *
     * This is done in a single batch:
     *   1. Adds the deviceId to {@code coOrganizerDeviceIds}.
     *   2. Removes the deviceId from every entrant list (waiting, selected,
     *      attending, declined, cancelled, removed) so they leave the entrant pool.
     *
     * @param eventId   The event document ID.
     * @param deviceId  The device ID of the entrant to promote.
     * @param onSuccess Called when the batch write succeeds.
     * @param onFailure Called with the exception if the batch fails.
     */
    public void addCoOrganizer(String eventId, String deviceId,
                               OnSuccessCallback onSuccess, OnFailureCallback onFailure) {
        DocumentReference eventRef = db.collection(COLLECTION_NAME).document(eventId);
        WriteBatch batch = db.batch();

        // Add to co-organizer list
        batch.update(eventRef, LIST_CO_ORGANIZERS, FieldValue.arrayUnion(deviceId));

        // Remove from every entrant list atomically
        batch.update(eventRef,
                LIST_WAITING,   FieldValue.arrayRemove(deviceId),
                LIST_SELECTED,  FieldValue.arrayRemove(deviceId),
                LIST_ATTENDING, FieldValue.arrayRemove(deviceId),
                LIST_DECLINED,  FieldValue.arrayRemove(deviceId),
                LIST_CANCELLED, FieldValue.arrayRemove(deviceId),
                LIST_REMOVED,   FieldValue.arrayRemove(deviceId)
        );

        batch.commit()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Co-organizer added: " + deviceId + " for event: " + eventId);
                    onSuccess.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add co-organizer: " + deviceId + " for event: " + eventId, e);
                    onFailure.onFailure(e);
                });
    }

    /**
     * Removes a co-organizer from the event. The user is only removed from
     * {@code coOrganizerDeviceIds} — they are not placed back into any entrant list.
     *
     * @param eventId   The event document ID.
     * @param deviceId  The device ID of the co-organizer to demote.
     * @param onSuccess Called when the update succeeds.
     * @param onFailure Called with the exception if the update fails.
     */
    public void removeCoOrganizer(String eventId, String deviceId,
                                  OnSuccessCallback onSuccess, OnFailureCallback onFailure) {
        db.collection(COLLECTION_NAME)
                .document(eventId)
                .update(LIST_CO_ORGANIZERS, FieldValue.arrayRemove(deviceId))
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Co-organizer removed: " + deviceId + " for event: " + eventId);
                    onSuccess.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to remove co-organizer: " + deviceId + " for event: " + eventId, e);
                    onFailure.onFailure(e);
                });
    }

    // ── DELETE ─────────────────────────────────────────────────────────────

    /**
     * Deletes an event document from Firestore
     *
     * @param eventId   The document ID of the event to delete.
     * @param onSuccess Called when the deletion succeeds.
     * @param onFailure Called with the exception if the deletion fails.
     */
    public void deleteEvent(String eventId, OnSuccessCallback onSuccess, OnFailureCallback onFailure) {
        db.collection(COLLECTION_NAME).document(eventId)
                .delete()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "Event Deleted" + eventId);
                    onSuccess.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete Event", e);
                    onFailure.onFailure(e);
                });
    }

    public void deletePoster(String eventId, OnSuccessCallback onSuccess, OnFailureCallback onFailure){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        db.collection(COLLECTION_NAME).document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot->{
                    String posterUrl = documentSnapshot.getString("posterUrl");
                    if(posterUrl != null && !posterUrl.isEmpty() ){
                        StorageReference photoRef = storage.getReferenceFromUrl(posterUrl);

                        photoRef.delete().addOnSuccessListener(aVoid -> {
                            //on succesfull photo delete
                            Log.d(TAG, "Deleted event poster");
                            db.collection(COLLECTION_NAME).document(eventId)
                                    .update("posterUrl", null)
                                    .addOnSuccessListener(v -> {
                                        //on url field clear
                                        Log.d(TAG, "Deleted poster and cleared URL");
                                        onSuccess.onSuccess();
                                    })
                                    .addOnFailureListener(e->{
                                        //failure to clear url
                                        Log.e(TAG, "failed to delete URL", e);
                                        onFailure.onFailure(e);
                                    });
                        });
                    }else{
                        //nothing to delete, we good
                        onSuccess.onSuccess();
                    }
                }).addOnFailureListener(e->{
                    Log.e(TAG, "failed to grab event info");
                    onFailure.onFailure(e);
                });
    }

    // ── SNAPSHOT LISTENER ─────────────────────────────────────────────────

    public interface OnEventSnapshotCallback { void onEventSnapshot(Event event); }

    public ListenerRegistration addSnapshotListenerForEvent(String eventId, OnEventSnapshotCallback onEventSnapshot, OnFailureCallback onFailure) {
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(eventId);
        return docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {

            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    onFailure.onFailure(e);
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    Event event = snapshot.toObject(Event.class);
                    event.setEventId(snapshot.getId());
                    onEventSnapshot.onEventSnapshot(event);
                } else {
                    Log.d(TAG, "Current data: null");
                    onEventSnapshot.onEventSnapshot(null);
                }
            }
        });
    }

    public ListenerRegistration eventListenerAll( OnEventListFetchedCallback onFetched, OnFailureCallback onFailure) {
        return db.collection(COLLECTION_NAME)
                .addSnapshotListener((value, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        onFailure.onFailure(e);
                        return;
                    }
                    if (value != null) {
                        List<Event> events = value.toObjects(Event.class);
                        onFetched.onFetched(events);
                    }
                });
    }
}