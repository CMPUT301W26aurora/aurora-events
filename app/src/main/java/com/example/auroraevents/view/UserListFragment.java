package com.example.auroraevents.view;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.Organizer;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserArrayAdapter;
import com.example.auroraevents.server.EventDb;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class UserListFragment extends Fragment {
    private ArrayList<User> userList;
    private UserArrayAdapter userListAdapter;
    private ListView userListView;
    private Button doneButton, mapButton, filterButton, sortButton;

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
            Event currentEvent;
        };
        String eventId = args.getString("eventId");

        // get current event from EventDB
        CountDownLatch latch = new CountDownLatch(1);
        EventDb.getInstance().getEvent(eventId,
                event -> {
                    ref.currentEvent = event;
                    latch.countDown();
                },
                e -> {
            Log.d(TAG, "Error fetching event" + e);
            latch.countDown();
        }
        );

        // Get list of entrants and make a user array adapter
        userList = ref.currentEvent.getListOfAllUsers();
        userListAdapter = new UserArrayAdapter(requireContext(), userList);
        userListView.setAdapter(userListAdapter);

        // Inflate and add the header
        View header = inflater.inflate(R.layout.header_entrant_fragment, userListView, false);
        userListView.addHeaderView(header, null, false);

        // Go back
        doneButton = view.findViewById(R.id.done_button);
        doneButton.setOnClickListener( v -> {
            getParentFragmentManager().popBackStack();
                });

        // Filter by status TODO
        /*
        if (String filter == "waiting") {
            userList = ref.currentEvent.getWaitListOfUsers();
        }
        else if (String filter...) {...}
        userListAdapter.notifyDataSetChanged();
         */

        return view;
    }
}
