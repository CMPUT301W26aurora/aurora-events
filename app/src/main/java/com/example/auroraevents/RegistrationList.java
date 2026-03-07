package com.example.auroraevents;

import static com.example.auroraevents.EventDb.LIST_ATTENDING;
import static com.example.auroraevents.EventDb.LIST_CANCELLED;
import static com.example.auroraevents.EventDb.LIST_DECLINED;
import static com.example.auroraevents.EventDb.LIST_REMOVED;
import static com.example.auroraevents.EventDb.LIST_SELECTED;
import static com.example.auroraevents.EventDb.LIST_WAITING;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import kotlin.jvm.internal.Lambda;

public class RegistrationList {
    private String eventId;
    private final List<String> waitingList;     // signed up, awaiting lottery
    private final List<String> selectedList;    // drawn / invited but not yet confirmed
    private final List<String> attendingList;   // confirmed attendees
    private final List<String> declinedList;    // invited then self declined
    private final List<String> cancelledList;   // self cancelled
    private final List<String> removedList;     // force removed

    public RegistrationList() {
        waitingList = new ArrayList<>();
        selectedList = new ArrayList<>();
        attendingList = new ArrayList<>();
        declinedList = new ArrayList<>();
        cancelledList = new ArrayList<>();
        removedList = new ArrayList<>();
    }

