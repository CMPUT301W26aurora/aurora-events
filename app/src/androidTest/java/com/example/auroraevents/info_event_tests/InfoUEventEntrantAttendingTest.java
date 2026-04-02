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
//public class InfoUEventEntrantAttendingTest {
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
//                "event for info screen attending test",
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
//        //TODO: add user to attending list
//        fail("TODO: add user to attending list");
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
//     * Tests that when the user is on the attending list, the bottom bar shows only:
//     *     - attendees_count
//     *     - attending_label
//     * @author Jared Strandlund
//     */
//    @Test
//    public void showsElements() {
//        bottomBarShowTest(
//                false,
//                false,
//                true,
//                false,
//                false,
//                false,
//                true,
//                false,
//                false
//        );
//    }
//}
