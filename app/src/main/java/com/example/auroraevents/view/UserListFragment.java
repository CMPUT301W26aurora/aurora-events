package com.example.auroraevents.view;

import static android.content.ContentValues.TAG;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.Organizer;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserArrayAdapter;
import com.example.auroraevents.server.EventDb;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

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
        ArrayList<String> filter = new ArrayList<String>();
        if (!(filter.isEmpty())) {
            userList = new ArrayList<User>();
            if (filter.contains("Waiting")) {
                userList.addAll(currentEvent.getWaitingListOfUsers());
            }
            if (filter.contains("Invited")) {
                userList.addAll(currentEvent.getSelectedListOfUsers());
            }
            if (filter.contains("Rejected")) {
                userList.addAll(currentEvent.getDeclinedListOfUsers());
            }
            if (filter.contains("Participating")) {
                userList.addAll(currentEvent.getDeclinedListOfUsers());
            }
            if (filter.contains("Cancelled")) {
                userList.addAll(currentEvent.getDeclinedListOfUsers());
            }
        }
        userListAdapter.notifyDataSetChanged();

        // Delete users TODO figure out how to link with delete button
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedUserID = Objects.requireNonNull(userListAdapter.getItem(position)).getDeviceId();
                currentEvent.registrationList.addToCancelledList(selectedUserID);
            }
        });

        // Sort users TODO figure out how to get dates and sort them as well as UI

        return view;
    }
}
