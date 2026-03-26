package com.example.auroraevents;

import static com.example.auroraevents.registration_tests.RegistrationListTestsSupport.checkSingle;
import static com.example.auroraevents.registration_tests.RegistrationListTestsSupport.setUpAttendingList;
import static com.example.auroraevents.registration_tests.RegistrationListTestsSupport.setUpCancelledList;
import static com.example.auroraevents.registration_tests.RegistrationListTestsSupport.setUpDeclinedList;
import static com.example.auroraevents.registration_tests.RegistrationListTestsSupport.setUpRemovedList;
import static com.example.auroraevents.registration_tests.RegistrationListTestsSupport.setUpSelectedList;
import static com.example.auroraevents.registration_tests.RegistrationListTestsSupport.setUpWaitingList;
import static com.example.auroraevents.TestsSupport.setUpEvent;
import static com.example.auroraevents.TestsSupport.setUpUser;
import static com.example.auroraevents.TestsSupport.signIn;
import static com.example.auroraevents.TestsSupport.takeDownEvent;
import static com.example.auroraevents.TestsSupport.takeDownUser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.User;
import com.example.auroraevents.server.EventDb;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;

public class UserDeleteTest {
    User user;
    User user2;
    Event waitingEvent;
    Event selectedEvent;
    Event attendingEvent;
    Event declinedEvent;
    Event cancelledEvent;
    Event removedEvent;
    final String TAG = "UserDeleteTest";

    @BeforeClass
    public static void prepare() {
        signIn();
    }

    @Before
    public void before() {
        user = new User(
                "user delete test",
                "User for the user delete tests",
                "user@delete.test",
                "5559998888",
                User.ROLE_ENTRANT
        );
        setUpUser(user);

        user2 = new User(
                "user delete test user 2",
                "Second user for the user delete test",
                "user2@delete.test",
                "1234567890",
                User.ROLE_ENTRANT
        );
        setUpUser(user2);

        waitingEvent = new Event(
                "test organizer",
                "waiting",
                "event for user on the waiting list for profile delete test",
                "free",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                TAG,
                false,
                0,
                null);
        selectedEvent = new Event(
                "test organizer",
                "selected",
                "event for user on the selected list for profile delete test",
                "free",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                TAG,
                false,
                0,
                null);
        attendingEvent = new Event(
                "test organizer",
                "attending",
                "event for user on the attending list for profile delete test",
                "free",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                TAG,
                false,
                0,
                null);
        declinedEvent = new Event(
                "test organizer",
                "declined",
                "event for user on the declined list for profile delete test",
                "free",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                TAG,
                false,
                0,
                null);
        cancelledEvent = new Event(
                "test organizer",
                "cancelled",
                "event for user on the cancelled list for profile delete test",
                "free",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                TAG,
                false,
                0,
                null);
        removedEvent = new Event(
                "test organizer",
                "removed",
                "event for user on the removed list for profile delete test",
                "free",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                TAG,
                false,
                0,
                null);

        setUpEvent(waitingEvent);
        setUpWaitingList(waitingEvent.registrationList, user.getDeviceId());
        setUpWaitingList(waitingEvent.registrationList, user2.getDeviceId());

        setUpEvent(selectedEvent);
        setUpSelectedList(selectedEvent.registrationList, user.getDeviceId());
        setUpSelectedList(selectedEvent.registrationList, user2.getDeviceId());

        setUpEvent(attendingEvent);
        setUpAttendingList(attendingEvent.registrationList, user.getDeviceId());
        setUpAttendingList(attendingEvent.registrationList, user2.getDeviceId());

        setUpEvent(declinedEvent);
        setUpDeclinedList(declinedEvent.registrationList, user.getDeviceId());
        setUpDeclinedList(declinedEvent.registrationList, user2.getDeviceId());

        setUpEvent(cancelledEvent);
        setUpCancelledList(cancelledEvent.registrationList, user.getDeviceId());
        setUpCancelledList(cancelledEvent.registrationList, user2.getDeviceId());

        setUpEvent(removedEvent);
        setUpRemovedList(removedEvent.registrationList, user.getDeviceId());
        setUpRemovedList(removedEvent.registrationList, user2.getDeviceId());
    }

    @After
    public void after() {
        takeDownUser(user);
        takeDownUser(user2);
        takeDownEvent(waitingEvent);
        takeDownEvent(selectedEvent);
        takeDownEvent(attendingEvent);
        takeDownEvent(declinedEvent);
        takeDownEvent(cancelledEvent);
        takeDownEvent(removedEvent);
    }

