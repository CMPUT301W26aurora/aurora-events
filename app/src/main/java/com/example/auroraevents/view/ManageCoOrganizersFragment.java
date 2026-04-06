package com.example.auroraevents.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.RegistrationList;
import com.example.auroraevents.model.SelectedUser;
import com.example.auroraevents.server.EventDb;
import com.example.auroraevents.server.UserDb;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Lets the primary organizer view, add, and remove co-organizers for their event.
 *
 * Co-organizer state is stored in the coOrganizerDeviceIds field on the Firestore Event document.
 * A real-time snapshot listener keeps this screen in sync, so changes made on another device
 * appear immediately without a manual refresh.
 *
 * Acceptance criteria satisfied:
 * - Organizer can assign any entrant (waiting / selected / attending) as a co-organizer.
 * - Co-organizer is atomically removed from all entrant lists when assigned.
 * - Co-organizer cannot rejoin the entrant pool while listed as a co-organizer.
 * - Co-organizer role is stored per-event, not globally.
 * - Organizer can view the current list of co-organizers.
 * - Organizer can remove a co-organizer, freeing them to rejoin the entrant pool.
 *
 * Required bundle arguments:
 * - "eventId": Firestore document ID of the event.
 * - "organizerDeviceId": device ID of the primary organizer.
 * @author Joshua Terry
 */
public class ManageCoOrganizersFragment extends Fragment {

    private static final String TAG = "ManageCoOrgsFragment";

    private String eventId;
    private String organizerDeviceId;

    private ListenerRegistration eventListener;

    private ListView coOrganizerListView;
    private TextView emptyCoOrganizerText;
    private CoOrganizerAdapter coOrgAdapter;
    private final ArrayList<String> coOrgIds = new ArrayList<>();

