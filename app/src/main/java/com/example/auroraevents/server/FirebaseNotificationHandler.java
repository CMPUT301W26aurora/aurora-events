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

public class FirebaseNotificationHandler extends FirebaseMessagingService {

    private static final String TAG              = "FCMService";
    private static final String COLLECTION_USERS = "Users";
    private static final String FIELD_FCM_TOKEN  = "fcmToken";

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

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        Map<String, String> data = message.getData();
        if (data.isEmpty()) return;

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String eventId  = data.get("eventId");
        String title    = data.get("title");
        String body     = data.get("body");

        Log.d(TAG, "onMessageReceived: " + title + " — " + body);

        // Save to Firestore
        Notification notification = new Notification(deviceId, eventId, title, body);
        FirebaseFirestore.getInstance()
                .collection("Notifications")
                .add(notification)
                .addOnSuccessListener(ref -> {
                    notification.setNotificationId(ref.getId());
                    Log.d(TAG, "Notification saved: " + ref.getId());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save notification", e));

        showNotification(title, body);
    }

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