package com.example.auroraevents;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class InfoUEventTestSupport {

    public static void signIn(long timeout, TimeUnit unit) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Boolean> status = new AtomicReference<>(true);

        FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener(result -> latch.countDown())
                .addOnFailureListener(e -> {
                    Log.e("TEST", "signIn failed", e);
                    status.set(false);
                    latch.countDown();
                });

        try {
            assertTrue("signIn timed out", latch.await(timeout, unit));
        } catch (InterruptedException e) {
            fail("signIn was interrupted");
        }
        assertTrue("signIn failed", status.get());
    }

    public static void signIn() {
        signIn(10, TimeUnit.SECONDS);
    }
}