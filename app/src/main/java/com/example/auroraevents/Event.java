package com.example.auroraevents;

import java.util.ArrayList;
import java.util.List;

public class Event {
    // Participant lists — each list holds device IDs (User.deviceId)
    private List<String> waitingList;     // signed up, awaiting lottery
    private List<String> selectedList;    // drawn / invited but not yet confirmed
    private List<String> attendingList;   // confirmed attendees
    private List<String> declinedList;    // invited then self declined
    private List<String> cancelledList;   // self cancelled
    private List<String> removedList;     // force removed

    public Event() {
        waitingList   = new ArrayList<>();
        selectedList  = new ArrayList<>();
        attendingList = new ArrayList<>();
        declinedList  = new ArrayList<>();
        cancelledList = new ArrayList<>();
        removedList   = new ArrayList<>();
    }

    /**
     * Returns a list of device IDs of entrants on the waiting list.
     * @return The waiting list of entrant device IDs
     */
    public List<String> getWaitingList()                               { return waitingList; }
    /**
     * Sets the waiting list to include only the provided entrant device IDs.
     * Please run {@code cleanLists()} when all the entrants are added.
     * @param waitingList
     *     List of entrant device IDs to set the waiting list to
     */
    public void         setWaitingList(List<String> waitingList)       { this.waitingList = waitingList; }
    /**
     * Add the specified entrant device ID to the waiting list.
     *     Does nothing if the entrant is already on the selected, attending, or removed lists.
     * @author Jared Strandlund
     * @param entrantID
     *     The entrant's device ID
     * @return {@code true} on success
     */
    public boolean      addToWaitingList(String entrantID)             {
        if (selectedList.contains(entrantID) || attendingList.contains(entrantID) || removedList.contains(entrantID) || declinedList.contains(entrantID) || waitingList.contains(entrantID))
            return false;
        else {
            cancelledList.remove(entrantID);
            waitingList.add(entrantID);
            return true;
        }
    }

