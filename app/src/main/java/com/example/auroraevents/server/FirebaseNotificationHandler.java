package com.example.auroraevents.server;

import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Collections;

/**
 * Firebase Cloud Messaging service for handling FCM token refresh and incoming messages.
 *
 * Responsible for storing the device's FCM token in Firestore whenever it is refreshed,
 * and handling incoming push notifications when the app is in the foreground.
 *
 * Register this service in AndroidManifest.xml with the MESSAGING_EVENT intent filter.
 * Also call FirebaseMessaging.getInstance().getToken() on app startup to ensure the
 * token is always current.
 */
public class FirebaseNotificationHandler extends FirebaseMessagingService {
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
     * Called when a push notification is received while the app is in the foreground.
     *
     * When the app is in the background or closed, Android displays the notification
     * automatically and this method is not called. Override this method to handle
     * foreground notifications, such as showing an in-app alert or updating the UI.
     *
     * @param message The incoming RemoteMessage containing the notification payload.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        if (message.getNotification() == null) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(message.getNotification().getTitle())
                .setContentText(message.getNotification().getBody())
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