    private Button addCoOrganizerButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_manage_co_organizers, container, false);

        Bundle args = getArguments();
        if (args == null) {
            getParentFragmentManager().popBackStack();
            return view;
        }
        eventId = args.getString("eventId");
        organizerDeviceId = args.getString("organizerDeviceId");

        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        coOrganizerListView  = view.findViewById(R.id.co_organizer_list);
        emptyCoOrganizerText = view.findViewById(R.id.empty_co_organizer_text);
        addCoOrganizerButton = view.findViewById(R.id.add_co_organizer_button);

        coOrgAdapter = new CoOrganizerAdapter();
        coOrganizerListView.setAdapter(coOrgAdapter);

        addCoOrganizerButton.setOnClickListener(v -> showAddCoOrganizerDialog());

        attachEventListener();
        return view;
    }

    /**
     * Attaches a real-time Firestore snapshot listener to the event document.
     *
     * Whenever the coOrganizerDeviceIds field changes, the list and empty-state view
     * are updated automatically.
     */
    private void attachEventListener() {
        eventListener = EventDb.getInstance().addSnapshotListenerForEvent(eventId,
                event -> {
                    if (event == null) return;
                    coOrgIds.clear();
                    coOrgIds.addAll(event.getCoOrganizerDeviceIds());
                    coOrgAdapter.notifyDataSetChanged();
                    emptyCoOrganizerText.setVisibility(coOrgIds.isEmpty() ? View.VISIBLE : View.GONE);
                },
                e -> Log.e(TAG, "Snapshot listener failed", e));
    }

    /**
     * Fetches the latest event snapshot and shows a picker of eligible entrants.
     */
    private void showAddCoOrganizerDialog() {
        EventDb.getInstance().getEvent(eventId,
                event -> {
                    if (event == null) {
                        Toast.makeText(getContext(), "Could not load event data.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    buildPickEntrantDialog(event);
                },
                e -> {
                    Log.e(TAG, "Failed to fetch event", e);
                    Toast.makeText(getContext(), "Error loading entrants.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Builds and displays a dialog of eligible entrants using their names instead of IDs.
     */
    private void buildPickEntrantDialog(Event event) {
        RegistrationList reg = event.getRegistrationList();
        List<String> currentCoOrgs = event.getCoOrganizerDeviceIds();

        List<String> eligible = new ArrayList<>();

        for (String id : reg.getWaitingList()) {
            if (!currentCoOrgs.contains(id) && !eligible.contains(id)) eligible.add(id);
        }
        for (SelectedUser selectedUser : reg.getSelectedList()) {
            String id = selectedUser.getUserId();

            // Check if they are already a Co-Org or already in the eligible list
            if (!currentCoOrgs.contains(id) && !eligible.contains(id)) {
                eligible.add(id);
            }
        }
        for (String id : reg.getAttendingList()) {
            if (!currentCoOrgs.contains(id) && !eligible.contains(id)) eligible.add(id);
        }

        if (eligible.isEmpty()) {
            Toast.makeText(getContext(),
                    "No eligible entrants to assign as co-organizer.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> displayLabels = new ArrayList<>();
        int total = eligible.size();
        int[] loaded = {0};

        for (int i = 0; i < eligible.size(); i++) {
            String deviceId = eligible.get(i);
            displayLabels.add(deviceId);

            final int index = i;

            UserDb.getInstance().getUser(deviceId,
                    user -> {
                        if (user != null && user.getName() != null && !user.getName().isEmpty()) {
                            displayLabels.set(index, user.getName());
                        }

                        loaded[0]++;
                        if (loaded[0] == total) {
                            showDialogWithNames(eligible, displayLabels);
                        }
                    },
                    e -> {
                        loaded[0]++;
                        if (loaded[0] == total) {
                            showDialogWithNames(eligible, displayLabels);
                        }
                    }
            );
        }
    }

    private void showDialogWithNames(List<String> eligible, List<String> displayLabels) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Select an Entrant")
                .setItems(displayLabels.toArray(new String[0]), (dialog, which) ->
                        confirmAndAddCoOrganizer(eligible.get(which)))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmAndAddCoOrganizer(String deviceId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Add Co-Organizer")
                .setMessage("Make this entrant a co-organizer?\n\nThey will be removed from the entrant pool.")
                .setPositiveButton("Confirm", (dialog, which) ->
                        EventDb.getInstance().addCoOrganizer(eventId, deviceId,
                                () -> Toast.makeText(getContext(), "Co-organizer added.", Toast.LENGTH_SHORT).show(),
                                e -> Toast.makeText(getContext(), "Failed to add co-organizer.", Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmAndRemoveCoOrganizer(String deviceId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Co-Organizer")
                .setMessage("Remove this co-organizer?")
                .setPositiveButton("Remove", (dialog, which) ->
                        EventDb.getInstance().removeCoOrganizer(eventId, deviceId,
                                () -> Toast.makeText(getContext(), "Removed.", Toast.LENGTH_SHORT).show(),
                                e -> Toast.makeText(getContext(), "Failed.", Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (eventListener != null) {
            eventListener.remove();
        }
    }

    private class CoOrganizerAdapter extends ArrayAdapter<String> {

        CoOrganizerAdapter() {
            super(requireContext(), 0, coOrgIds);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_co_organizer, parent, false);
            }

            String deviceId = coOrgIds.get(position);

            TextView nameText = convertView.findViewById(R.id.co_organizer_name);
            TextView idText = convertView.findViewById(R.id.co_organizer_id);
            Button removeButton = convertView.findViewById(R.id.remove_co_organizer_button);

            nameText.setText(deviceId);
            idText.setText(deviceId);

            UserDb.getInstance().getUser(deviceId,
                    user -> {
                        if (user != null && user.getName() != null && !user.getName().isEmpty()) {
                            nameText.setText(user.getName());
                        }
                    },
                    e -> Log.w(TAG, "Name lookup failed")
            );

            removeButton.setOnClickListener(v -> confirmAndRemoveCoOrganizer(deviceId));

            return convertView;
        }
    }
}