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
import java.util.List;
import java.util.Objects;
import java.util.Random;

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

    public List<String> getSelectedList(){return selectedList;}

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
    private void transitionUser(String userId,
                                List<String> toList,
                                String toName,
                                List<String> fromList,
                                String fromName,
                                int capacity,
                                OnDbUpdateListener listener){
        if (capacity > -1 && toList.size() >= capacity){
            listener.onComplete(RegistrationResult.CAPACITY_FULL);
            return;
        }

        if(toList.contains(userId)){
            listener.onComplete(RegistrationResult.ALREADY_IN_LIST);
            return;
        }
        changeDb(fromName, toName, userId, new OnDbUpdateListener() {
            @Override
            public void onSuccess() {
                if (fromList != null) fromList.remove(userId);
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
                                 List<String> fromList,
                                 String fromName,
                                 List<String> toList,
                                 String toName,
                                 int capacity,
                                 OnDbUpdateListener listener) {

        if (capacity > -1 && (toList.size() + userIDs.size()) > capacity) {
            listener.onComplete(RegistrationResult.CAPACITY_FULL);
            return;
        }
        EventDb.getInstance().moveGroupUsers(eventId, fromName, toName, userIDs,
                () -> {
                    fromList.removeAll(userIDs);
                    toList.addAll(userIDs);
                    listener.onComplete(RegistrationResult.SUCCESS);
                },
                e -> listener.onComplete(RegistrationResult.DATABASE_ERROR)
        );
    }

    public void addToSelectedList(String userID, OnDbUpdateListener listener) {
        if (attendingList.contains(userID) || removedList.contains(userID)) {
            listener.onComplete(RegistrationResult.BLOCKED);
            return;
        }
        transitionUser(userID, selectedList, LIST_SELECTED, waitingList, LIST_WAITING, -1, listener);
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
        if (!selectedList.contains(userID)) {
            listener.onComplete(RegistrationResult.BLOCKED);
            return;
        }
        transitionUser(userID, attendingList, LIST_ATTENDING,selectedList,LIST_SELECTED , getAttendingCapacity(), listener);
    }

    public void addToWaitingList(String userID, OnDbUpdateListener listener){
        if(attendingList.contains(userID) || selectedList.contains(userID) || removedList.contains(userID) || waitingList.contains(userID)){
            listener.onComplete(RegistrationResult.BLOCKED);
            return;
        }
        transitionUser(userID, waitingList, LIST_WAITING, null, null, getWaitingCapacity(), listener);
    }

    public void addToDeclinedList(String userID, OnDbUpdateListener listener){
        if(!selectedList.contains(userID) || removedList.contains(userID)){
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
}
