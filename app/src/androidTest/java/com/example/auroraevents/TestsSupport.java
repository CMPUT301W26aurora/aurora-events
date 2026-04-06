package com.example.auroraevents;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.util.Log;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;

import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.User;
import com.example.auroraevents.server.EventDb;
import com.example.auroraevents.server.UserDb;
import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Matcher;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Support functions for testing
 * @author Jared Strandlund
 * @author Sean Ross (Sourced clickChildWithView)
 */
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


    /**
     * Solution for clicking a specific child view inside a RecyclerView item.
     * from: https://stackoverflow.com/questions/28476507/using-espresso-to-click-view-inside-recyclerview-item
     * answer:https://stackoverflow.com/a/30338665
     * Author: blade
     */
    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                v.performClick();
            }
        };
    }
}
