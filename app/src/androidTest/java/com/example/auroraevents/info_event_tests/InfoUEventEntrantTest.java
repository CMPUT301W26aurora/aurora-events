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

public class InfoUEventEntrantTest {
    Event event;

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void prepare() {
        signIn();
    }

    @Before
    public void before() {
        event = new Event(
                "dummy",
                "event info screen test",
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
        onView(withText(event.getName())).check(matches(isDisplayed()));
    }

    @After
    public void after() {
        takeDownEvent(event);
    }

    /**
     * Tests if the report button opens the report dialog
     * @author Jared Strandlund
     */
    @Test
    public void reportButton() {
        onView(withId(R.id.report_button)).check(matches(isDisplayed()));
        onView(withId(R.id.report_button)).perform(click());
        onView(withId(R.layout.fragment_report_confirm)).check(matches(isDisplayed()));
    }

    /**
     * Tests if the cancel button in the report dialog closes it
     * @author Jared Strandlund
     */
    @Test
    public void reportDialogCancel() {
        onView(withId(R.id.report_button)).perform(click());
        onView(withId(R.id.cancel_button)).perform(click());
        onView(withId(R.layout.fragment_report_confirm)).check(doesNotExist());
    }

    /**
     * Tests if the cancel button in the report dialog closes it
     * @author Jared Strandlund
     */
    @Test
    public void reportDialogReport() {
        onView(withId(R.id.report_button)).perform(click());
        onView(withId(R.id.report_button)).perform(click());
        onView(withId(R.layout.fragment_report_confirm)).check(doesNotExist());
    }
}
