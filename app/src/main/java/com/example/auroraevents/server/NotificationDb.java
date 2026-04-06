package com.example.auroraevents.server;

import android.util.Log;

import com.example.auroraevents.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotificationDb {

    private static final String TAG = "NotificationDb";

    private static NotificationDb instance;
    private final FirebaseFirestore db;

    private NotificationDb() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized NotificationDb getInstance() {
        if (instance == null) instance = new NotificationDb();
        return instance;
    }

    // ── Interfaces ────────────────────────────────────────────────────────────

    public interface OnNotificationsUpdated {
        void onUpdated(List<Notification> notifications);
    }

    public interface OnError {
        void onError(Exception e);
    }

    private interface NameMapCallback {
        void onReady(Map<String, String> nameMap);
    }


    public ListenerRegistration attachOrganizerNotificationsListener(
            String organizerId,
            OnNotificationsUpdated onUpdated,
            OnError onError) {

        return db.collection("Notifications")
                .whereEqualTo("sentFromId", organizerId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null) {
                        Log.e(TAG, "Snapshot listener error", e);
                        if (onError != null && e != null) onError.onError(e);
                        return;
                    }

                    List<Notification> fresh = new ArrayList<>();
                    Set<String> recipientIds = new HashSet<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Notification n = doc.toObject(Notification.class);
                        n.setNotificationId(doc.getId());
                        fresh.add(n);
                        if (n.getDeviceId() != null) recipientIds.add(n.getDeviceId());
                    }

                    if (fresh.isEmpty()) {
                        onUpdated.onUpdated(fresh);
                        return;
                    }

                    fetchUserNames(new ArrayList<>(recipientIds), nameMap -> {
                        for (Notification n : fresh) {
                            String name = nameMap.get(n.getDeviceId());
                            n.setRecipientName(name != null ? name : n.getDeviceId());
                        }
                        onUpdated.onUpdated(fresh);
                    });
                });
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private void fetchUserNames(List<String> deviceIds, NameMapCallback callback) {
        Map<String, String> nameMap   = new HashMap<>();
        int                 total     = deviceIds.size();
        int                 chunkSize = 30;
        int                 numChunks = (int) Math.ceil((double) total / chunkSize);
        int[]               done      = {0};

        for (int i = 0; i < total; i += chunkSize) {
            List<String> chunk = deviceIds.subList(i, Math.min(i + chunkSize, total));

            db.collection("Users")
                    .whereIn("deviceId", chunk)
                    .get()
                    .addOnSuccessListener(snap -> {
                        for (QueryDocumentSnapshot doc : snap) {
                            String id   = doc.getString("deviceId");
                            String name = doc.getString("name");
                            if (id != null) nameMap.put(id, name != null ? name : id);
                        }
                        done[0]++;
                        if (done[0] == numChunks) callback.onReady(nameMap);
                    })
                    .addOnFailureListener(err -> {
                        Log.e(TAG, "Failed to fetch user chunk", err);
                        done[0]++;
                        if (done[0] == numChunks) callback.onReady(nameMap);
                    });
        }
    }
}