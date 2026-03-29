package com.example.auroraevents.info_event_tests;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.example.auroraevents.TestsSupport.setUpEvent;
import static com.example.auroraevents.TestsSupport.signIn;
import static com.example.auroraevents.TestsSupport.takeDownEvent;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.auroraevents.MainActivity;
import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class InfoUEventEntrantJoinTest {
    /*TODO:
    - no list shows:
            - event_deadline
            - waiting_list_count
            - attendees_count
            - join_button
            - lottery_info_button
    - cancelled shows:
            - event_deadline
            - waiting_list_count
            - attendees_count
            - join_button
            - lottery_info_button
    - declined shows:
            - event_deadline
            - waiting_list_count
            - attendees_count
            - join_button
            - lottery_info_button
    - join_button joins
     */

    Event event;

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void prepare() {
        signIn();
    }

    @Before
    public void before() {
        String eventName = "event info screen test";

        event = new Event(
                "dummy",
                eventName,
                "event for info screen attending test",
                "free",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                "testing environment",
                false,
                -1,
                -1,
                null);
        setUpEvent(event);

        onView(withId(R.id.nav_browse)).perform(click());
        onData(is(equalTo(event))).inAdapterView(withId(R.id.events_list)).perform(click());
        try {
            new CountDownLatch(1).await(1, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}
        onView(withText(eventName)).check(matches(isDisplayed()));
    }

    @After
    public void after() {
        takeDownEvent(event);
    }

    /**
     * Checks if the info button popup works
     * @author Jared Strandlund
     */
    @Test
    public void infoButton() {
        onView(withId(R.id.lottery_info_button)).check(matches(isDisplayed()));
        onView(withId(R.id.lottery_info_button)).perform(click());
        onView(withText(R.string.lottery_info_title)).check(matches(isDisplayed()));
        onView(withText(R.string.lottery_info_description)).check(matches(isDisplayed()));
        onView(withText(R.string.okay_button_text)).perform(click());
        onView(withText(R.string.lottery_info_title)).check(doesNotExist());
        onView(withText(R.string.lottery_info_description)).check(doesNotExist());
    }
}
