package com.example.auroraevents.view;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.auroraevents.R;
import com.example.auroraevents.model.Notification;
import com.example.auroraevents.model.NotificationArrayAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class NotificationFragment extends Fragment {

    private static final String TAG = "NotificationList";

    private ListView                 listView;
    private NotificationArrayAdapter adapter;
    private ArrayList<Notification>  notifications;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notifications = new ArrayList<>();
        adapter       = new NotificationArrayAdapter(requireContext(), notifications);
        listView      = view.findViewById(R.id.notifications_list);

        // Add header
        View header = LayoutInflater.from(requireContext())
                .inflate(R.layout.header_notification_list, listView, false);
        listView.addHeaderView(header, null, false);
        listView.setAdapter(adapter);

        // Tap a notification to open its event
        listView.setOnItemClickListener((parent, v, position, id) -> {
            Notification selected = notifications.get(position - 1);
            if (selected.getEventId() == null) return;

            Bundle args = new Bundle();
            args.putString("eventId", selected.getEventId());

            InfoUEventFragment infoUEventFragment = new InfoUEventFragment();
            infoUEventFragment.setArguments(args);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, infoUEventFragment)
                    .addToBackStack(null)
                    .commit();
        });

        loadNotifications();
    }

    private void loadNotifications() {
        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(), Settings.Secure.ANDROID_ID
        );

        FirebaseFirestore.getInstance()
                .collection("Notifications")
                .whereEqualTo("deviceId", deviceId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    notifications.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Notification n = doc.toObject(Notification.class);
                        n.setNotificationId(doc.getId());
                        notifications.add(n);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load notifications", e));
    }
}