package com.example.auroraevents;

/**
 * @author Jared Strandlund
 * @version 1
 * @see WaitingList
 */
public class WaitingListItem {
    private final Entrant entrant;
    private WaitingList.WaitingListStatus status;

    /**
     * Constructor to add an entrant with a specified status
     * @author Jared Strandlund
     * @param entrant
     *     The entrant this item tracks
     * @param status
     *     The initial status of the entrant
     */
    public WaitingListItem (Entrant entrant, WaitingList.WaitingListStatus status) {
        this.entrant = entrant;
        this.status = status;
    }

    /**
     * Get the entrant this item tracks
     * @author Jared Strandlund
     * @return
     *     The entrant this item tracks
     */
    public Entrant getEntrant () {
        return entrant;
    }

    /**
     * Get the current status of this entrant
     * @author Jared Strandlund
     * @return
     *     The current status
     */
    public WaitingList.WaitingListStatus getStatus () {
        return status;
    }

    /**
     * Set the status
     * @author Jared Strandlund
     * @param status
     *     The new status
     * @return
     *     0 on success
     */
    public int setStatus (WaitingList.WaitingListStatus status) {
        this.status = status;
        return 0;
    }
}