    @Test
    public void deleteTest() {
        user.deleteUser();
        final CountDownLatch firstLatch = new CountDownLatch(1);
        try {
            firstLatch.await(user2.getDatabaseTimeout(), user2.getTimeoutUnit());
        } catch (InterruptedException ignored) {
        }
        // Make sure the fields are cleared
        assertEquals("", user.getName());
        assertEquals("", user.getEmail());
        assertEquals("", user.getPhoneNumber());
        assertEquals(User.ROLE_ENTRANT, user.getRole());
        assertEquals(0, user.getNotificationHistory().size());
        assertEquals(0, user.getTags().size());
        // Make sure user isn't on registration lists, and didn't delete other users
        final CountDownLatch waitingLatch = new CountDownLatch(1);
        EventDb.getInstance().getEventsForUser(user2.getDeviceId(), EventDb.LIST_WAITING,
                events -> {
                    assertEquals(1, events.size());
                    waitingEvent = events.get(0);
                    waitingLatch.countDown();
                }, e -> {
                    fail();
                    waitingLatch.countDown();
                }
        );
        try {
            assertTrue(waitingLatch.await(user2.getDatabaseTimeout(), user2.getTimeoutUnit()));
        } catch (InterruptedException e) {
            fail();
        }

        final CountDownLatch selectedLatch = new CountDownLatch(1);
        EventDb.getInstance().getEventsForUser(user2.getDeviceId(), EventDb.LIST_SELECTED,
                events -> {
                    assertEquals(1, events.size());
                    selectedEvent = events.get(0);
                    selectedLatch.countDown();
                }, e -> {
                    fail();
                    selectedLatch.countDown();
                }
        );
        try {
            assertTrue(selectedLatch.await(user2.getDatabaseTimeout(), user2.getTimeoutUnit()));
        } catch (InterruptedException e) {
            fail();
        }

        final CountDownLatch attendingLatch = new CountDownLatch(1);
        EventDb.getInstance().getEventsForUser(user2.getDeviceId(), EventDb.LIST_ATTENDING,
                events -> {
                    assertEquals(1, events.size());
                    attendingEvent = events.get(0);
                    attendingLatch.countDown();
                }, e -> {
                    fail();
                    attendingLatch.countDown();
                }
        );
        try {
            assertTrue(attendingLatch.await(user2.getDatabaseTimeout(), user2.getTimeoutUnit()));
        } catch (InterruptedException e) {
            fail();
        }

        final CountDownLatch declinedLatch = new CountDownLatch(1);
        EventDb.getInstance().getEventsForUser(user2.getDeviceId(), EventDb.LIST_DECLINED,
                events -> {
                    assertEquals(1, events.size());
                    declinedEvent = events.get(0);
                    declinedLatch.countDown();
                }, e -> {
                    fail();
                    declinedLatch.countDown();
                }
        );
        try {
            assertTrue(declinedLatch.await(user2.getDatabaseTimeout(), user2.getTimeoutUnit()));
        } catch (InterruptedException e) {
            fail();
        }

        final CountDownLatch cancelledLatch = new CountDownLatch(1);
        EventDb.getInstance().getEventsForUser(user2.getDeviceId(), EventDb.LIST_CANCELLED,
                events -> {
                    assertEquals(1, events.size());
                    cancelledEvent = events.get(0);
                    cancelledLatch.countDown();
                }, e -> {
                    fail();
                    cancelledLatch.countDown();
                }
        );
        try {
            assertTrue(cancelledLatch.await(user2.getDatabaseTimeout(), user2.getTimeoutUnit()));
        } catch (InterruptedException e) {
            fail();
        }

        final CountDownLatch removedLatch = new CountDownLatch(1);
        EventDb.getInstance().getEventsForUser(user2.getDeviceId(), EventDb.LIST_REMOVED,
                events -> {
                    assertEquals(1, events.size());
                    removedEvent = events.get(0);
                    removedLatch.countDown();
                }, e -> {
                    fail();
                    removedLatch.countDown();
                }
        );
        try {
            assertTrue(removedLatch.await(user2.getDatabaseTimeout(), user2.getTimeoutUnit()));
        } catch (InterruptedException e) {
            fail();
        }

        final CountDownLatch lastLatch = new CountDownLatch(1);
        try {
            lastLatch.await(user2.getDatabaseTimeout(), user2.getTimeoutUnit());
        } catch (InterruptedException ignored) {
        }

        checkSingle(waitingEvent.registrationList, user2.getDeviceId());
        checkSingle(selectedEvent.registrationList, user2.getDeviceId());
        checkSingle(attendingEvent.registrationList, user2.getDeviceId());
        checkSingle(declinedEvent.registrationList, user2.getDeviceId());
        checkSingle(cancelledEvent.registrationList, user2.getDeviceId());
        checkSingle(removedEvent.registrationList, user2.getDeviceId());
    }
}
