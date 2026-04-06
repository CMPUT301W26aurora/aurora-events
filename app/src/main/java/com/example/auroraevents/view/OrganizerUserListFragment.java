package com.example.auroraevents.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.auroraevents.R;
import com.example.auroraevents.model.CsvExportManager;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.RegistrationList;
import com.example.auroraevents.model.SelectedUser;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserAdapter;
import com.example.auroraevents.model.UserAdapterWrapper;
import com.example.auroraevents.server.EventDb;
import com.example.auroraevents.server.UserDb;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays list of all entrants with any and all status.
 * Allows Organizers to filter and cancel entrants.
 */
public class OrganizerUserListFragment extends DialogFragment {
    private UserAdapter adapter;
    private String TAG = "OrganizerUserListFragment";
    private RegistrationList registrationList;
    private String currentEventId;
    private ListenerRegistration listenerRegistration;
    private List<UserAdapterWrapper> masterUiList;
    private List<String> statuses;
    private CsvExportManager csvExportManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_user_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.entrants_list_org);
        ImageButton deleteButton  = view.findViewById(R.id.delete_user_button_org_item);
        Button filterButton       = view.findViewById(R.id.filter_button);
        Button doneButton         = view.findViewById(R.id.done_button);

        Bundle args = getArguments();
        statuses = new ArrayList<>();

        if (args != null) {
            currentEventId = args.getString("eventId");
        }

        csvExportManager = new CsvExportManager(requireContext());
        adapter = new UserAdapter(new ArrayList<>(), false, new UserAdapter.OnUserInteractionListener() {
            @Override
            public void Onclick(User user) {
                RemoveUserPopUpDialog removeDialog = RemoveUserPopUpDialog.newInstance(
                        registrationList,
                        user.getDeviceId(),
                        currentEventId
                );
                removeDialog.show(getParentFragmentManager(), "remove_picker");
            }
            @Override
            public void OnNotify(User user){
                //do nothing here
            }
        });

        doneButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        filterButton.setOnClickListener(v -> {
            FilterUserPopUpDialog dialog = FilterUserPopUpDialog.newInstance();
            dialog.setOnFilterAppliedListener(selectedStatuses -> {
                statuses = selectedStatuses;
                orgUserApplyFilter(selectedStatuses);
            });
            dialog.setOnExportRequestedListener(
                    selectedStatuses -> csvExportManager.export(masterUiList, selectedStatuses)
            );
            dialog.show(getChildFragmentManager(), "filter_dialog");
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        listenerRegistration = EventDb.getInstance().addSnapshotListenerForEvent(
                currentEventId,
                event -> {
                    registrationList = event.getRegistrationList();
                    loadParticipantData();
                },
                e -> Log.e(TAG, "failed to fetch event", e)
        );
    }

    private void loadParticipantData() {
        if (registrationList == null) return;
        List<String> allIds = registrationList.getAllUsers();

        UserDb.getInstance().fetchParticipants(allIds, new UserDb.OnUsersLoadedListener() {
            @Override
            public void onUsersUpdate(List<User> participants) {
                Log.d(TAG, "Fetched " + participants.size() + " users from DB");
                Log.d(TAG, "RegistrationList has " + registrationList.getAllUsers().size() + " IDs");
                List<UserAdapterWrapper> uiList = createDisplayList(participants, registrationList);
                masterUiList = uiList;
                Log.d(TAG, "UI List size: " + uiList.size());
                orgUserApplyFilter(statuses);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Failed to refresh list", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<UserAdapterWrapper> createDisplayList(List<User> fetchedUsers,
                                                       RegistrationList registration) {
        List<UserAdapterWrapper> displayList = new ArrayList<>();

        Map<String, Date> map = new HashMap<>();
        for (SelectedUser se : registration.getSelectedList()) {
            map.put(se.getUserId(), se.getSelectedAt().toDate());
        }

        for (User user : fetchedUsers) {
            String userId = user.getDeviceId();
            String status = "Waiting";
            Date time = null;

            if (map.containsKey(userId)) {
                status = "Selected";
                time = map.get(userId);
            } else if (registration.getCancelledList().contains(userId)) {
                status = "Cancelled";
            } else if (registration.getDeclinedList().contains(userId)) {
                status = "Declined";
            } else if (registration.getAttendingList().contains(userId)) {
                status = "Accepted";
            } else if (registrationList.getRemovedList().contains(userId)) {
                status = "Removed";
            }

            displayList.add(new UserAdapterWrapper(user, status, time));
        }

        return displayList;
    }

    private void orgUserApplyFilter(List<String> statuses) {
        if (masterUiList == null) return;
        if (statuses.isEmpty()) {
            adapter.setUserList(masterUiList);
            adapter.notifyDataSetChanged();
            return;
        }

        List<UserAdapterWrapper> filtered = new ArrayList<>();
        for (UserAdapterWrapper user : masterUiList) {
            if (statuses.contains(user.getStatus())) {
                filtered.add(user);
            }
        }

        adapter.setUserList(filtered);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        View navBar = getActivity().findViewById(R.id.nav_bar);
        if (navBar != null) {
            navBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        View nav = getActivity().findViewById(R.id.nav_bar);
        if (nav != null) {
            nav.setVisibility(View.GONE);
        }
    }
}