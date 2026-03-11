package com.example.auroraevents;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TestsSupport {
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

    public static void setUpEvent(Event event, long timeout, TimeUnit unit) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Boolean> status = new AtomicReference<>(true);

        EventDb.getInstance().addEvent(event,
                unused -> latch.countDown(),                       // on success this will unblock the thread
                e -> { status.set(false); latch.countDown(); }  // on failure this will also unblock the thread, but set status to false
        );

        try {
            assertTrue("setUpEvent timed out", latch.await(timeout, unit)); // blocks thread for 10 seconds max, but will get unlocked sooner if either callback from above fires
        } catch (InterruptedException e) {
            fail("setUpEvent was interrupted");
        }

        // this now runs only after we recieve the callback from addEvent
        assertTrue("setUpEvent failed", status.get());
    }

    public static void setUpEvent(Event event) {
        setUpEvent(event, 10, TimeUnit.SECONDS);
    }

    public static void takeDownEvent(Event event, long timeout, TimeUnit unit) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Boolean> status = new AtomicReference<>(true);

        EventDb.getInstance().deleteEvent(event.getEventId(),
                latch::countDown,
                e -> { status.set(false); latch.countDown(); }
        );

        try {
            assertTrue("takeDownEvent timed out", latch.await(timeout, unit));
        } catch (InterruptedException e) {
            fail("takeDownEvent was interrupted");
        }

        assertTrue("takeDownEvent failed", status.get());
    }

    public static void takeDownEvent(Event event) {
        takeDownEvent(event, 10, TimeUnit.SECONDS);
    }

    public static void setUpUser(User user, long timeout, TimeUnit unit) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Boolean> status = new AtomicReference<>(true);

        UserDb.getInstance().addUser(user,
                latch::countDown,                       // on success this will unblock the thread
                e -> { status.set(false); latch.countDown(); }  // on failure this will also unblock the thread, but set status to false
        );

        try {
            assertTrue("setUpUser timed out", latch.await(timeout, unit)); // blocks thread for 10 seconds max, but will get unlocked sooner if either callback from above fires
        } catch (InterruptedException e) {
            fail("setUpUser was interrupted");
        }

        // this now runs only after we receive the callback from addUser
        assertTrue("setUpUser failed", status.get());
    }

    public static void setUpUser(User user) {
        setUpUser(user, 10, TimeUnit.SECONDS);
    }

    public static void takeDownUser(User user, long timeout, TimeUnit unit) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Boolean> status = new AtomicReference<>(true);

        UserDb.getInstance().deleteUser(user.getDeviceId(),
                latch::countDown,
                e -> { status.set(false); latch.countDown(); }
        );

        try {
            assertTrue("takeDownUser timed out", latch.await(timeout, unit));
        } catch (InterruptedException e) {
            fail("takeDownUser was interrupted");
        }

        assertTrue("takeDownUSer failed", status.get());
    }

    public static void takeDownUser(User user) {
        takeDownUser(user, 10, TimeUnit.SECONDS);
    }

}
