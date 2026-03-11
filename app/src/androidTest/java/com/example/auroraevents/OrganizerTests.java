package com.example.auroraevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * This class contains tests for the Organizer functionality
 */
public class OrganizerTests {
    private static final String ORGANIZER_ID = "device-abc-123";
    private static final String TITLE        = "Aurora Hackathon";
    private static final String DESCRIPTION  = "A fun coding competition.";
    private static final String DATE         = "2025-08-15";
    private static final String TIME         = "14:00:00";
    private static final String START_TIME   = "2025-07-01 09:00:00";
    private static final String END_TIME     = "2025-08-10 23:59:59";
    private static final String LOCATION     = "Edmonton Convention Centre";
    private static final int    CAPACITY     = 100;
    Organizer organizer;

    @Before
    public void setUp() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.useEmulator("10.0.2.2", 8080);
    }

    /**
     * Tests event creation
     */
    @Test
    public void createEvent_thenGetEvent_returnsMatchingEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> fetchedEvent = new AtomicReference<>();
        AtomicReference<String> createdId = new AtomicReference<>();

        // Create test event
        EventDb.addEvent(testEvent(),
                id -> {
                    createdId.set(id);

                    // Fetch event using getEvent()
                    EventDb.getInstance().getEvent(id,
                            event -> {
                                fetchedEvent.set(event);
                                latch.countDown();
                            },
                            e -> {
                                fail("getEvent failed: " + e.getMessage());
                                latch.countDown();
                            }
                    );
                },
                e -> {
                    fail("addEvent failed: " + e.getMessage());
                    latch.countDown();
                }
        );

        latch.await(10, TimeUnit.SECONDS);

        // Check that fetched event matches
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

    @After
    public void clearEmulatorData() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/emulator/v1/projects/"
                        + "aurora-events"
                        + "/databases/(default)/documents")
                .delete()
                .build();
        client.newCall(request).execute();
    }
}