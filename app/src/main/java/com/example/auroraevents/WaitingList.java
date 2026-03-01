package com.example.auroraevents;

import java.util.ArrayList;

/**
 * @author Jared Strandlund
 * @version 1
 * @see WaitingListItem
 */
public class WaitingList {
    /**
     * enum to keep track of the current status of an entrant
     */
    public enum WaitingListStatus {
        WAITING,
        SELF_LEFT,
        FORCE_LEFT,
        INVITED,
        DECLINED,
        ACCEPTED
    }

    private ArrayList<WaitingListItem> list;
    private ArrayList<Entrant> entrants;

    /**
     * Default constructor to initialize empty lists
     * @author Jared Strandlund
     */
    public WaitingList() {
        list = new ArrayList<>();
        entrants = new ArrayList<>();
    }

    /**
     * Get the list of all entrants
     * @author Jared Strandlund
     * @return
     *     list of all entrants
     */
    public ArrayList<Entrant> getEntrants() {
        return entrants;
    }

    /**
     * Get the list of all entrants with the specified status
     * @author Jared Strandlund
     * @return
     *     list of all entrants
     */
    public ArrayList<Entrant> getEntrants(WaitingListStatus status) {
        ArrayList<Entrant> output = new ArrayList<>();
        for (WaitingListItem item : list) {
            if (item.getStatus() == status) output.add(item.getEntrant());
        }
        return output;
    }

    /**
     * Get the WaitingListStatus of a specified entrant
     * @author Jared Strandlund
     * @param entrant
     *     the entrant to get the status for
     * @return
     *     the status of the entrant
     *     null if entrant not on waiting list
     */
    public WaitingListStatus getStatus(Entrant entrant) {
        int idx = entrants.indexOf(entrant);
        if (idx >= 0) { // in list
            return list.get(idx).getStatus();
        } else { // not in list
            return null;
        }
    }

    /**
     * Change the status of the specified entrant
     *     Adds the entrant to the waiting list if not already on it
     * @author Jared Strandlund
     * @param entrant
     *     The entrant to change/add the status
     * @param status
     *     The new status
     * @return
     *     0 if entrant already in list
     *     -1 if entrant not in list (added to list)
     *     1 if error
     */
    public int setStatus(Entrant entrant, WaitingListStatus status) {
        int idx = entrants.indexOf(entrant);
        if (idx >= 0) { // in list
            if (list.get(idx).setStatus(status) == 0) {
                return 0;
            } else {
                return 1;
            }
        } else if (idx == -1) { // not in list
            list.add(new WaitingListItem(entrant, status));
            entrants.add(entrant);
            return -1;
        } else { // error
            return 1;
        }
    }

    /**
     * Adds the entrant to the waiting list
     * US 01.01.01
     * @author Jared Strandlund
     * @param entrant
     *     The entrant to add
     * @return
     *     0 on success
     *     error code (>0) if error
     */
    public int join (Entrant entrant) {
        int status = setStatus(entrant, WaitingListStatus.WAITING);
        if (status > 0) { // error
            return status;
        } else { // no error
            return 0;
        }
    }

    /**
     * Removes the entrant from waiting list
     * US 01.01.02
     * @author Jared Strandlund
     * @param entrant
     *     The entrant that left
     * @return
     *     0 on success
     *     error code (>0) if error
     */
    public int leave (Entrant entrant) {
        int status = setStatus(entrant, WaitingListStatus.SELF_LEFT);
        if (status > 0) { // error
            return status;
        } else { // no error
            return 0;
        }
    }

    /**
     * Marks the entrant as accepted (attending)
     * US 01.05.02
     * @author Jared Strandlund
     * @param entrant
     *     The entrant that accepted
     * @return
     *     0 on success
     *     error code (>0) if error
     */
    public int accept (Entrant entrant) {
        int status = setStatus(entrant, WaitingListStatus.ACCEPTED);
        if (status > 0) { // error
            return status;
        } else { // no error
            return 0;
        }
    }

    /**
     * Marks the entrant as declined (not attending)
     * US 01.05.03
     * @author Jared Strandlund
     * @param entrant
     *     The entrant that declined
     * @return
     *     0 on success
     *     error code (>0) if error
     */
    public int decline (Entrant entrant) {
        int status = setStatus(entrant, WaitingListStatus.DECLINED);
        if (status > 0) { // error
            return status;
        } else { // no error
            return 0;
        }
    }

    /**
     * Forcibly removes the entrant from the waiting list
     * US 01.06.04
     * @author Jared Strandlund
     * @param entrant
     *     The entrant to remove
     * @return
     *     0 on success
     *     error code (>0) if error
     */
    public int remove (Entrant entrant) {
        int status = setStatus(entrant, WaitingListStatus.FORCE_LEFT);
        if (status > 0) { // error
            return status;
        } else { // no error
            return 0;
        }
    }

    /**
     * Marks the entrant as invited
     * US 02.05.02
     * @author Jared Strandlund
     * @param entrant
     *     The entrant that is invited
     * @return
     *     0 on success
     *     error code (>0) if error
     */
    public int invite (Entrant entrant) {
        int status = setStatus(entrant, WaitingListStatus.INVITED);
        if (status > 0) { // error
            return status;
        } else { // no error
            return 0;
        }
    }

}
