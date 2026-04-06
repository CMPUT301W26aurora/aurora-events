package com.example.auroraevents.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.auroraevents.R;
import com.example.auroraevents.model.Notification;
import com.example.auroraevents.model.OrganizerNotificationArrayAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Displays every notification sent by a specific organizer.
 */
public class OrganizerNotificationFragment extends Fragment {

    public static final String ARG_ORGANIZER_ID    = "organizerId";
    public static final String ARG_ORGANIZER_NAME  = "organizerName";
    public static final String ARG_ORGANIZER_EMAIL = "organizerEmail";

    private static final String TAG = "OrganizerNotifList";

    private OrganizerNotificationArrayAdapter adapter;
    private final ArrayList<Notification> notifications = new ArrayList<>();

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Back button
        view.findViewById(R.id.back_button_organizer_notif)
                .setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Organizer info line: "Name | email | deviceId"
        String organizerId    = requireArguments().getString(ARG_ORGANIZER_ID, "");
        String organizerName  = requireArguments().getString(ARG_ORGANIZER_NAME, "");
        String organizerEmail = requireArguments().getString(ARG_ORGANIZER_EMAIL, "");

        TextView infoText = view.findViewById(R.id.organizer_info_text);
        infoText.setText(organizerName + "  |  " + organizerEmail + "  |  " + organizerId);

        // List
        adapter = new OrganizerNotificationArrayAdapter(requireContext(), notifications);
        ListView listView = view.findViewById(R.id.organizer_notifications_list);
        listView.setAdapter(adapter);

        if (!organizerId.isEmpty()) {
            loadNotificationsForOrganizer(organizerId);
        } else {
            Log.e(TAG, "No organizerId argument provided");
        }
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private void loadNotificationsForOrganizer(String organizerId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Notifications")
                .whereEqualTo("sentFromId", organizerId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {

                    notifications.clear();
                    Set<String> recipientIds = new HashSet<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Notification n = doc.toObject(Notification.class);
                        n.setNotificationId(doc.getId());
                        notifications.add(n);
                        if (n.getDeviceId() != null) recipientIds.add(n.getDeviceId());
                    }

                    if (notifications.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    fetchUserNames(db, new ArrayList<>(recipientIds), nameMap -> {
                        for (Notification n : notifications) {
                            String name = nameMap.get(n.getDeviceId());
                            n.setRecipientName(name != null ? name : n.getDeviceId());
                        }
                        adapter.notifyDataSetChanged();
                    });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load notifications", e));
    }

    private void fetchUserNames(FirebaseFirestore db,
                                List<String> deviceIds,
                                NameMapCallback callback) {

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
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to fetch user chunk", e);
                        done[0]++;
                        if (done[0] == numChunks) callback.onReady(nameMap);
                    });
        }
    }

    private interface NameMapCallback {
        void onReady(Map<String, String> nameMap);
    }
}