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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RegistrationList {
    private String eventId;
    private final List<String> waitingList;     // signed up, awaiting lottery
    private final List<SelectedUser> selectedList;    // drawn / invited but not yet confirmed
    private final List<String> attendingList;   // confirmed attendees
    private final List<String> declinedList;    // invited then self declined
    private final List<String> cancelledList;   // self cancelled
    private final List<String> removedList;     // force removed
    private int attendingCapacity;
    private int waitingCapacity;

    public RegistrationList() {
        waitingList = new ArrayList<>();
        selectedList = new ArrayList<>();
        attendingList = new ArrayList<>();
        declinedList = new ArrayList<>();
        cancelledList = new ArrayList<>();
        removedList = new ArrayList<>();
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
        Log.d("RegistrationList", "set event ID to: " + eventId);
    }


    //--Getters and Setters------------------------------------------------------------------------------------------
    public int getAttendingCapacity() {return attendingCapacity;}
    public void setAttendingCapacity(int attendingCapacity) {this.attendingCapacity = attendingCapacity;}
    public int getWaitingCapacity() {return waitingCapacity;}
    public void setWaitingCapacity(int waitingCapacity) {this.waitingCapacity = waitingCapacity;}

    public List<SelectedUser> getSelectedList(){return selectedList;}

    public List<String> getRemovedList(){return removedList;}

    public List<String> getWaitingList(){return waitingList;}

    public List<String> getAttendingList(){return attendingList;}

    public List<String> getDeclinedList(){return declinedList;}

    public List<String> getCancelledList(){return cancelledList;}
    public enum RegistrationResult {
        SUCCESS,
        ALREADY_IN_LIST,
        BLOCKED,
        DATABASE_ERROR,
        CAPACITY_FULL
    }

    /**
     * Interface for listeners
     */
    public interface OnDbUpdateListener{
        void onSuccess();
        void onFailure();
        void onComplete(RegistrationResult result);
    }
    //-- Add user functions ------------------------------------------------------------------------------------------
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
     * A function that acts a base template to move users over to another list, in practice we never group
     * move a single person from waiting to selected without calling group transition, so toList is always a
     * List of strings, so we can ignore the case of it being a group transition of selected users.
     *
     * @param userId
     * @param toList
     * @param toName
     * @param fromList
     * @param fromName
     * @param capacity
     * @param listener
     */
    private void transitionUser(String userId,
                                List<String> toList,
                                String toName,
                                Object fromList,
                                String fromName,
                                int capacity,
                                OnDbUpdateListener listener){
        int currentSize = (toList instanceof List) ? ((List<?>) toList).size() : 0;
        if (capacity > -1 && currentSize >= capacity){
            listener.onComplete(RegistrationResult.CAPACITY_FULL);
            return;
        }

        if(toName.equals(fromName)){
            listener.onComplete(RegistrationResult.ALREADY_IN_LIST);
            return;
        }
        changeDb(fromName, toName, userId, new OnDbUpdateListener() {
            @Override
            public void onSuccess() {
                if (fromList instanceof List) {
                    if (fromName.equals(LIST_SELECTED)) {
                        selectedList.removeIf(u -> u.getUserId().equals(userId));
                    } else {
                        ((List<String>) fromList).remove(userId);
                    }
                }

                toList.add(userId);
                listener.onComplete(RegistrationResult.SUCCESS);
            }
            @Override
            public void onFailure() {
                listener.onComplete(RegistrationResult.DATABASE_ERROR);
            }
            @Override
            public void onComplete(RegistrationResult result) {/*do nothing*/}
        });

    }
    private void transitionGroup(List<String> userIDs,
                                 Object fromList,
                                 String fromName,
                                 Object toList,
                                 String toName,
                                 int capacity,
                                 OnDbUpdateListener listener) {
        int currentSize = (toList instanceof List) ? ((List<?>) toList).size() : 0;
        if (capacity > -1 && (currentSize + userIDs.size()) > capacity) {
            listener.onComplete(RegistrationResult.CAPACITY_FULL);
            return;
        }
        EventDb.getInstance().moveGroupUsers(eventId, fromName, toName, userIDs,
                () -> {
                    if (fromList instanceof List) {
                        List<?> list = (List<?>) fromList;
                        if (!list.isEmpty() && list.get(0) instanceof SelectedUser) {
                            removeFromSelected((List<SelectedUser>) fromList, userIDs);
                        } else {
                            ((List<String>) fromList).removeAll(userIDs);
                        }
                    }
                    if(toList instanceof List) {
                        if(toName.equals(LIST_SELECTED)){
                            List<SelectedUser> wrappedIds = wrapIdList(userIDs);

                            ((List<SelectedUser>) toList).addAll(wrappedIds);

                        }else{
                            ((List<String>) toList).addAll(userIDs);
                        }
                    }

                    listener.onComplete(RegistrationResult.SUCCESS);
                },
                e -> listener.onComplete(RegistrationResult.DATABASE_ERROR)
        );
    }


    public void addToRemovedList(String userID, OnDbUpdateListener listener, List<String> fromList, String fromName) {
        if (removedList.contains(userID)) {
            listener.onComplete(RegistrationResult.BLOCKED);
            return;
        }
        transitionUser(userID,removedList, LIST_REMOVED,  fromList,fromName , -1, listener);
    }

    public void addToCancelledList(String userID, OnDbUpdateListener listener, List<String> fromList, String fromName) {
        if (removedList.contains(userID)) {
            listener.onComplete(RegistrationResult.BLOCKED);
            return;
        }
        transitionUser(userID,cancelledList, LIST_CANCELLED, fromList,fromName, -1, listener);
    }

    public void addToAttendingList(String userID, OnDbUpdateListener listener){
        if (!getSelectedUsers().contains(userID)) {
            listener.onComplete(RegistrationResult.BLOCKED);
            return;
        }
        transitionUser(userID, attendingList, LIST_ATTENDING,selectedList,LIST_SELECTED , getAttendingCapacity(), listener);
    }

    public void addToWaitingList(String userID, OnDbUpdateListener listener){
        if(attendingList.contains(userID) || getSelectedUsers().contains(userID) || removedList.contains(userID) || waitingList.contains(userID)){
            listener.onComplete(RegistrationResult.BLOCKED);
            return;
        }
        transitionUser(userID, waitingList, LIST_WAITING, null, null, getWaitingCapacity(), listener);
    }

    public void addToDeclinedList(String userID, OnDbUpdateListener listener){
        if(!getSelectedUsers().contains(userID) || removedList.contains(userID)){
            listener.onComplete(RegistrationResult.BLOCKED);
            return;
        }
        transitionUser(userID, declinedList, LIST_DECLINED, selectedList, LIST_SELECTED, -1, listener);
    }
    public void reinstateUser(String userID, OnDbUpdateListener listener) {
        if (!removedList.contains(userID)) {
            listener.onComplete(RegistrationResult.BLOCKED);
            return;
        }
        transitionUser(userID, waitingList, LIST_WAITING, removedList, LIST_REMOVED, getWaitingCapacity(), listener);
    }

    // add firebase functionality for remove all

    //check userdB and add some more functionality maybe
    @Exclude
    public int getEmptySlotAmount() {
        if (getAttendingCapacity() < 0)
            return -1;
        return getAttendingCapacity() - getAttendingList().size() - getSelectedList().size();
    }
    //-- Sample -------------------------------------------------------------------------------------
    @Exclude
    public void performLottery(int amount, OnDbUpdateListener listener) {
        int limit = Math.min(amount, waitingList.size()); // I'm unsure about the lottery reqs, ask later...
        if (limit <= 0) {
            listener.onComplete(RegistrationResult.SUCCESS);
            return;
        }

        List<String> winners = new ArrayList<>();
        List<String> pool = new ArrayList<>(waitingList);
        Random random = new Random();

        for (int i = 0; i < limit; i++) {
            int index = random.nextInt(pool.size());
            winners.add(pool.remove(index));
        }

        transitionGroup(winners, waitingList, LIST_WAITING, selectedList, LIST_SELECTED, -1, listener);
    }

    public void removeFromAllLists(String userID, OnDbUpdateListener listener) {
        EventDb.getInstance().removeUserFromAllLists(eventId, userID,
                () -> {
                    waitingList.remove(userID);
                    selectedList.removeIf(user->user.getUserId().equals(userID));
                    attendingList.remove(userID);
                    declinedList.remove(userID);
                    cancelledList.remove(userID);
                    removedList.remove(userID);

                    listener.onComplete(RegistrationResult.SUCCESS);
                },
                e -> listener.onComplete(RegistrationResult.DATABASE_ERROR)
        );
    }

    /**
     * Another Helper function to wrap a set of ids, used in the perform lottery
     * @param userIDs
     * @return
     */
    private List<SelectedUser> wrapIdList(List<String> userIDs) {
        List<SelectedUser> wrappedList = new ArrayList<>();
        Date now = new Date(); // Single timestamp for the whole batch

        for (String id : userIDs) {
            wrappedList.add(new SelectedUser(id, now));
        }
        return wrappedList;
    }

    /**
     * Another Helper function to unwrap a set of ids, used in contains calls
     * @return
     */
    private List<String> getSelectedUsers(){
        List<String> sel = new ArrayList<>();
        for(SelectedUser user : selectedList){
            sel.add(user.getUserId());
        }
        return sel;
    }

    //https://www.geeksforgeeks.org/java/arraylist-removeif-method-in-java/
    /**
     * A filter method that removes elements from a list based on a conditional
     *
     * @param list the selected list to be removed
     * @param idsToRemove the set of ids to remove
     */
    private void removeFromSelected(List<SelectedUser> list, List<String> idsToRemove) {
        list.removeIf(user -> idsToRemove.contains(user.getUserId()));
    }


}