    /**
     * Returns a list of device IDs of entrants on the selected list.
     * @return The selected list of entrant device IDs
     */
    public List<String> getSelectedList()                              { return selectedList; }
    /**
     * Sets the selected list to include only the provided entrant device IDs.
     * Please run {@code cleanLists()} when all the entrants are added.
     * @param selectedList
     *     List of entrant device IDs to set the selected list to
     */
    public void         setSelectedList(List<String> selectedList)     { this.selectedList = selectedList; }
    /**
     * Add the specified entrant device ID to the selected list.
     *     Does nothing if the entrant is not on the waiting list.
     * @author Jared Strandlund
     * @param entrantID
     *     The entrant's device ID
     * @return {@code true} on success
     */
    public boolean      addToSelectedList(String entrantID)            {
        if (waitingList.remove(entrantID)) {
            selectedList.add(entrantID);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a list of device IDs of entrants on the attending list.
     * @return The attending list of entrant device IDs
     */
    public List<String> getAttendingList()                             { return attendingList; }
    /**
     * Sets the attending list to include only the provided entrant device IDs.
     * Please run {@code cleanLists()} when all the entrants are added.
     * @param attendingList
     *     List of entrant device IDs to set the attending list to
     */
    public void         setAttendingList(List<String> attendingList)   { this.attendingList = attendingList; }
    /**
     * Add the specified entrant device ID to the attending list.
     *     Does nothing if the entrant is not on the selected list.
     * @author Jared Strandlund
     * @param entrantID
     *     The entrant's device ID
     * @return {@code true} on success
     */
    public boolean      addToAttendingList(String entrantID)           {
        if (selectedList.remove(entrantID)) {
            attendingList.add(entrantID);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a list of device IDs of entrants on the declined list.
     * @return The declined list of entrant device IDs
     */
    public List<String> getDeclinedList()                              { return declinedList; }
    /**
     * Sets the declined list to include only the provided entrant device IDs.
     * Please run {@code cleanLists()} when all the entrants are added.
     * @param declinedList
     *     List of entrant device IDs to set the declined list to
     */
    public void         setDeclinedList(List<String> declinedList)     { this.declinedList = declinedList; }
    /**
     * Add the specified entrant device ID to the declined list.
     *     Does nothing if the entrant is not on the selected list.
     * @author Jared Strandlund
     * @param entrantID
     *     The entrant's device ID
     * @return {@code true} on success
     */
    public boolean      addToDeclinedList(String entrantID)            {
        if (selectedList.remove(entrantID)) {
            declinedList.add(entrantID);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a list of device IDs of entrants on the cancelled list.
     * @return The cancelled list of entrant device IDs
     */
    public List<String> getCancelledList()                             { return cancelledList; }
    /**
     * Sets the cancelled list to include only the provided entrant device IDs.
     * Please run {@code cleanLists()} when all the entrants are added.
     * @param cancelledList
     *     List of entrant device IDs to set the cancelled list to
     */
    public void         setCancelledList(List<String> cancelledList)   { this.cancelledList = cancelledList; }
    /**
     * Add the specified entrant device ID to the cancelled list.
     *     Does nothing if the entrant is not on the waiting list.
     * @author Jared Strandlund
     * @param entrantID
     *     The entrant's device ID
     * @return {@code true} on success
     */
    public boolean      addToCancelledList(String entrantID)           {
        if (waitingList.remove(entrantID)) {
            cancelledList.add(entrantID);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a list of device IDs of entrants on the removed list.
     * @return The removed list of entrant device IDs
     */
    public List<String> getRemovedList()                               { return removedList; }
    /**
     * Sets the removed list to include only the provided entrant device IDs.
     * Please run {@code cleanLists()} when all the entrants are added.
     * @param removedList
     *     List of entrant device IDs to set the removed list to
     */
    public void         setRemovedList(List<String> removedList)       { this.removedList = removedList; }
    /**
     * Add the specified entrant device ID to the removed list (will be blocked from being added to any other entrant list).
     * @author Jared Strandlund
     * @param entrantID
     *     The entrant's device ID
     */
    public boolean      addToRemovedList(String entrantID)             {
        waitingList.remove(entrantID);
        selectedList.remove(entrantID);
        attendingList.remove(entrantID);
        declinedList.remove(entrantID);
        cancelledList.remove(entrantID);
        if (removedList.contains(entrantID)) {
            return false;
        } else {
            removedList.add(entrantID);
            return true;
        }
    }
    /**
     * Remove the specified entrant device ID from the removed list (the entrant will be able to be added to entrant lists).
     *      Does nothing if the entrant is not on the removed list.
     * @param entrantID
     * The entrant's device ID
     * @return {@code true} on success
     */
    public boolean      removeFromRemovedList(String entrantID)        { return removedList.remove(entrantID); }

    /**
     * Returns a list of device IDs of entrants on any entrant list.
     * @return The list of all entrant device IDs
     */
    public List<String> getAllEntrantsList()                           {
        List<String> output = new ArrayList<>();
        output.addAll(this.getAttendingList());
        output.addAll(this.getSelectedList());
        output.addAll(this.getWaitingList());
        output.addAll(this.getDeclinedList());
        output.addAll(this.getCancelledList());
        output.addAll(this.getRemovedList());

        return output;
    }

    /**
     * Removes duplicate entrants from the entrant lists.
     *      The priority of the lists is:
     *          removed > declined > attending > selected > waiting > cancelled.
     *      In other words, the removed list will remain unchanged.
     *          Entrants will be removed from the declined list if they're on the removed list.
     *          Entrants will be removed from the attending list if they're on the removed list or the declined list.
     *          And so on.
     * @author Jared Strandlund
     */
    public void         tidyLists()                                   {
        declinedList.removeAll(removedList);

        attendingList.removeAll(removedList);
        attendingList.removeAll(declinedList);

        selectedList.removeAll(removedList);
        selectedList.removeAll(declinedList);
        selectedList.removeAll(attendingList);

        waitingList.removeAll(removedList);
        waitingList.removeAll(declinedList);
        waitingList.removeAll(attendingList);
        waitingList.removeAll(selectedList);

        cancelledList.removeAll(removedList);
        cancelledList.removeAll(declinedList);
        cancelledList.removeAll(attendingList);
        cancelledList.removeAll(selectedList);
        cancelledList.removeAll(waitingList);
    }

}
