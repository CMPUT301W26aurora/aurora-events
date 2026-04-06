package com.example.auroraevents.server;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helper class to send FCM push notifications to a list of device IDs.
 * Looks up each device's FCM token from Firestore (stored by FirebaseNotificationHandler.onNewToken),
 * then sends the notification via a Firebase Cloud Function.
 *
 * Requires a Cloud Function named "sendNotification" deployed to your project.
 * See the comment below for the Cloud Function code.
 * @author Joshua Terry
 */
public class NotificationSender {

    private static final String TAG              = "NotificationSender";
    private static final String COLLECTION_USERS = "Users";
    private static final String FIELD_FCM_TOKEN  = "fcmToken";

    /**
     * Sends a push notification to all provided device IDs.
     *
     * @param deviceIds  List of device IDs to notify
     * @param sentFromId The organizer's device ID sending the notification
     * @param title      Notification title (typically the event name)
     * @param body       Notification message body
     * @param eventId    Event ID to include in the notification data payload
     * @param onSuccess  Runnable called on the main thread when all sends succeed
     * @param onFailure  Callback called if any send fails
     */
    public static void send(
            List<String> deviceIds,
            String sentFromId,
            String title,
            String body,
            String eventId,
            Runnable onSuccess,
            OnFailureListener onFailure
    ) {
        FirebaseFirestore db      = FirebaseFirestore.getInstance();
        AtomicInteger     pending = new AtomicInteger(deviceIds.size());
        AtomicInteger     failed  = new AtomicInteger(0);

        if (deviceIds.isEmpty()) {
            onSuccess.run();
            return;
        }

        for (String deviceId : deviceIds) {
            db.collection(COLLECTION_USERS)
                    .document(deviceId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String token = doc.getString(FIELD_FCM_TOKEN);
                        if (token != null) {
                            sendToToken(token, sentFromId, title, body, eventId,
                                    () -> checkDone(pending, failed, onSuccess, onFailure),
                                    e  -> {
                                        failed.incrementAndGet();
                                        checkDone(pending, failed, onSuccess, onFailure);
                                    });
                        } else {
                            Log.w(TAG, "No FCM token for device: " + deviceId);
                            checkDone(pending, failed, onSuccess, onFailure);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to fetch token for device: " + deviceId, e);
                        failed.incrementAndGet();
                        checkDone(pending, failed, onSuccess, onFailure);
                    });
        }
    }

    /**
     * Calls the "sendNotification" Firebase Cloud Function with the token and message.
     */
    private static void sendToToken(
            String token,
            String sentFromId,
            String title,
            String body,
            String eventId,
            Runnable onSuccess,
            OnFailureListener onFailure
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("token",      token);
        data.put("sentFromId", sentFromId);
        data.put("title",      title);
        data.put("body",       body);
        data.put("eventId",    eventId);

        FirebaseFunctions.getInstance()
                .getHttpsCallable("sendNotification")
                .call(data)
                .addOnSuccessListener(result -> onSuccess.run())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Cloud Function call failed", e);
                    onFailure.onFailure(e);
                });
    }

    private static void checkDone(AtomicInteger pending, AtomicInteger failed,
                                  Runnable onSuccess, OnFailureListener onFailure) {
        if (pending.decrementAndGet() == 0) {
            if (failed.get() == 0) {
                onSuccess.run();
            } else {
                onFailure.onFailure(new Exception(failed.get() + " notification(s) failed to send"));
            }
        }
    }

    public interface OnFailureListener {
        void onFailure(Exception e);
    }
}