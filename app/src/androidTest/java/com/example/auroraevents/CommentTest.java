package com.example.auroraevents;

import static org.junit.Assert.assertEquals;

import com.example.auroraevents.model.Comment;
import com.google.firebase.Timestamp;

import org.junit.Test;

/**
 * Tests comment class functions
 * @author Sean Ross
 */

public class CommentTest {
    @Test
    public void testConstructorAndGetters(){
        Timestamp now = Timestamp.now();
        Comment comment = new Comment("Hello", "user", "parent", "id", "event", now, "username");

        assertEquals("Hello", comment.getComment());
        assertEquals("user", comment.getUserId());
        assertEquals("parent", comment.getParentId());
        assertEquals("username", comment.getUsername());
        assertEquals(now, comment.getTimestamp());
    }

    @Test
    public void testGetTimeStampLong() {
        Timestamp specificTime = new Timestamp(111111, 0); // Example date
        Comment comment = new Comment();
        comment.setTimestamp(specificTime);

        long expectedMillis = specificTime.toDate().getTime();
        assertEquals(expectedMillis, comment.getTimeStampLong());
    }
}
