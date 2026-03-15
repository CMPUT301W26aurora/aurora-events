package com.example.auroraevents;

import static com.example.auroraevents.TestsSupport.setUpEvent;
import static com.example.auroraevents.TestsSupport.signIn;
import static com.example.auroraevents.TestsSupport.takeDownEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.example.auroraevents.model.Event;
import com.example.auroraevents.server.EventDb;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class OrganizerEventCreationTest {
    private static final String ORGANIZER_ID = "device-abc-123";
    private static final String TITLE        = "Aurora Hackathon";
    private static final String DESCRIPTION  = "A fun coding competition.";
    private static final String DATE         = "2025-08-15";
    private static final String START_TIME   = "2025-07-01 09:00:00";
    private static final String END_TIME     = "2025-08-10 23:59:59";
    private static final String LOCATION     = "Edmonton Convention Centre";
    private static final int    CAPACITY     = 100;

    private Event myEvent;

    @BeforeClass
    public static void prepare() {
        signIn();
    }

    @Before
    public void setUp() {
        myEvent = testEvent();
        setUpEvent(myEvent, 60, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() {
        takeDownEvent(myEvent);
    }

    @Test
    public void testCreateEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> fetchedEvent = new AtomicReference<>();

        EventDb.getInstance().getEvent(myEvent.getEventId(),
                event -> {
                    fetchedEvent.set(event);
                    latch.countDown();
                },
                e -> {
                    fail("getEvent failed: " + e.getMessage());
                    latch.countDown();
                }
        );

        latch.await(10, TimeUnit.SECONDS);

        Event event = fetchedEvent.get();
        assertNotNull("Event should exist in the database", event);
        assertEquals(ORGANIZER_ID, event.getOrganizerDeviceId());
        assertEquals(TITLE,        event.getName());
        assertEquals(DESCRIPTION,  event.getDescription());
        assertEquals(LOCATION,     event.getLocation());
        assertEquals(CAPACITY,     event.getCapacity());
    }

    private Event testEvent() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return new Event(
                ORGANIZER_ID,
                TITLE,
                DESCRIPTION,
                LocalDate.parse(DATE).atStartOfDay(),
                LocalDateTime.parse(START_TIME, formatter),
                LocalDateTime.parse(END_TIME, formatter),
                LOCATION,
                CAPACITY
        );
    }
}