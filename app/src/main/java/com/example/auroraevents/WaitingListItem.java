package com.example.auroraevents;

public class WaitingListItem {
    private Entrant entrant;
    private WaitingList.WaitingListStatus status;

    public WaitingListItem (Entrant entrant) {
        this.entrant = entrant;
        status = WaitingList.WaitingListStatus.WAITING;
    }

    public WaitingListItem (Entrant entrant, WaitingList.WaitingListStatus status) {
        this.entrant = entrant;
        this.status = WaitingList.WaitingListStatus.WAITING;
    }

    public Entrant getEntrant () {
        return entrant;
    }

    public WaitingList.WaitingListStatus getStatus () {
        return status;
    }

    public int setStatus (WaitingList.WaitingListStatus status) {
        this.status = status;
        return 0;
    }
}
