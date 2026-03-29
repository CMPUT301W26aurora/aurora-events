package com.example.auroraevents.info_event_tests;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertTrue;

import androidx.fragment.app.Fragment;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.auroraevents.MainActivity;
import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.view.InfoUEventFragment;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class InfoUEventTestsSupport {
    public static void openEvent(ActivityScenarioRule<MainActivity> scenarioRule,  Event event) {
        onView(withId(R.id.nav_browse)).perform(click());
        onData(is(equalTo(event))).inAdapterView(withId(R.id.events_list)).perform(click());
        try {
            new CountDownLatch(1).await(1, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}

        // test if the event info fragment opened
        scenarioRule.getScenario().onActivity(activity -> {
            Fragment current = activity.getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            assertTrue(current instanceof InfoUEventFragment);
        });
        onView(withText(event.getName())).check(matches(isDisplayed()));
    }

    /**
     * Tests if the provided element is displayed on screen
     * @author Jared Strandlund
     * @param id The element id (e.g. `R.id.join_button`)
     * @param showed {@code true} if it should be displayed
     */
    public static void showTest(int id, boolean showed) {
        if (showed) {
            onView(withId(id)).check(matches(isDisplayed()));
        } else {
            onView(withId(id)).check(matches(not(isDisplayed())));
        }
    }

    /**
     * Tests if the correct elements of the bottom bar are on screen
     * <p> {@code true} if it should be displayed </p>
     * @author Jared Strandlund
     */
    public static void bottomBarShowTest(
            boolean event_deadline,
            boolean waiting_list_count,
            boolean attendees_count,
            boolean join_button,
            boolean leave_button,
            boolean select_button_set,
            boolean attending_label,
            boolean cannot_attend_label,
            boolean lottery_info_button
    ) {
        showTest(R.id.event_deadline, event_deadline);
        showTest(R.id.waiting_list_count, waiting_list_count);
        showTest(R.id.attendees_count, attendees_count);

        showTest(R.id.join_button, join_button);
        showTest(R.id.leave_button, leave_button);
        showTest(R.id.select_button_set, select_button_set);
        showTest(R.id.attending_label, attending_label);
        showTest(R.id.cannot_attend_label, cannot_attend_label);

        showTest(R.id.lottery_info_button, lottery_info_button);
    }
}
