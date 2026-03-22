package com.example.auroraevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.example.auroraevents.TestsSupport.setUpEvent;
import static com.example.auroraevents.TestsSupport.setUpUser;
import static com.example.auroraevents.TestsSupport.signIn;
import static com.example.auroraevents.TestsSupport.takeDownEvent;
import static com.example.auroraevents.TestsSupport.takeDownUser;

import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.User;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;

public class ProfileFragmentTest {
    User user;
    final String TAG = "ProfileFragmentTest";

    @BeforeClass
    public static void prepare() {
        signIn();
    }

    @Before
    public void before() {
        user = new User(
                "profile test",
                "User for the profile tests",
                "user@profile.test",
                "",
                User.ROLE_ENTRANT
        );
        setUpUser(user);
    }

    @After
    public void after() {
        takeDownUser(user);
    }

    @Test
    public void deleteTest() {
        // Set up
        Event noneEvent = new Event(
                "test organizer",
                "none",
                "event for user not on the list for profile delete test",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                TAG,
                0);
        Event waitingEvent = new Event(
                "test organizer",
                "none",
                "event for user on the waiting list for profile delete test",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                TAG,
                0);
        Event selectedEvent = new Event(
                "test organizer",
                "none",
                "event for user on the selected list for profile delete test",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                TAG,
                0);
        Event attendingEvent = new Event(
                "test organizer",
                "none",
                "event for user on the attending list for profile delete test",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                TAG,
                0);
        Event declinedEvent = new Event(
                "test organizer",
                "none",
                "event for user on the declined list for profile delete test",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                TAG,
                0);
        Event cancelledEvent = new Event(
                "test organizer",
                "none",
                "event for user on the cancelled list for profile delete test",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                TAG,
                0);
        Event removedEvent = new Event(
                "test organizer",
                "none",
                "event for user on the removed list for profile delete test",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                TAG,
                0);

        setUpEvent(noneEvent);

        setUpEvent(waitingEvent);
        RegistrationListTestsSupport.setUpWaitingList(waitingEvent.registrationList, user.getDeviceId());
        RegistrationListTestsSupport.checkSingle(waitingEvent.registrationList, user.getDeviceId());

        setUpEvent(selectedEvent);
        RegistrationListTestsSupport.setUpSelectedList(selectedEvent.registrationList, user.getDeviceId());
        RegistrationListTestsSupport.checkSingle(selectedEvent.registrationList, user.getDeviceId());

        setUpEvent(attendingEvent);
        RegistrationListTestsSupport.setUpAttendingList(attendingEvent.registrationList, user.getDeviceId());
        RegistrationListTestsSupport.checkSingle(attendingEvent.registrationList, user.getDeviceId());

        setUpEvent(declinedEvent);
        RegistrationListTestsSupport.setUpDeclinedList(declinedEvent.registrationList, user.getDeviceId());
        RegistrationListTestsSupport.checkSingle(declinedEvent.registrationList, user.getDeviceId());

        setUpEvent(cancelledEvent);
        RegistrationListTestsSupport.setUpCancelledList(cancelledEvent.registrationList, user.getDeviceId());
        RegistrationListTestsSupport.checkSingle(cancelledEvent.registrationList, user.getDeviceId());

        setUpEvent(removedEvent);
        RegistrationListTestsSupport.setUpRemovedList(removedEvent.registrationList, user.getDeviceId());
        RegistrationListTestsSupport.checkSingle(removedEvent.registrationList, user.getDeviceId());

        //TODO: Make sure all the notifications are there

        // Test
        // Navigate to profile screen
        onView(withId(R.id.nav_profile)).perform(click());
        // Click delete profile button
        onView(withId(R.id.delete_profile_button)).perform(click());
        //TODO: Make sure the fields are empty
        //TODO: Make sure user isn't on registration lists (`getAllLists`)
        //TODO: Make sure there's no notifications

        // Take down
        takeDownEvent(noneEvent);
        takeDownEvent(waitingEvent);
        takeDownEvent(selectedEvent);
        takeDownEvent(attendingEvent);
        takeDownEvent(declinedEvent);
        takeDownEvent(cancelledEvent);
        takeDownEvent(removedEvent);
    }
}
