package com.example.auroraevents;

import android.animation.ValueAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.auroraevents.view.CameraFragment;
import com.example.auroraevents.view.EventListFragment;
import com.example.auroraevents.view.NotificationFragment;
import com.example.auroraevents.view.ProfileFragment;
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

    private ImageButton navScan, navBrowse, navNotifications, navProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navScan          = findViewById(R.id.nav_scan);
        navBrowse        = findViewById(R.id.nav_browse);
        navNotifications = findViewById(R.id.nav_notifications);
        navProfile       = findViewById(R.id.nav_profile);

        deviceId   = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // create notification channel
        createNotificationChannel();

        // request notification permission
        requestNotificationPermission();

        // sign in anonymously, then save FCM token
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

        // Set default tab
        setActiveTab(navBrowse);
        loadFragment(new EventListFragment());

        navScan.setOnClickListener(v -> {
            setActiveTab(navScan);
            loadFragment(new CameraFragment());
        });

        navBrowse.setOnClickListener(v -> {
            setActiveTab(navBrowse);
            loadFragment(new EventListFragment());
        });

        navNotifications.setOnClickListener(v -> {
            setActiveTab(navNotifications);
            loadFragment(new NotificationFragment());
        });

        navProfile.setOnClickListener(v -> {
            setActiveTab(navProfile);
            loadFragment(new ProfileFragment());
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
    private void setActiveTab(ImageButton selected) {
        ImageButton[] tabs = { navScan, navBrowse, navNotifications, navProfile };

        for (ImageButton tab : tabs) {
            int targetWidth = dpToPx(tab == selected ? 88 : 52);
            int drawable = (tab == selected)
                    ? R.drawable.nav_item_active
                    : R.drawable.nav_item_inactive;

            animateTabWidth(tab, targetWidth);
            tab.setBackground(ContextCompat.getDrawable(this, drawable));
        }
    }

    private void animateTabWidth(ImageButton tab, int targetWidth) {
        int startWidth = tab.getLayoutParams().width;

        ValueAnimator animator = ValueAnimator.ofInt(startWidth, targetWidth);
        animator.setDuration(250); // milliseconds
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(anim -> {
            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) tab.getLayoutParams();
            params.width = (int) anim.getAnimatedValue();
            tab.setLayoutParams(params);
        });
        animator.start();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
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