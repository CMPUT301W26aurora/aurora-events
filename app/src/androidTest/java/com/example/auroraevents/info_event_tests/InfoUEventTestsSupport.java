package com.example.auroraevents.info_event_tests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import com.example.auroraevents.R;

public class InfoUEventTestsSupport {
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
            onView(withId(id)).check(doesNotExist());
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
