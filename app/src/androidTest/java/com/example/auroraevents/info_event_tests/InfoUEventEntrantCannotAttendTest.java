//package com.example.auroraevents.info_event_tests;
//
//import static com.example.auroraevents.TestsSupport.setUpEvent;
//import static com.example.auroraevents.TestsSupport.signIn;
//import static com.example.auroraevents.TestsSupport.takeDownEvent;
//import static com.example.auroraevents.info_event_tests.InfoUEventTestsSupport.bottomBarShowTest;
//import static com.example.auroraevents.info_event_tests.InfoUEventTestsSupport.openEvent;
//import static org.junit.Assert.fail;
//
//import androidx.test.ext.junit.rules.ActivityScenarioRule;
//
//import com.example.auroraevents.MainActivity;
//import com.example.auroraevents.model.Event;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Rule;
//import org.junit.Test;
//
//import java.time.LocalDateTime;
//
//public class InfoUEventEntrantCannotAttendTest {
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
//
//    }
//
//    @After
//    public void after() {
//        takeDownEvent(event);
//    }
//
//    /**
//     * Tests that when the user is on the removed list, the bottom bar only shows:
//     *     - cannot_attend_label
//     * @author Jared Strandlund
//     */
//    @Test
//    public void onRemovedList() {
//        // Set up
//        event = new Event(
//                "dummy",
//                "event info screen test",
//                "event for info screen cannot attend test",
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
//        //TODO: add to removed list
//        fail("TODO: add to removed list");
//
//        openEvent(scenario, event);
//
//        // Test
//        bottomBarShowTest(
//                false,
//                false,
//                false,
//                false,
//                false,
//                false,
//                false,
//                true,
//                false
//        );
//    }
//
//    /**
//     * Tests that when there are no spots on the attending/selected list, the bottom bar only shows:
//     *     - attendees_count
//     *     - cannot_attend_label
//     *     - lottery_info_button
//     * @author Jared Strandlund
//     */
//    @Test
//    public void noAttendingCapacity() {
//        // Set up
//        event = new Event(
//                "dummy",
//                "event info screen test",
//                "event for info screen cannot attend test",
//                "free",
//                LocalDateTime.now().plusDays(2),
//                LocalDateTime.now().minusDays(1),
//                LocalDateTime.now().plusDays(1),
//                "testing environment",
//                false,
//                -1,
//                0,
//                null);
//        setUpEvent(event);
//
//        openEvent(scenario, event);
//
//        // Test
//        bottomBarShowTest(
//                false,
//                false,
//                true,
//                false,
//                false,
//                false,
//                false,
//                true,
//                true
//        );
//    }
//
//    /**
//     * Tests that when there is no room on the waiting list, the bottom bar only shows:
//     *     - waiting_list_count
//     *     - attendees_count
//     *     - cannot_attend_label
//     *     - lottery_info_button
//     * @author Jared Strandlund
//     */
//    @Test
//    public void noWaitingCapacity() {
//        // Set up
//        event = new Event(
//                "dummy",
//                "event info screen test",
//                "event for info screen cannot attend test",
//                "free",
//                LocalDateTime.now().plusDays(2),
//                LocalDateTime.now().minusDays(1),
//                LocalDateTime.now().plusDays(1),
//                "testing environment",
//                false,
//                0,
//                -1,
//                null);
//        setUpEvent(event);
//
//        openEvent(scenario, event);
//
//        // Test
//        bottomBarShowTest(
//                false,
//                true,
//                true,
//                false,
//                false,
//                false,
//                false,
//                true,
//                true
//        );
//    }
//
//    /**
//     * Tests that when the event registration end time has passed, the bottom bar only shows:
//     *     - event_deadline
//     *     - cannot_attend_label
//     *     - lottery_info_button
//     * @author Jared Strandlund
//     */
//    @Test
//    public void registrationClosed() {
//        // Set up
//        event = new Event(
//                "dummy",
//                "event info screen test",
//                "event for info screen cannot attend test",
//                "free",
//                LocalDateTime.now().plusDays(2),
//                LocalDateTime.now().minusDays(2),
//                LocalDateTime.now().minusDays(1),
//                "testing environment",
//                false,
//                -1,
//                -1,
//                null);
//        setUpEvent(event);
//
//        openEvent(scenario, event);
//
//        // Test
//        bottomBarShowTest(
//                true,
//                false,
//                false,
//                false,
//                false,
//                false,
//                false,
//                true,
//                true
//        );
//    }
//}
