//package com.example.auroraevents.info_event_tests;
//
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.action.ViewActions.click;
//import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
//import static androidx.test.espresso.assertion.ViewAssertions.matches;
//import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
//import static androidx.test.espresso.matcher.ViewMatchers.withId;
//import static androidx.test.espresso.matcher.ViewMatchers.withText;
//import static com.example.auroraevents.TestsSupport.setUpEvent;
//import static com.example.auroraevents.TestsSupport.signIn;
//import static com.example.auroraevents.TestsSupport.takeDownEvent;
//import static com.example.auroraevents.info_event_tests.InfoUEventTestsSupport.openEvent;
//import static org.hamcrest.CoreMatchers.not;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.fail;
//
//import androidx.fragment.app.Fragment;
//import androidx.test.ext.junit.rules.ActivityScenarioRule;
//
//import com.example.auroraevents.MainActivity;
//import com.example.auroraevents.R;
//import com.example.auroraevents.model.Event;
//import com.example.auroraevents.view.InfoUEventFragment;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Rule;
//import org.junit.Test;
//
//import java.time.LocalDateTime;
//
//public class InfoUEventAdminTest {
//    Event event;
//
//    @Rule
//    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);
//
//    @BeforeClass
//    public static void prepare() {
//        signIn();
//    }
//
//    @Before
//    public void before() {
//        String eventName = "event info screen test";
//
//        event = new Event(
//                "dummy",
//                eventName,
//                "event for info screen admin general test",
//                "free",
//                LocalDateTime.now().plusDays(2),
//                LocalDateTime.now().minusDays(1),
//                LocalDateTime.now().plusDays(1),
//                "testing environment",
//                false,
//                -1,
//                -1,
//                null);
//        setUpEvent(event);
//
//        //TODO: make user admin
//        fail("TODO: make user admin");
//
//        openEvent(scenario, event);
//    }
//
//    @After
//    public void after() {
//        takeDownEvent(event);
//    }
//
//    /**
//     * Tests if the cancel button in the delete dialog closes it
//     * @author Jared Strandlund
//     */
//    @Test
//    public void reportDialogCancel() {
//        onView(withId(R.id.reported_num)).check(matches(isDisplayed()));
//        onView(withText(event.getNumReports())).check(matches(isDisplayed()));
//        onView(withId(R.id.delete_button)).check(matches(isDisplayed()));
//        onView(withId(R.id.bottom_bar)).check(matches(not(isDisplayed())));
//
//        onView(withId(R.id.delete_button)).perform(click());
//        onView(withText(R.string.permanent_warning_title)).check(matches(isDisplayed()));
//        onView(withText(R.string.permanent_warning_description)).check(matches(isDisplayed()));
//
//        onView(withId(R.id.permanent_warning_back_button)).perform(click());
//        onView(withText(R.string.report_dialog_title)).check(doesNotExist());
//        onView(withText(R.string.report_dialog_list)).check(doesNotExist());
//    }
//
//    /**
//     * Tests if the delete button in the delete dialog closes it and the event
//     * @author Jared Strandlund
//     */
//    @Test
//    public void reportDialogReport() {
//        onView(withId(R.id.reported_num)).check(matches(isDisplayed()));
//        onView(withText(event.getNumReports())).check(matches(isDisplayed()));
//        onView(withId(R.id.delete_button)).check(matches(isDisplayed()));
//        onView(withId(R.id.bottom_bar)).check(matches(not(isDisplayed())));
//
//        onView(withId(R.id.delete_button)).perform(click());
//        onView(withText(R.string.permanent_warning_title)).check(matches(isDisplayed()));
//        onView(withText(R.string.permanent_warning_description)).check(matches(isDisplayed()));
//
//        onView(withId(R.id.permanent_warning_delete_button)).perform(click());
//        onView(withText(R.string.report_dialog_title)).check(doesNotExist());
//        onView(withText(R.string.report_dialog_list)).check(doesNotExist());
//
//        scenario.getScenario().onActivity(activity -> {
//            Fragment current = activity.getSupportFragmentManager()
//                    .findFragmentById(R.id.fragment_container);
//            assertFalse(current instanceof InfoUEventFragment);
//        });
//    }
//}
