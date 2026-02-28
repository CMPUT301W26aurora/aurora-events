package com.example.auroraevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * WaitingList local unit test, which will execute on the development machine (host).
 *
 * @see WaitingList
 */
public class WaitingListTest {
    WaitingList waitingList;

    @Before
    public void before() {
        waitingList = new WaitingList();
    }

    /**
     * Tests the constructor, getEntrants(), and the null case of getStatus()
     */
    @Test
    public void constructorTest() {
        assertEquals(new ArrayList<Entrant>(), waitingList.getEntrants());
        assertNull(waitingList.getStatus(new Entrant("Becky Hopper", "beckhop@yahoo.ca")));
    }

    /**
     * Tests join(), getEntrants(), and getStatus()
     */
    @Test
    public void joinTest() {
        Entrant entrant = new Entrant("Robert", "rob@bank.org");
        waitingList.join(entrant);
        assertEquals(0, waitingList.getEntrants().indexOf(entrant));
        assertEquals(entrant, waitingList.getEntrants().get(0));
        assertEquals(WaitingList.WaitingListStatus.WAITING, waitingList.getStatus(entrant));
    }
}
