package com.example.auroraevents;

import java.util.ArrayList;

public class WaitingList {
    public enum WaitingListStatus {
        WAITING,
        SELF_LEFT,
        FORCE_LEFT,
        DECLINED,
        ACCEPTED
    }

    private ArrayList<WaitingListItem> list;
    private ArrayList<Entrant> entrants;

    public WaitingList() {
        list = new ArrayList<>();
        entrants = new ArrayList<>();
    }

    public ArrayList<Entrant> getEntrants() {
        return entrants;
    }

    public WaitingListStatus getStatus(Entrant entrant) {
        int idx = entrants.indexOf(entrant);
        if (idx >= 0) { // in list
            return list.get(idx).getStatus();
        } else { // not in list
            return null;
        }
    }

    private int change (Entrant entrant, WaitingListStatus status) {
        int idx = entrants.indexOf(entrant);
        if (idx >= 0) { // in list
            list.get(idx).setStatus(status);
            return 0;
        } else if (idx == -1) { // not in list
            list.add(new WaitingListItem(entrant, status));
            entrants.add(entrant);
            return -1;
        } else { // error
            return 1;
        }
    }

    // US 01.01.01
    public int join (Entrant entrant) {
        int status = change(entrant, WaitingListStatus.WAITING);
        if (status > 0) { // error
            return status;
        } else { // no error
            return 0;
        }
    }

    // US 01.01.02
    public int leave (Entrant entrant) {
        int status = change(entrant, WaitingListStatus.SELF_LEFT);
        if (status > 0) { // error
            return status;
        } else { // no error
            return 0;
        }
    }

    // US 01.05.02
    public int accept (Entrant entrant) {
        int status = change(entrant, WaitingListStatus.ACCEPTED);
        if (status > 0) { // error
            return status;
        } else { // no error
            return 0;
        }
    }

    // US 01.05.03
    public int decline (Entrant entrant) {
        int status = change(entrant, WaitingListStatus.DECLINED);
        if (status > 0) { // error
            return status;
        } else { // no error
            return 0;
        }
    }

    // US 01.06.04
    public int remove (Entrant entrant) {
        int status = change(entrant, WaitingListStatus.FORCE_LEFT);
        if (status > 0) { // error
            return status;
        } else { // no error
            return 0;
        }
    }

}
