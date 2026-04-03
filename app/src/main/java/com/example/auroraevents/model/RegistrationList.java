package com.example.auroraevents.model;

import static com.example.auroraevents.server.EventDb.LIST_ATTENDING;
import static com.example.auroraevents.server.EventDb.LIST_CANCELLED;
import static com.example.auroraevents.server.EventDb.LIST_DECLINED;
import static com.example.auroraevents.server.EventDb.LIST_REMOVED;
import static com.example.auroraevents.server.EventDb.LIST_SELECTED;
import static com.example.auroraevents.server.EventDb.LIST_WAITING;

import android.util.Log;

import com.example.auroraevents.server.EventDb;
import com.example.auroraevents.server.UserDb;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class RegistrationList {
    private String eventId;
    private final List<String> waitingList;     // signed up, awaiting lottery
    private final List<String> selectedList;    // drawn / invited but not yet confirmed
    private final List<String> attendingList;   // confirmed attendees
    private final List<String> declinedList;    // invited then self declined
    private final List<String> cancelledList;   // self cancelled
    private final List<String> removedList;     // force removed
    private int attendingCapacity;
    private int waitingCapacity;
    private Integer databaseTimeout = 10;
    private TimeUnit timeoutUnit = TimeUnit.SECONDS;

    public RegistrationList() {
        waitingList = new ArrayList<>();
        selectedList = new ArrayList<>();
        attendingList = new ArrayList<>();
        declinedList = new ArrayList<>();
        cancelledList = new ArrayList<>();
        removedList = new ArrayList<>();
    }

    public RegistrationList(int databaseTimeout, TimeUnit timeoutUnit) {
        this();
        this.databaseTimeout = databaseTimeout;
        this.timeoutUnit = timeoutUnit;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RegistrationList that = (RegistrationList) o;
        Log.d("RegistrationList", eventId + " vs. " + that.eventId);
        return
                new HashSet<>(getWaitingList()).containsAll(that.getWaitingList()) &&
                new HashSet<>(getSelectedList()).containsAll(that.getSelectedList()) &&
                new HashSet<>(getAttendingList()).containsAll(that.getAttendingList()) &&
                new HashSet<>(getDeclinedList()).containsAll(that.getDeclinedList()) &&
                new HashSet<>(getCancelledList()).containsAll(that.getCancelledList()) &&
                new HashSet<>(getRemovedList()).containsAll(that.getRemovedList()) &&
                getAttendingCapacity() == that.getAttendingCapacity() &&
                getWaitingCapacity() == that.getWaitingCapacity();
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
        Log.d("RegistrationList", "set event ID to: " + eventId);
    }

    public int getAttendingCapacity() {
        return attendingCapacity;
    }
    public void setAttendingCapacity(int attendingCapacity) {
        this.attendingCapacity = attendingCapacity;
    }

    public int getWaitingCapacity() {
        return waitingCapacity;
    }
    public void setWaitingCapacity(int waitingCapacity) {
        this.waitingCapacity = waitingCapacity;
    }

    public interface OnDbUpdateListener{
        void onSuccess();
        void onFailure();
        void onComplete(int status);
    }

    public Integer getDatabaseTimeout() {
        return databaseTimeout;
    }
    public void setDatabaseTimeout(Integer databaseTimeout) {
        this.databaseTimeout = databaseTimeout;
    }

    public TimeUnit getTimeoutUnit() {
        return timeoutUnit;
    }
    public void setTimeoutUnit(TimeUnit unit) {
        timeoutUnit = unit;
    }

    /**
     * Changes the list that the user is on in the database.
     *
     * @param fromFieldName The list that the user is currently on, and will be removed from. (null to only add)
     * @param toFieldName   The list that the user will be put on to (null to only remove)
     * @param userID        The device ID of the user
     * @param listener      Listener for handling success and failure
     * @author Jared Strandlund, Sean Ross
     */
    private void changeDb(String fromFieldName, String toFieldName, String userID, OnDbUpdateListener listener) {
        if ((toFieldName == null) && (fromFieldName == null)) return;

        if (fromFieldName == null) {
            EventDb.getInstance().addUserToList(eventId, toFieldName, userID,
                    () -> listener.onSuccess(),
                    e -> listener.onFailure()
            );
        } else if (toFieldName == null) {
            EventDb.getInstance().removeUserFromList(eventId, fromFieldName, userID,
                    () -> listener.onSuccess(),
                    e -> listener.onFailure()
            );
        } else {
            EventDb.getInstance().moveUserBetweenLists(eventId, fromFieldName, toFieldName, userID,
                    () -> listener.onSuccess(),
                    e -> listener.onFailure()
            );
        }
    }

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
     * @param listener The callback listener to handle results
     *
     * <p><b>Status Codes:</b></p>
     * <ul>
     * <li>{@code 0}: Success - User moved to waiting.</li>
     * <li>{@code 1}: Failure - User is selected, attending, or blocked.</li>
     * <li>{@code 2}: Failure - Database or network error occurred.</li>
     * <li>{@code 3}: Failure - Waiting Capacity has already been reached.</li>
     * <li>{@code -1}: Failure - User is already in the selected list.</li>
     * </ul>
     * @author Sean Ross, Jared Strandlund
     */
    public void addToWaitingList(String userID, OnDbUpdateListener listener) {
        if (waitingCapacity > -1 && waitingList.size() >= waitingCapacity) {
            listener.onComplete(3);
            return;
        }else if (selectedList.contains(userID) || attendingList.contains(userID) || removedList.contains(userID)){
            listener.onComplete(1);
            return;
        }else if (waitingList.contains(userID)){
            listener.onComplete(-1);
            return;
        }

        String fromList = null;
        if(cancelledList.contains(userID)) {fromList =LIST_CANCELLED;}
        if(declinedList.contains(userID)) {fromList = LIST_DECLINED;}

        final String finalFromList = fromList;
        changeDb(finalFromList, LIST_WAITING, userID, new OnDbUpdateListener() {
            @Override
            public void onSuccess() {
                waitingList.add(userID);
                listener.onComplete(0);
            }
            @Override
            public void onFailure() {
                listener.onComplete(2);
            }
            @Override
            public void onComplete(int status) { /* Not used here */ }
        });
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
     * @param listener The callback listener to handle operation result
     * <p><b>Status Codes:</b></p>
     * <ul>
     * <li>{@code 0}: Success - User moved from waiting to selected.</li>
     * <li>{@code 1}: Failure - User was not in the waiting list.</li>
     * <li>{@code 2}: Failure - Database or network error occurred.</li>
     * <li>{@code 3}: Failure - Attending capacity has been reached.</li>
     * <li>{@code -1}: Failure - User is already in the selected list.</li>
     * </ul>
     * @author Sean Ross, Jared Strandlund
     */
    public void addToSelectedList(String userID, OnDbUpdateListener listener) {

        if(attendingCapacity > -1 && attendingList.size() >= attendingCapacity) {
            listener.onComplete(3);
            return;
        }else if(selectedList.contains(userID)){
            listener.onComplete(-1);
            return;
        }else if (removedList.contains(userID)){
            listener.onComplete(1);
        }

        changeDb(LIST_WAITING, LIST_SELECTED, userID, new OnDbUpdateListener() {
            @Override
            public void onSuccess(){
                waitingList.remove(userID);
                selectedList.add(userID);
                listener.onComplete(0);
            }
            @Override
            public void onFailure(){
                listener.onComplete(2);
            }
            @Override
            public void onComplete(int status){/*do nothing*/}
        });

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
     * @return
     *     {@code 0} when successful add
     *     {@code -1} when already on list
     *     {@code 1} when already on blocking list
     *     {@code 2} when database change fails
     * @author Jared Strandlund
     */
    public int addToAttendingList(String userID) {
        if (selectedList.remove(userID)) {
            boolean status = changeDb(LIST_SELECTED, LIST_ATTENDING, userID);
            if (status) {
                attendingList.add(userID);
                return 0;
            } else {
                selectedList.add(userID);
                return 2;
            }
        } else {
            if (attendingList.contains(userID))
                return -1;
            else
                return 1;
        }
    }

    /**
     * Add all the specified entrant device IDs to the attending list.
     * Does nothing if the entrant is not on the selected list.
     *
     * @param userIDs The entrants' device IDs
     * @return
     *     {@code 0} when successful add
     *     {@code -1} when already on list
     *     {@code 1} when already on blocking list
     *     {@code 2} when database change fails
     * @author Jared Strandlund
     */
    public List<Integer> addAllToAttendingList(List<String> userIDs) {
        int size = userIDs.size();
        List<String> ids = new ArrayList<>(size);
        ids.addAll(userIDs);
        List<Integer> output = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            output.add(i, addToAttendingList(ids.get(i)));
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
     * @return
     *     {@code 0} when successful add
     *     {@code -1} when already on list
     *     {@code 1} when already on blocking list
     *     {@code 2} when database change fails
     * @author Jared Strandlund
     */
    public int addToDeclinedList(String userID) {
        if (selectedList.remove(userID)) {
            boolean status = changeDb(LIST_SELECTED, LIST_DECLINED, userID);
            if (status) {
                declinedList.add(userID);
                return 0;
            } else {
                selectedList.add(userID);
                return 2;
            }
        } else {
            if (declinedList.contains(userID))
                return -1;
            else
                return 1;
        }
    }

    /**
     * Add all the specified entrant device IDs to the declined list.
     * Does nothing if the entrant is not on the selected list.
     *
     * @param userIDs The entrants' device IDs
     * @return
     *     {@code 0} when successful add
     *     {@code -1} when already on list
     *     {@code 1} when already on blocking list
     *     {@code 2} when database change fails
     * @author Jared Strandlund
     */
    public List<Integer> addAllToDeclinedList(List<String> userIDs) {
        int size = userIDs.size();
        List<String> ids = new ArrayList<>(size);
        ids.addAll(userIDs);
        List<Integer> output = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            output.add(i, addToDeclinedList(ids.get(i)));
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
     * @return
     *     {@code 0} when successful add
     *     {@code -1} when already on list
     *     {@code 1} when already on blocking list
     *     {@code 2} when database change fails
     * @author Jared Strandlund
     */
    public int addToCancelledList(String userID) {
        if (waitingList.remove(userID)) {
            boolean status = changeDb(LIST_WAITING, LIST_CANCELLED, userID);
            if (status) {
                cancelledList.add(userID);
                return 0;
            } else {
                waitingList.add(userID);
                return 2;
            }
        } else {
            if (cancelledList.contains(userID))
                return -1;
            else
                return 1;
        }
    }

    /**
     * Add all the specified entrant device IDs to the cancelled list.
     * Does nothing if the entrant is not on the waiting list.
     *
     * @param userIDs The entrants' device IDs
     * @return
     *     {@code 0} when successful add
     *     {@code -1} when already on list
     *     {@code 1} when already on blocking list
     *     {@code 2} when database change fails
     * @author Jared Strandlund
     */
    public List<Integer> addAllToCancelledList(List<String> userIDs) {
        int size = userIDs.size();
        List<String> ids = new ArrayList<>(size);
        ids.addAll(userIDs);
        List<Integer> output = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            output.add(i, addToCancelledList(ids.get(i)));
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
     * @return
     *     {@code 0} when successful add
     *     {@code -1} when already on list
     *     {@code 1} when already on blocking list
     *     {@code 2} when database change fails
     * @author Jared Strandlund
     */
    public int addToRemovedList(String userID) {
        if (waitingList.remove(userID))
            if (!changeDb(LIST_WAITING, null, userID)) {
                waitingList.add(userID);
                return 2;
            }
        if (selectedList.remove(userID))
            if (!changeDb(LIST_SELECTED, null, userID)) {
                selectedList.add(userID);
                return 2;
            }
        if (attendingList.remove(userID))
            if (!changeDb(LIST_ATTENDING, null, userID)) {
                attendingList.add(userID);
                return 2;
            }
        if (declinedList.remove(userID))
            if (!changeDb(LIST_DECLINED, null, userID)) {
                declinedList.add(userID);
                return 2;
            }
        if (cancelledList.remove(userID))
            if (!changeDb(LIST_CANCELLED, null, userID)) {
                cancelledList.add(userID);
                return 2;
            }

        if (removedList.contains(userID)) {
            return -1;
        } else {
            removedList.add(userID);
            boolean status = changeDb(null, LIST_REMOVED, userID);
            return status ? 0 : 2;
        }
    }

    /**
     * Add all the specified entrant device IDs to the removed list (will be blocked from being added to any other entrant list).
     *
     * @param userIDs The entrants' device IDs
     * @return
     *     {@code 0} when successful add
     *     {@code -1} when already on list
     *     {@code 1} when already on blocking list
     *     {@code 2} when database change fails
     * @author Jared Strandlund
     */
    public List<Integer> addAllToRemovedList(List<String> userIDs) {
        int size = userIDs.size();
        List<String> ids = new ArrayList<>(size);
        ids.addAll(userIDs);
        List<Integer> output = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            output.add(i, addToRemovedList(ids.get(i)));
        }
        return output;
    }

    /**
     * Remove the specified entrant device ID from the removed list (the entrant will be able to be added to entrant lists).
     * Does nothing if the entrant is not on the removed list.
     *
     * @param userID The entrant's device ID
     * @return
     *     {@code 0} when successful add
     *     {@code -1} when not on list
     *     {@code 2} when database change fails
     * @author Jared Strandlund
     */
    public int removeFromRemovedList(String userID) {
        if (removedList.remove(userID)) {
            if (!changeDb(LIST_REMOVED, null, userID)) {
                removedList.add(userID);
                return 2;
            } else return 0;
        } else return -1;
    }

    /**
     * Returns a list of device IDs of entrants on any entrant list.
     *
     * @return The list of all entrant device IDs
     */
    @Exclude
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

    /**
     * Removes the specified user from all the lists (for when the user is being deleted)
     * @param userID The entrant's device ID
     * @return List of statuses.
     *     <p> Status: </p>
     *     <p> - {@code 0} when user removed from the list successfully </p>
     *     <p> - {@code -1} when not on the list </p>
     *     <p> - {@code 2} when database error </p>
     *     <br>
     *     <p> Index {@code 0}: max. of all list statuses</p>
     *     <p> Index {@code 1}: waiting list status</p>
     *     <p> Index {@code 2}: selected list status</p>
     *     <p> Index {@code 3}: attending list status</p>
     *     <p> Index {@code 4}: declined list status</p>
     *     <p> Index {@code 5}: cancelled list status</p>
     *     <p> Index {@code 5}: removed list status</p>
     */
    public List<Integer> removeFromAllLists(String userID) {
        List<Integer> output = new ArrayList<>(7);
        output.add(0, Integer.MIN_VALUE);

        if (waitingList.remove(userID)) {
            if (!changeDb(LIST_WAITING, null, userID)) {
                waitingList.add(userID);
                output.add(1, 2);
            } else
                output.add(1, 0);
        } else
            output.add(1, -1);
        if (selectedList.remove(userID)) {
            if (!changeDb(LIST_SELECTED, null, userID)) {
                selectedList.add(userID);
                output.add(2, 2);
            } else
                output.add(2, 0);
        } else
            output.add(2, -1);
        if (attendingList.remove(userID)) {
            if (!changeDb(LIST_ATTENDING, null, userID)) {
                attendingList.add(userID);
                output.add(3, 2);
            } else
                output.add(3, 0);
        } else
            output.add(3, -1);
        if (declinedList.remove(userID)) {
            if (!changeDb(LIST_DECLINED, null, userID)) {
                declinedList.add(userID);
                output.add(4, 2);
            } else
                output.add(4, 0);
        } else
            output.add(4, -1);
        if (cancelledList.remove(userID)) {
            if (!changeDb(LIST_CANCELLED, null, userID)) {
                cancelledList.add(userID);
                output.add(5, 2);
            } else
                output.add(5, 0);
        } else
            output.add(5, -1);
        if (removedList.remove(userID)) {
            if (!changeDb(LIST_REMOVED, null, userID)) {
                removedList.add(userID);
                output.add(6, 2);
            } else
                output.add(6, 0);
        } else
            output.add(6, -1);

        output.set(0, Collections.max(output));
        return output;
    }

    // ── Sampling ──────────────────────────────────────────────────
    /**
     * Connects and fetches user objects from database using their device IDs and returns an array list of them
     * @author Won Koh & Jared Strandlund
     * @param listOfDeviceIDs
     * The list of user's device IDs
     * @return
     * The list of user objects that were fetched with given device IDs
     */
    @Exclude
    public List<User> getUsersFromDB(List<String> listOfDeviceIDs) {
        ArrayList<User> listOfUsers = new ArrayList<>();
        // Fetch users from database
        for (String userId : listOfDeviceIDs) {
            UserDb.getInstance().getUser(userId,
                    u -> {
                        if (u != null) {
                            listOfUsers.add(u);
                        }
                    },
                    e -> {
                        Log.e("Main", "Error fetching user", e);
                    }
            );
        }
        return listOfUsers;
    }

    /**
     * Connects and fetches user objects from database using their device IDs and returns an array list of them
     * @author Won Koh & Jared Strandlund
     * @param listName
     * The name of the list of user's device IDs (one of LIST_ATTENDING, LIST_SELECTED, LIST_WAITING, LIST_CANCELLED, LIST_DECLINED, LIST_REMOVED)
     * @return
     * The list of user objects that were fetched with given device IDs
     */
    public List<User> getUsersFromDB(String listName) {
        if (Objects.equals(listName, LIST_WAITING)) {
            return getUsersFromDB(getWaitingList());
        } else if (Objects.equals(listName, LIST_SELECTED)) {
            return getUsersFromDB(getSelectedList());
        } else if (Objects.equals(listName, LIST_ATTENDING)) {
            return getUsersFromDB(getAttendingList());
        } else if (Objects.equals(listName, LIST_DECLINED)) {
            return getUsersFromDB(getDeclinedList());
        } else if (Objects.equals(listName, LIST_CANCELLED)) {
            return getUsersFromDB(getCancelledList());
        } else if (Objects.equals(listName, LIST_REMOVED)) {
            return getUsersFromDB(getRemovedList());
        } else
            return new ArrayList<>();
    }

    // ── Sampling ──────────────────────────────────────────────────
    /**
     * Returns the amount of empty slots that is available in the event
     * @return
     * Amount of empty slots available (-1 is infinite)
     */
    @Exclude
    public int getEmptySlotAmount() {
        if (getAttendingCapacity() < 0)
            return -1;
        return getAttendingCapacity() - getAttendingList().size() - getSelectedList().size();
    }

    /**
     * Randomly samples users in the waiting list and adds the selected ones to the selected list
     * then send notification to both the users who were selected and not
     * @author Won Koh & Jared Strandlund
     */
    @Exclude
    public void randomSampling(int amount, int capacity) {
        // There are more empty slots than there are users in waiting list: Select everyone from waiting list
        // Also do the same if the capacity = 0 (This is when there is no limit)
        if (capacity == 0 || amount >= waitingList.size()) {
            addAllToSelectedList(waitingList);
        }
        else { // Random sampling
            Random random = new Random();
            int limit = Math.min(amount, capacity);
            for (int i = 0; i < limit; i++) {
                // Generate random index using the waitingList size (waiting list will shrink so this will prevent index out of bounds)
                int randomIndex = random.nextInt(waitingList.size());
                String selectedUserID = waitingList.get(randomIndex);
                addToSelectedList(selectedUserID);
            }
        }
    }

    /**
     * Randomly samples users in the waiting list and adds the selected ones to the selected list
     * then send notification to both the users who were selected and not
     */
    @Exclude
    public void randomSampling() {
        randomSampling(getEmptySlotAmount(), getAttendingCapacity());
    }
}
