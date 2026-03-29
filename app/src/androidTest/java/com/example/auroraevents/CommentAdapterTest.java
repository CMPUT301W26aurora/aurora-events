package com.example.auroraevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

import android.Manifest;
import android.os.Build;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import com.example.auroraevents.model.Comment;
import com.example.auroraevents.model.CommentAdapter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapterTest {
    private CommentAdapter adapter;
    private List<Comment> testComments;

    @Before
    public void setup() {
        testComments = new ArrayList<>();
        testComments.add(new Comment("Parent", "u1", null, "id1", "e1", null, "User1"));
        testComments.add(new Comment("Reply", "u2", "id1", "id2", "e1", null, "User2"));

        adapter = new CommentAdapter(testComments, null, "wow", "entrant", "oof");
    }

    /**
     * Tests comment creation and position in the list
     */
    @Test
    public void testCommentPos(){
        assertEquals("User1", testComments.get(0).getUsername());
    }
    /**
     * Tests the ItemCount()
     */
    @Test
    public void testItemCount() {
        assertEquals(2, adapter.getItemCount());
    }

}
