package com.example.auroraevents;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class InfoUEventHelper {

    private static final String TAG = "InfoUEventHelper";

    public static void signIn(long timeout, TimeUnit unit) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Boolean> status = new AtomicReference<>(true);

        FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener(result -> latch.countDown())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "sign in failed", e);
                    status.set(false);
                    latch.countDown();
                });

        try {
            latch.await(timeout, unit);
        } catch (InterruptedException e) {
            Log.e(TAG, "sign in failed", e);
        }
    }

    public static void signIn() {
        signIn(10, TimeUnit.SECONDS);
    }
}