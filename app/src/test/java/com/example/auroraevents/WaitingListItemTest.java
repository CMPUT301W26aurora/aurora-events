package com.example.auroraevents;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * WaitingListItem local unit test, which will execute on the development machine (host).
 *
 * @see WaitingListItem
 */
public class WaitingListItemTest {
    /**
     * Tests the constructor, getEntrant(), and getStatus()
     */
    @Test
    public void constructorGet() {
        Entrant entrant = new Entrant("Alex Romanoff", "a.roman@gmail.com");
        WaitingList.WaitingListStatus status = WaitingList.WaitingListStatus.WAITING;
        WaitingListItem item = new WaitingListItem(entrant, status);
        assertEquals(entrant, item.getEntrant());
        assertEquals(status, item.getStatus());
    }

    /**
     * Tests setStatus()
     */
    @Test
    public void setStatus() {
        WaitingList.WaitingListStatus status1 = WaitingList.WaitingListStatus.WAITING;
        WaitingList.WaitingListStatus status2 = WaitingList.WaitingListStatus.SELF_LEFT;
        WaitingListItem item = new WaitingListItem(new Entrant("Jane Robinson", "jr@math.org"), status1);
        assertEquals(status1, item.getStatus());

        item.setStatus(status2);
        assertEquals(status2, item.getStatus());
    }
}
