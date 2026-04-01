package com.example.auroraevents;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.example.auroraevents.TestsSupport.clickChildViewWithId;
import static com.example.auroraevents.TestsSupport.setUpEvent;
import static com.example.auroraevents.TestsSupport.setUpUser;
import static com.example.auroraevents.TestsSupport.signIn;
import static com.example.auroraevents.TestsSupport.takeDownEvent;
import static com.example.auroraevents.TestsSupport.takeDownUser;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasToString;

import static org.hamcrest.Matchers.allOf;

import android.Manifest;
import android.os.Build;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.User;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * tests the comment Fragment
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CommentFragmentTest {
    Event event;
    //rules so it works
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.CAMERA
    );

    @BeforeClass
    public static void prepare(){
        signIn();
    }

    @Before
    public void before(){
        onView(withId(R.id.nav_browse)).check(matches(isDisplayed()));
        event = new Event(
                "orgId",
                "extremeTurkeyTactics",
                "description",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                "Location",
                10
        );

        setUpEvent(event);

        onView(withId(R.id.nav_browse)).perform(click());
        onData(hasToString(containsString("extremeTurkeyTactics")))
                .inAdapterView(withId(R.id.events_list))
                .perform(click());
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        onView(allOf(withId(R.id.event_name),isDescendantOfA(isAssignableFrom(android.widget.ScrollView.class)))).check(matches(withText("extremeTurkeyTactics")));
        onView(withId(R.id.comment_button)).perform(click());
    }

    @After
    public void after() {
        if (event != null) {
            takeDownEvent(event);
        }

    }


    /**
     * tests if you can post a comment
     */
    @Test
    public void test02_testPostComment() {
        String testComment = "This is a test comment " + System.currentTimeMillis();

        onView(withId(R.id.edit_text_comment))
                .perform(typeText(testComment), pressImeActionButton(), closeSoftKeyboard());

        try { Thread.sleep(500); } catch (InterruptedException e) {}
        onView(withId(R.id.button_post)).perform(click());

        try { Thread.sleep(3500); } catch (InterruptedException e) {}
        onView(withId(R.id.recycler_view_comments))
                .check(matches(hasDescendant(withText(testComment))));


    }

    /**
     * tests bar visibility
     */
    @Test
    public void test01_barVisibility(){
        onView(withId(R.id.recycler_view_comments)).check(matches(isDisplayed()));
        onView(withId(R.id.nav_bar)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }


    /**
     * tests replying
     */
    @Test
    public void test03_testPostComment2() {
        String testComment = "This is a test comment " + System.currentTimeMillis();

        onView(withId(R.id.edit_text_comment))
                .perform(typeText(testComment), pressImeActionButton(), closeSoftKeyboard());

        try { Thread.sleep(500); } catch (InterruptedException e) {}
        onView(withId(R.id.button_post)).perform(click());

        try { Thread.sleep(3500); } catch (InterruptedException e) {}
        onView(withId(R.id.recycler_view_comments))
                .check(matches(hasDescendant(withText(testComment))));

        onView(withId(R.id.recycler_view_comments))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.comment_button_reply)));

        onView(withId(R.id.reply_indicator)).check(matches(isDisplayed()));
        onView(withId(R.id.text_replying_to)).check(matches(withText(containsString("Replying to @"))));

        String replyText = "This is a reply " + System.currentTimeMillis();
        onView(withId(R.id.edit_text_comment))
                .perform(typeText(replyText), closeSoftKeyboard());

        onView(withId(R.id.button_post)).perform(click());

        try { Thread.sleep(3500); } catch (InterruptedException e) {}
        onView(withId(R.id.recycler_view_comments))
                .check(matches(hasDescendant(withText(replyText))));

    }

    /**
     * tests canceling replies
     */
    @Test
    public void test04_cancelReply(){
        String testComment = "This is a test comment " + System.currentTimeMillis();

        onView(withId(R.id.edit_text_comment))
                .perform(typeText(testComment), pressImeActionButton(), closeSoftKeyboard());

        try { Thread.sleep(500); } catch (InterruptedException e) {}
        onView(withId(R.id.button_post)).perform(click());

        try { Thread.sleep(3500); } catch (InterruptedException e) {}
        onView(withId(R.id.recycler_view_comments))
                .check(matches(hasDescendant(withText(testComment))));

        onView(withId(R.id.recycler_view_comments))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.comment_button_reply)));

        onView(withId(R.id.reply_indicator)).check(matches(isDisplayed()));
        onView(withId(R.id.text_replying_to)).check(matches(withText(containsString("Replying to @"))));

        onView(withId(R.id.button_cancel_reply)).perform(click());
        onView(withId(R.id.reply_indicator)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

    }

    /**
     * tests post deletion
     */
    @Test
    public void test05_deletePost(){
        String testComment = "This is a test comment " + System.currentTimeMillis();

        onView(withId(R.id.edit_text_comment))
                .perform(typeText(testComment), pressImeActionButton(), closeSoftKeyboard());

        try { Thread.sleep(500); } catch (InterruptedException e) {}
        onView(withId(R.id.button_post)).perform(click());

        try { Thread.sleep(3500); } catch (InterruptedException e) {}
        onView(withId(R.id.recycler_view_comments))
                .check(matches(hasDescendant(withText(testComment))));

        onView(withId(R.id.recycler_view_comments))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.comment_button_delete)));
        onView(withText("Delete"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click());
        try { Thread.sleep(3000); } catch (InterruptedException e) {}
        onView(withText(testComment)).check(doesNotExist());
    }

}
