package com.example.auroraevents.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.auroraevents.R;
import com.example.auroraevents.model.Notification;
import com.example.auroraevents.model.OrganizerNotificationArrayAdapter;
import com.example.auroraevents.server.NotificationDb;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * Displays every notification sent by a specific organizer, kept live via a snapshot listener.
 */
public class OrganizerNotificationFragment extends Fragment {

    public static final String ARG_ORGANIZER_ID    = "organizerId";
    public static final String ARG_ORGANIZER_NAME  = "organizerName";
    public static final String ARG_ORGANIZER_EMAIL = "organizerEmail";

    private static final String TAG = "OrganizerNotifList";

    private OrganizerNotificationArrayAdapter adapter;
    private ListenerRegistration notifListener;

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

        // add spacing between RecyclerView items
        adapter = new OrganizerNotificationArrayAdapter();
        RecyclerView recyclerView = view.findViewById(R.id.organizer_notifications_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            private final int space = (int) (16 * getResources().getDisplayMetrics().density);
            @Override
            public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View v,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                if (parent.getChildAdapterPosition(v) != 0) outRect.top = space;
            }
        });
        recyclerView.setAdapter(adapter);

        if (!organizerId.isEmpty()) {
            attachSnapshotListener(organizerId);
        } else {
            Log.e(TAG, "No organizerId argument provided");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (notifListener != null) {
            notifListener.remove();
            notifListener = null;
        }
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private void attachSnapshotListener(String organizerId) {
        notifListener = NotificationDb.getInstance().attachOrganizerNotificationsListener(
                organizerId,
                notifications -> adapter.setNotifications(notifications),
                e -> Log.e(TAG, "Failed to load notifications", e)
        );
    }
}