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

import androidx.fragment.app.DialogFragment;

import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserArrayAdapter;
import com.example.auroraevents.server.EventDb;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Displays list of all entrants with any and all status
 * Allows Organizers to filter and cancel entrants
 */
public class UserListFragment extends DialogFragment {
    private Event currentEvent;
    private List<User> userList;
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
        String eventId = args.getString("eventId");

        // get current event from EventDB
        userListView = view.findViewById(R.id.entrants_list);
        EventDb.getInstance().getEvent(eventId,
                event -> {
                    loadEntrantsList(event, inflater, view, userListView);
                },
                e -> {
                    Log.d(TAG, "Error fetching event" + e);
                }
        );
        return view;
    }

    /**
     * Get the current event's entrants and display them, as well as implementing the buttons for filter and done button
     * @param event Current Event
     * @author Won Koh
     */
    public void loadEntrantsList(Event event, LayoutInflater inflater, View view, ListView userListView) {
        // Get list of entrants and make a user array adapter
        userList = event.registrationList.getUsersFromDB(event.registrationList.getAllEntrantsList());
        userListAdapter = new UserArrayAdapter(requireContext(), userList, event, userListAdapter, this);
        userListView.setAdapter(userListAdapter);

        // Inflate and add the header
        View header = inflater.inflate(R.layout.header_entrant_fragment, userListView, false);
        userListView.addHeaderView(header, null, false);

        // Go back
        doneButton = view.findViewById(R.id.done_button);
        doneButton.setOnClickListener( v -> {
            getParentFragmentManager().popBackStack();
        });

        // Filter by status
        filterButton = view.findViewById(R.id.filter_button);
        filterButton.setOnClickListener(v -> {
            FilterUserPopUpDialog dialog = FilterUserPopUpDialog.newInstance(
                    event,
                    userList,
                    userListView
            );
            dialog.show(getParentFragmentManager(), "filter_users");
        });
    }
}
