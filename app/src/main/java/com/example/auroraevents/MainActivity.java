package com.example.auroraevents;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        verifyFirebaseSignIn(savedInstanceState);
    }

    private void verifyFirebaseSignIn(Bundle savedInstanceState) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            Log.d(TAG, "User already signed in: " + auth.getCurrentUser().getUid());
            saveFcmToken();
            loadStartFragment(savedInstanceState);
        } else {
            auth.signInAnonymously()
                    .addOnSuccessListener(result -> {
                        Log.d(TAG, "Anonymous sign-in successful: " + result.getUser().getUid());
                        saveFcmToken();
                        loadStartFragment(savedInstanceState);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Anonymous sign-in failed", e);
                    });
        }
    }

    private void saveFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(deviceId)
                            .set(Collections.singletonMap("fcmToken", token), SetOptions.merge())
                            .addOnSuccessListener(unused ->
                                    Log.d(TAG, "FCM token saved for device: " + deviceId))
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Failed to save FCM token", e));
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to get FCM token", e));
    }

    private void loadStartFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, new EventFragment())
                    .commit();
        }
    }
}