    public RegistrationList(String eventId) {
        this();
        this.eventId = eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    private boolean changeDb(String fromFieldName, String toFieldName, String userID) {
        AtomicReference<Boolean> status = new AtomicReference<>(true);

        if ((toFieldName == null) && (fromFieldName == null)) return false;
        if (fromFieldName == null) {
            EventDb.getInstance().addUserToList(eventId, toFieldName, userID, null,
                    e -> status.set(false)
            );
        } else if (toFieldName == null) {
            EventDb.getInstance().removeUserFromList(eventId, fromFieldName, userID, null,
                    e -> status.set(false)
            );
        } else {
            EventDb.getInstance().moveUserBetweenLists(eventId, fromFieldName, toFieldName, userID, null,
                    e -> status.set(false)
            );
        }

        return status.get();
    }

    //TODO: add EventDb calls to all adders

    /**
     * Returns a list of device IDs of entrants on the waiting list.
     *
     * @return The waiting list of entrant device IDs
     */
    public List<String> getWaitingList() {
        return waitingList;
    }

    /**
     * Add the specified entrant device ID to the waiting list.
     * Does nothing if the entrant is already on the selected, attending, or removed lists.
     *
     * @param userID The entrant's device ID
     * @return {@code true} on success
     * @author Jared Strandlund
     */
    public boolean addToWaitingList(String userID) {
        if (selectedList.contains(userID) || attendingList.contains(userID) || removedList.contains(userID) || declinedList.contains(userID))
            return false;
        else if (waitingList.contains(userID))
            return true;
        else {
            int status = 0;
            if (cancelledList.remove(userID)) {
                if (changeDb(LIST_CANCELLED, LIST_WAITING, userID))
                    status = 1;
            } else {
                if (changeDb(null, LIST_WAITING, userID))
                    status = 2;
            }

            if (status == 0) {
                waitingList.add(userID);
                return true;
            } else {
                if (status == 1) cancelledList.add(userID);
                return false;
            }
        }
    }

    /**
     * Add all the specified entrant device IDs to the waiting list.
     * Does nothing if the entrant is already on the selected, attending, or removed lists.
     *
     * @param userIDs The entrants' device IDs
     * @return {@code true} on success
     * @author Jared Strandlund
     */
    public List<Boolean> addAllToWaitingList(List<String> userIDs) {
        List<Boolean> output = new ArrayList<>();
        for (int i = 0; i < userIDs.size(); i++) {
            output.set(i, addToWaitingList(userIDs.get(i)));
        }
        return output;
    }

    /**
     * Returns a list of device IDs of entrants on the selected list.
     *
     * @return The selected list of entrant device IDs
     */
    public List<String> getSelectedList() {
        return selectedList;
    }

    /**
     * Add the specified entrant device ID to the selected list.
     * Does nothing if the entrant is not on the waiting list.
     *
     * @param userID The entrant's device ID
     * @return {@code true} on success
     * @author Jared Strandlund
     */
    public boolean addToSelectedList(String userID) {
        if (waitingList.remove(userID)) {
            boolean status = changeDb(LIST_WAITING, LIST_SELECTED, userID);
            if (status) {
                selectedList.add(userID);
                return true;
            } else {
                waitingList.add(userID);
                return false;
            }
        } else return selectedList.contains(userID);
    }

    /**
     * Add all the specified entrant device IDs to the selected list.
     * Does nothing if the entrant is not on the waiting list.
     *
     * @param userIDs The entrants' device IDs
     * @return {@code true} on success
     * @author Jared Strandlund
     */
    public List<Boolean> addAllToSelectedList(List<String> userIDs) {
        List<Boolean> output = new ArrayList<>();
        for (int i = 0; i < userIDs.size(); i++) {
            output.set(i, addToSelectedList(userIDs.get(i)));
        }
        return output;
    }

    /**
     * Returns a list of device IDs of entrants on the attending list.
     *
     * @return The attending list of entrant device IDs
     */
    public List<String> getAttendingList() {
        return attendingList;
    }

    /**
     * Add the specified entrant device ID to the attending list.
     * Does nothing if the entrant is not on the selected list.
     *
     * @param userID The entrant's device ID
     * @return {@code true} on success
     * @author Jared Strandlund
     */
    public boolean addToAttendingList(String userID) {
        if (selectedList.remove(userID)) {
            boolean status = changeDb(LIST_SELECTED, LIST_ATTENDING, userID);
            if (status) {
                attendingList.add(userID);
                return true;
            } else {
                selectedList.add(userID);
                return false;
            }
        } else return attendingList.contains(userID);
    }

    /**
     * Add all the specified entrant device IDs to the attending list.
     * Does nothing if the entrant is not on the selected list.
     *
     * @param userIDs The entrants' device IDs
     * @return {@code true} on success
     * @author Jared Strandlund
     */
    public List<Boolean> addAllToAttendingList(List<String> userIDs) {
        List<Boolean> output = new ArrayList<>();
        for (int i = 0; i < userIDs.size(); i++) {
            output.set(i, addToAttendingList(userIDs.get(i)));
        }
        return output;
    }

    /**
     * Returns a list of device IDs of entrants on the declined list.
     *
     * @return The declined list of entrant device IDs
     */
    public List<String> getDeclinedList() {
        return declinedList;
    }

    /**
     * Add the specified entrant device ID to the declined list.
     * Does nothing if the entrant is not on the selected list.
     *
     * @param userID The entrant's device ID
     * @return {@code true} on success
     * @author Jared Strandlund
     */
    public boolean addToDeclinedList(String userID) {
        if (selectedList.remove(userID)) {
            boolean status = changeDb(LIST_SELECTED, LIST_DECLINED, userID);
            if (status) {
                declinedList.add(userID);
                return true;
            } else {
                selectedList.add(userID);
                return false;
            }
        } else return declinedList.contains(userID);
    }

    /**
     * Add all the specified entrant device IDs to the declined list.
     * Does nothing if the entrant is not on the selected list.
     *
     * @param userIDs The entrants' device IDs
     * @return {@code true} on success
     * @author Jared Strandlund
     */
    public List<Boolean> addAllToDeclinedList(List<String> userIDs) {
        List<Boolean> output = new ArrayList<>();
        for (int i = 0; i < userIDs.size(); i++) {
            output.set(i, addToDeclinedList(userIDs.get(i)));
        }
        return output;
    }

    /**
     * Returns a list of device IDs of entrants on the cancelled list.
     *
     * @return The cancelled list of entrant device IDs
     */
    public List<String> getCancelledList() {
        return cancelledList;
    }

    /**
     * Add the specified entrant device ID to the cancelled list.
     * Does nothing if the entrant is not on the waiting list.
     *
     * @param userID The entrant's device ID
     * @return {@code true} on success
     * @author Jared Strandlund
     */
    public boolean addToCancelledList(String userID) {
        if (waitingList.remove(userID)) {
            boolean status = changeDb(LIST_WAITING, LIST_CANCELLED, userID);
            if (status) {
                cancelledList.add(userID);
                return true;
            } else {
                waitingList.add(userID);
                return false;
            }
        } else return cancelledList.contains(userID);
    }

    /**
     * Add all the specified entrant device IDs to the cancelled list.
     * Does nothing if the entrant is not on the waiting list.
     *
     * @param userIDs The entrants' device IDs
     * @return {@code true} on success
     * @author Jared Strandlund
     */
    public List<Boolean> addAllToCancelledList(List<String> userIDs) {
        List<Boolean> output = new ArrayList<>();
        for (int i = 0; i < userIDs.size(); i++) {
            output.set(i, addToCancelledList(userIDs.get(i)));
        }
        return output;
    }

    /**
     * Returns a list of device IDs of entrants on the removed list.
     *
     * @return The removed list of entrant device IDs
     */
    public List<String> getRemovedList() {
        return removedList;
    }

    /**
     * Add the specified entrant device ID to the removed list (will be blocked from being added to any other entrant list).
     *
     * @param userID The entrant's device ID
     * @author Jared Strandlund
     */
    public boolean addToRemovedList(String userID) {
        if (waitingList.remove(userID))
            if (!changeDb(LIST_WAITING, null, userID)) {
                waitingList.add(userID);
                return false;
            }
        if (selectedList.remove(userID))
            if (!changeDb(LIST_SELECTED, null, userID)) {
                selectedList.add(userID);
                return false;
            }
        if (attendingList.remove(userID))
            if (!changeDb(LIST_ATTENDING, null, userID)) {
                attendingList.add(userID);
                return false;
            }
        if (declinedList.remove(userID))
            if (!changeDb(LIST_DECLINED, null, userID)) {
                declinedList.add(userID);
                return false;
            }
        if (cancelledList.remove(userID))
            if (!changeDb(LIST_CANCELLED, null, userID)) {
                cancelledList.add(userID);
                return false;
            }

        if (!removedList.contains(userID)) {
            removedList.add(userID);
        }
        return true;
    }

    /**
     * Add all the specified entrant device IDs to the removed list (will be blocked from being added to any other entrant list).
     *
     * @param userIDs The entrants' device IDs
     * @author Jared Strandlund
     */
    public List<Boolean> addAllToRemovedList(List<String> userIDs) {
        List<Boolean> output = new ArrayList<>();
        for (int i = 0; i < userIDs.size(); i++) {
            output.set(i, addToRemovedList(userIDs.get(i)));
        }
        return output;
    }

    /**
     * Remove the specified entrant device ID from the removed list (the entrant will be able to be added to entrant lists).
     * Does nothing if the entrant is not on the removed list.
     *
     * @param userID The entrant's device ID
     * @return {@code true} on success
     */
    public boolean removeFromRemovedList(String userID) {
        if (removedList.remove(userID)) {
            if (!changeDb(LIST_REMOVED, null, userID)) {
                removedList.add(userID);
                return false;
            } else return true;
        } else return false;
    }

    /**
     * Returns a list of device IDs of entrants on any entrant list.
     *
     * @return The list of all entrant device IDs
     */
    public List<String> getAllEntrantsList() {
        List<String> output = new ArrayList<>();
        output.addAll(this.getAttendingList());
        output.addAll(this.getSelectedList());
        output.addAll(this.getWaitingList());
        output.addAll(this.getDeclinedList());
        output.addAll(this.getCancelledList());
        output.addAll(this.getRemovedList());

        return output;
    }

//    /**
//     * Removes duplicate entrants from the entrant lists.
//     * The priority of the lists is:
//     * removed > declined > attending > selected > waiting > cancelled.
//     * In other words, the removed list will remain unchanged.
//     * Entrants will be removed from the declined list if they're on the removed list.
//     * Entrants will be removed from the attending list if they're on the removed list or the declined list.
//     * And so on.
//     * This is automatically enforced by the adders.
//     *
//     * @author Jared Strandlund
//     */
//    public void tidyLists() {
//        declinedList.removeAll(removedList);
//
//        attendingList.removeAll(removedList);
//        attendingList.removeAll(declinedList);
//
//        selectedList.removeAll(removedList);
//        selectedList.removeAll(declinedList);
//        selectedList.removeAll(attendingList);
//
//        waitingList.removeAll(removedList);
//        waitingList.removeAll(declinedList);
//        waitingList.removeAll(attendingList);
//        waitingList.removeAll(selectedList);
//
//        cancelledList.removeAll(removedList);
//        cancelledList.removeAll(declinedList);
//        cancelledList.removeAll(attendingList);
//        cancelledList.removeAll(selectedList);
//        cancelledList.removeAll(waitingList);
//    }
}
