package com.example.auroraevents.view;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserArrayAdapter;
import com.example.auroraevents.server.EventDb;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class UserListFragment extends DialogFragment {
    private Event currentEvent;
    private ArrayList<User> userList;
    private UserArrayAdapter userListAdapter;
    private ListView userListView;
    private Button doneButton, mapButton, filterButton, sortButton;
    private ImageButton deleteButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.user_list_fragment, container, false);

        // get Event ID from bundle
        Bundle args = getArguments();
        if (args == null || args.getString("eventId") == null) {
            Log.e(TAG, "Missing eventId argument");
            getParentFragmentManager().popBackStack();
            return view;
        }
        var ref = new Object() {
            Event fetchedEvent;
        };
        String eventId = args.getString("eventId");

        // get current event from EventDB
        CountDownLatch latch = new CountDownLatch(1);
        EventDb.getInstance().getEvent(eventId,
                event -> {
                    ref.fetchedEvent = event;
                    latch.countDown();
                },
                e -> {
                    Log.d(TAG, "Error fetching event" + e);
                    latch.countDown();
                }
        );
        currentEvent = ref.fetchedEvent;

        // Get list of entrants and make a user array adapter
        userList = currentEvent.getListOfAllUsers();
        userListAdapter = new UserArrayAdapter(requireContext(), userList, currentEvent);
        userListView.setAdapter(userListAdapter);

        // Inflate and add the header
        View header = inflater.inflate(R.layout.header_entrant_fragment, userListView, false);
        userListView.addHeaderView(header, null, false);

        // Go back
        doneButton = view.findViewById(R.id.done_button);
        doneButton.setOnClickListener( v -> {
            getParentFragmentManager().popBackStack();
                });

        // Filter by status TODO Figure out UI
        filterButton = view.findViewById(R.id.filter_button);
        var lambdaContext = new Object() {
            SwitchCompat participating_filter;
            SwitchCompat invited_filter;
            SwitchCompat rejected_filter;
            SwitchCompat waiting_filter;
            SwitchCompat cancelled_filter;
        };
        filterButton.setOnClickListener(v -> {
            new FilterUserPopUpFragment().show(
                    getChildFragmentManager(), null);
            lambdaContext.participating_filter = v.findViewById(R.id.particpating_switch);
            lambdaContext.invited_filter = v.findViewById(R.id.invited_switch);
            lambdaContext.rejected_filter = v.findViewById(R.id.rejected_switch);
            lambdaContext.waiting_filter = v.findViewById(R.id.waiting_switch);
            lambdaContext.cancelled_filter = v.findViewById(R.id.cancelled_switch);
        });
        userList = new ArrayList<User>();
        lambdaContext.participating_filter.setOnCheckedChangeListener( (buttonView, isChecked)->{
            userList.addAll(currentEvent.getAttendingListOfUsers());
        });
        lambdaContext.invited_filter.setOnCheckedChangeListener( (buttonView, isChecked)->{
            userList.addAll(currentEvent.getSelectedListOfUsers());
        });
        lambdaContext.rejected_filter.setOnCheckedChangeListener( (buttonView, isChecked)->{
            userList.addAll(currentEvent.getDeclinedListOfUsers());
        });
        lambdaContext.waiting_filter.setOnCheckedChangeListener( (buttonView, isChecked)->{
            userList.addAll(currentEvent.getWaitingListOfUsers());
        });
        lambdaContext.cancelled_filter.setOnCheckedChangeListener( (buttonView, isChecked)->{
            userList.addAll(currentEvent.getCancelledListOfUsers());
        });
        userListAdapter.notifyDataSetChanged();

        // Delete users TODO figure out how to link with delete button
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedUserID = Objects.requireNonNull(userListAdapter.getItem(position)).getDeviceId();
                currentEvent.registrationList.addToCancelledList(selectedUserID);
            }
        });
        userListAdapter.notifyDataSetChanged();

        // Sort users TODO figure out how to get dates and sort them as well as UI

        return view;
    }
}
