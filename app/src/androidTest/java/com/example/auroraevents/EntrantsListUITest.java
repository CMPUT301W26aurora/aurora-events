package com.example.auroraevents;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.example.auroraevents.TestsSupport.setUpEvent;
import static com.example.auroraevents.TestsSupport.setUpUser;
import static com.example.auroraevents.TestsSupport.signIn;
import static com.example.auroraevents.TestsSupport.takeDownEvent;
import static com.example.auroraevents.TestsSupport.takeDownUser;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

import android.os.Build;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.Organizer;
import com.example.auroraevents.model.User;
import com.example.auroraevents.registration_tests.RegistrationWaitingListTest;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class EntrantsListUITest {
    //Testing with Live database is a big no-no. Refactor with Mockito...
    /**
    Organizer organizer;
    Event myEvent;
    User user1;
    User user2;
    User user3;
    User user4;

    @BeforeClass
    public static void prepare() {
        signIn();
        RegistrationWaitingListTest.prepare();
    }

    @Before
    public void before() {
        myEvent = new Event(
                "OrganizerTestID",
                "entrants list test",
                "event for entrants list test",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                "testing environment",
                3);
        myEvent.setEventId("test event");
        setUpEvent(myEvent, 10, TimeUnit.SECONDS);

        organizer = new Organizer();
        organizer.setDeviceId("OrganizerTestID");

        // These 4 users will be used for the tests
        user1 = new User("TestID1","user1","email1","phone1","entrant");
        setUpUser(user1,10, TimeUnit.SECONDS);
        user2 = new User("TestID2","user2","email2","phone2","entrant");
        setUpUser(user2,10, TimeUnit.SECONDS);
        user3 = new User("TestID3","user3","email3","phone3","entrant");
        setUpUser(user3,10, TimeUnit.SECONDS);
        user4 = new User("TestID4","user4","email4","phone4","entrant");
        setUpUser(user4,10, TimeUnit.SECONDS);

        // Click on the created event
        onView(withId(R.id.nav_browse)).perform(click());
        onData(hasToString(containsString("entrants list test")))
                .inAdapterView(withId(R.id.events_list))
                .perform(click());
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        onView(withId(R.id.event_name)).check(matches(withText("entrants list test")));
    }

    @After
    public void after() {
        takeDownEvent(myEvent);
        takeDownUser(user1);
        takeDownUser(user2);
        takeDownUser(user3);
        takeDownUser(user4);
        takeDownUser(organizer);
    }

    @Test
    public void viewEntrantsTest() {

    }
    **/
}
