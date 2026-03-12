package com.example.auroraevents;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String NOTIFICATION_CHANNEL_ID = "default";
    private static final String NOTIFICATION_CHANNEL_NAME = "Default";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        deviceId   = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // create notification channel
        createNotificationChannel();

        // request notification permission (required for Android 13+)
        requestNotificationPermission();

        // Step 3 — sign in anonymously, then save FCM token
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener(result -> {
                    FirebaseMessaging.getInstance().getToken()
                            .addOnSuccessListener(token -> {
                                FirebaseFirestore.getInstance()
                                        .collection("Users")
                                        .document(deviceId)
                                        .set(Collections.singletonMap("fcmToken", token), SetOptions.merge())
                                        .addOnSuccessListener(unused -> {
                                            Log.d(TAG, "FCM token saved for device: " + deviceId);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to save FCM token", e);
                                        });
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Anonymous sign-in failed", e);
                });
    }

    /**
     * Creates the default notification channel required for Android 8.0 (Oreo) and above.
     * Must be called before any notifications are displayed.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            Log.d(TAG, "Notification channel created");
        }
    }

    /**
     * Requests the POST_NOTIFICATIONS permission required for Android 13 (Tiramisu) and above.
     * Presents a system permission dialog to the user if not already granted.
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE
            );
        }
    }
}