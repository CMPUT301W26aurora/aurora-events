package com.example.auroraevents.server;

import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.auroraevents.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Collections;
import java.util.Map;

/**
 * Firebase Cloud Messaging service for handling FCM token refresh and incoming messages.
 *
 * Responsible for storing the device's FCM token in Firestore whenever it is refreshed,
 * and handling incoming push notifications when the app is in the foreground or background.
 *
 * Register this service in AndroidManifest.xml with the MESSAGING_EVENT intent filter.
 * Also call FirebaseMessaging.getInstance().getToken() on app startup to ensure the
 * token is always current.
 */
public class FirebaseNotificationHandler extends FirebaseMessagingService {

    private static final String TAG              = "FCMService";
    private static final String COLLECTION_USERS = "Users";
    private static final String FIELD_FCM_TOKEN  = "fcmToken";

    /**
     * Called by Firebase when the device's FCM registration token is refreshed.
     *
     * Tokens may be refreshed when the app is restored on a new device, the user
     * uninstalls and reinstalls the app, or the user clears the app's data.
     *
     * The new token is stored in Firestore under Users/{deviceId}/fcmToken
     * using SetOptions.merge() so other user fields are not overwritten.
     *
     * @param token The new FCM registration token for this device.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        String deviceId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID
        );

        FirebaseFirestore.getInstance()
                .collection(COLLECTION_USERS)
                .document(deviceId)
                .set(Collections.singletonMap(FIELD_FCM_TOKEN, token), SetOptions.merge());
    }

    /**
     * Called when a data message is received, regardless of whether the app is in the
     * foreground or background. Saves the notification to Firestore and displays it
     * in the system tray.
     *
     * @param message The incoming RemoteMessage containing the data payload.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        Map<String, String> data = message.getData();
        if (data.isEmpty()) return;

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String title    = data.get("title");
        String body     = data.get("body");

        Log.d(TAG, "onMessageReceived: " + title + " — " + body);

        // Save to Firestore
        Notification notification = new Notification(deviceId, title, body);
        FirebaseFirestore.getInstance()
                .collection("Notifications")
                .add(notification)
                .addOnSuccessListener(ref -> {
                    notification.setNotificationId(ref.getId());
                    Log.d(TAG, "Notification saved: " + ref.getId());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save notification", e));

        // Show in tray
        showNotification(title, body);
    }

    /**
     * Displays a notification in the system tray.
     * On Android 13+ this requires POST_NOTIFICATIONS permission.
     *
     * @param title notification title
     * @param body  notification body
     */
    private void showNotification(String title, String body) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED) {
            manager.notify(1, builder.build());
        }
    }
}