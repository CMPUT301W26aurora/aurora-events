package com.example.auroraevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
     * @author Jared Strandlund
     */
    @Test
    public void constructorTest() {
        assertEquals(new ArrayList<Entrant>(), waitingList.getEntrants());
        assertNull(waitingList.getStatus(new Entrant("Becky Hopper", "beckhop@yahoo.ca")));
    }

    /**
     * Tests join(), getEntrants(), and getStatus()
     * @author Jared Strandlund
     */
    @Test
    public void joinTest() {
        Entrant entrant = new Entrant("Robert", "rob@bank.org");
        assertEquals(0, waitingList.join(entrant));
        assertEquals(1, waitingList.getEntrants().size());
        assertEquals(0, waitingList.getEntrants().indexOf(entrant));
        assertEquals(entrant, waitingList.getEntrants().get(0));
        assertEquals(WaitingList.WaitingListStatus.WAITING, waitingList.getStatus(entrant));
    }

    /**
     * Tests leave(), getEntrants(), and getStatus()
     * @author Jared Strandlund
     */
    @Test
    public void leaveTest() {
        Entrant entrant = new Entrant("Robert", "rob@bank.org");
        assertEquals(0, waitingList.join(entrant));
        assertEquals(0, waitingList.leave(entrant));
        assertEquals(1, waitingList.getEntrants().size());
        assertEquals(0, waitingList.getEntrants().indexOf(entrant));
        assertEquals(entrant, waitingList.getEntrants().get(0));
        assertEquals(WaitingList.WaitingListStatus.SELF_LEFT, waitingList.getStatus(entrant));
    }

    /**
     * Tests remove(), getEntrants(), and getStatus()
     * @author Jared Strandlund
     */
    @Test
    public void removeTest() {
        Entrant entrant = new Entrant("Robert", "rob@bank.org");
        assertEquals(0, waitingList.join(entrant));
        assertEquals(0, waitingList.remove(entrant));
        assertEquals(1, waitingList.getEntrants().size());
        assertEquals(0, waitingList.getEntrants().indexOf(entrant));
        assertEquals(entrant, waitingList.getEntrants().get(0));
        assertEquals(WaitingList.WaitingListStatus.FORCE_LEFT, waitingList.getStatus(entrant));
    }

    /**
     * Tests invite(), getEntrants(), and getStatus()
     * @author Jared Strandlund
     */
    @Test
    public void inviteTest() {
        Entrant entrant = new Entrant("Robert", "rob@bank.org");
        assertEquals(0, waitingList.join(entrant));
        assertEquals(0, waitingList.invite(entrant));
        assertEquals(1, waitingList.getEntrants().size());
        assertEquals(0, waitingList.getEntrants().indexOf(entrant));
        assertEquals(entrant, waitingList.getEntrants().get(0));
        assertEquals(WaitingList.WaitingListStatus.INVITED, waitingList.getStatus(entrant));
    }

    /**
     * Tests accept(), getEntrants(), and getStatus()
     * @author Jared Strandlund
     */
    @Test
    public void acceptTest() {
        Entrant entrant = new Entrant("Robert", "rob@bank.org");
        assertEquals(0, waitingList.join(entrant));
        assertEquals(0, waitingList.invite(entrant));
        assertEquals(0, waitingList.accept(entrant));
        assertEquals(1, waitingList.getEntrants().size());
        assertEquals(0, waitingList.getEntrants().indexOf(entrant));
        assertEquals(entrant, waitingList.getEntrants().get(0));
        assertEquals(WaitingList.WaitingListStatus.ACCEPTED, waitingList.getStatus(entrant));
    }

    /**
     * Tests decline(), getEntrants(), and getStatus()
     * @author Jared Strandlund
     */
    @Test
    public void declineTest() {
        Entrant entrant = new Entrant("Robert", "rob@bank.org");
        assertEquals(0, waitingList.join(entrant));
        assertEquals(0, waitingList.invite(entrant));
        assertEquals(0, waitingList.decline(entrant));
        assertEquals(1, waitingList.getEntrants().size());
        assertEquals(0, waitingList.getEntrants().indexOf(entrant));
        assertEquals(entrant, waitingList.getEntrants().get(0));
        assertEquals(WaitingList.WaitingListStatus.DECLINED, waitingList.getStatus(entrant));
    }

    public void addEntrants(WaitingList list) {
        //TODO: add entrants with different statuses
    }

    /**
     * Tests getEntrants(WaitingListStatus.WAITING)
     * @author Jared Strandlund
     */
    @Test
    public void getWaitingEntrants() {
        addEntrants(waitingList);
        ArrayList<Entrant> waitingEntrants = waitingList.getEntrants(WaitingList.WaitingListStatus.WAITING);
        ArrayList<Entrant> allEntrants = waitingList.getEntrants();

        // ensure all entrants have proper status
        for (Entrant entrant : waitingEntrants) {
            assertEquals(WaitingList.WaitingListStatus.WAITING, waitingList.getStatus(entrant));
        }

        // ensure all pertinent entrants are included
        for (Entrant entrant : allEntrants) {
            if (waitingList.getStatus(entrant) == WaitingList.WaitingListStatus.WAITING) {
                waitingEntrants.add(entrant);
                assertTrue(waitingEntrants.contains(entrant));
            }
        }
    }

    /**
     * Tests getEntrants(WaitingListStatus.SELF_LEFT)
     * @author Jared Strandlund
     */
    @Test
    public void getLeaveEntrants() {
        addEntrants(waitingList);
        ArrayList<Entrant> waitingEntrants = waitingList.getEntrants(WaitingList.WaitingListStatus.SELF_LEFT);
        ArrayList<Entrant> allEntrants = waitingList.getEntrants();

        // ensure all entrants have proper status
        for (Entrant entrant : waitingEntrants) {
            assertEquals(WaitingList.WaitingListStatus.SELF_LEFT, waitingList.getStatus(entrant));
        }

        // ensure all pertinent entrants are included
        for (Entrant entrant : allEntrants) {
            if (waitingList.getStatus(entrant) == WaitingList.WaitingListStatus.SELF_LEFT) {
                waitingEntrants.add(entrant);
                assertTrue(waitingEntrants.contains(entrant));
            }
        }
    }

    /**
     * Tests getEntrants(WaitingListStatus.FORCE_LEFT)
     * @author Jared Strandlund
     */
    @Test
    public void getRemoveEntrants() {
        addEntrants(waitingList);
        ArrayList<Entrant> waitingEntrants = waitingList.getEntrants(WaitingList.WaitingListStatus.FORCE_LEFT);
        ArrayList<Entrant> allEntrants = waitingList.getEntrants();

        // ensure all entrants have proper status
        for (Entrant entrant : waitingEntrants) {
            assertEquals(WaitingList.WaitingListStatus.FORCE_LEFT, waitingList.getStatus(entrant));
        }

        // ensure all pertinent entrants are included
        for (Entrant entrant : allEntrants) {
            if (waitingList.getStatus(entrant) == WaitingList.WaitingListStatus.FORCE_LEFT) {
                waitingEntrants.add(entrant);
                assertTrue(waitingEntrants.contains(entrant));
            }
        }
    }

    /**
     * Tests getEntrants(WaitingListStatus.INVITED)
     * @author Jared Strandlund
     */
    @Test
    public void getInvitedEntrants() {
        addEntrants(waitingList);
        ArrayList<Entrant> waitingEntrants = waitingList.getEntrants(WaitingList.WaitingListStatus.INVITED);
        ArrayList<Entrant> allEntrants = waitingList.getEntrants();

        // ensure all entrants have proper status
        for (Entrant entrant : waitingEntrants) {
            assertEquals(WaitingList.WaitingListStatus.INVITED, waitingList.getStatus(entrant));
        }

        // ensure all pertinent entrants are included
        for (Entrant entrant : allEntrants) {
            if (waitingList.getStatus(entrant) == WaitingList.WaitingListStatus.INVITED) {
                waitingEntrants.add(entrant);
                assertTrue(waitingEntrants.contains(entrant));
            }
        }
    }

    /**
     * Tests getEntrants(WaitingListStatus.ACCEPTED)
     * @author Jared Strandlund
     */
    @Test
    public void getAcceptedEntrants() {
        addEntrants(waitingList);
        ArrayList<Entrant> waitingEntrants = waitingList.getEntrants(WaitingList.WaitingListStatus.ACCEPTED);
        ArrayList<Entrant> allEntrants = waitingList.getEntrants();

        // ensure all entrants have proper status
        for (Entrant entrant : waitingEntrants) {
            assertEquals(WaitingList.WaitingListStatus.ACCEPTED, waitingList.getStatus(entrant));
        }

        // ensure all pertinent entrants are included
        for (Entrant entrant : allEntrants) {
            if (waitingList.getStatus(entrant) == WaitingList.WaitingListStatus.ACCEPTED) {
                waitingEntrants.add(entrant);
                assertTrue(waitingEntrants.contains(entrant));
            }
        }
    }

    /**
     * Tests getEntrants(WaitingListStatus.DECLINED)
     * @author Jared Strandlund
     */
    @Test
    public void getDeclinedEntrants() {
        addEntrants(waitingList);
        ArrayList<Entrant> waitingEntrants = waitingList.getEntrants(WaitingList.WaitingListStatus.DECLINED);
        ArrayList<Entrant> allEntrants = waitingList.getEntrants();

        // ensure all entrants have proper status
        for (Entrant entrant : waitingEntrants) {
            assertEquals(WaitingList.WaitingListStatus.DECLINED, waitingList.getStatus(entrant));
        }

        // ensure all pertinent entrants are included
        for (Entrant entrant : allEntrants) {
            if (waitingList.getStatus(entrant) == WaitingList.WaitingListStatus.DECLINED) {
                waitingEntrants.add(entrant);
                assertTrue(waitingEntrants.contains(entrant));
            }
        }
    }
}
