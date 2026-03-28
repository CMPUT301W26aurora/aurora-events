package com.example.auroraevents.view;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.EventArrayAdapter;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserViewModel;
import com.example.auroraevents.server.EventDb;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * Displays a list of all available events fetched form Firestore.
 * Allows users to view a list of all events.
 * Allows users to tap an event to view event details.
 * Implements US 01.01.03 - View list of events available for joining the waiting list.
 */
public class EventFragment extends Fragment {

    private static final String TAG = "EventFragment";
    private FloatingActionButton addEventButton;
    private UserViewModel userViewModel;
    private ArrayList<Event> allEventsList;
    private TextView noEventText;
    private EventArrayAdapter eventsAdapter;

    // resource used: https://stackoverflow.com/questions/51769944/android-studio-recylerview-in-fragment-using-data-from-firestore

    /**
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.event_fragment, container, false);

        addEventButton = root.findViewById(R.id.eventAddButton);
        addEventButton.setVisibility(GONE);

        // Show add event button only if the user is an organizer
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        userViewModel.getSelectedItem().observe(getViewLifecycleOwner(), user -> {
            Log.d(TAG, "user role = " + (user != null ? user.getRole() : "null"));
            if (user != null && User.ROLE_ORGANIZER.equals(user.getRole())) {
                addEventButton.setVisibility(VISIBLE);
            } else {
                addEventButton.setVisibility(GONE);
            }
        });

        ListView listView = root.findViewById(R.id.events_list);

        // Inflate and add the header
        View header = inflater.inflate(R.layout.header_event_fragment, listView, false);
        listView.addHeaderView(header, null, false);

        allEventsList = new ArrayList<>();
        ArrayList<com.example.auroraevents.model.Event> eventList = new ArrayList<>();
        ListView eventsListView = root.findViewById(R.id.events_list);

        // get user's device ID to determine user's status for the event
        String userId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        // create adapter with eventList
        eventsAdapter = new EventArrayAdapter(requireContext(), eventList, userId);
        eventsListView.setAdapter(eventsAdapter);

        noEventText = root.findViewById(R.id.no_event_found_text);

        // resource used: https://stackoverflow.com/questions/7309259/get-list-of-attributes-of-an-object-in-an-list
        // get all events from firestore
        EventDb.getInstance().getAllEvents(events -> {
                    for (com.example.auroraevents.model.Event event : events) {
                        Log.d(TAG, "Event" + event.getName() + " in " + event.getLocation());
                        // TODO: add isPublic event to Event.java
                        // Boolean isPublic = event.isPublic();
                        // if (isPublic == null || isPublic) {
                        allEventsList.add(event);
                        eventList.add(event);
                    // }
                    }
                    eventsAdapter.notifyDataSetChanged();
                }, e -> Log.d(TAG, "Error fetching events" + e.getMessage()));

            // handle event taps by user to get the event's position
            eventsListView.setOnItemClickListener((parent, v, position, id) -> {
            Event selectedEvent = eventList.get(position - 1);

            // resource used: https://www.geeksforgeeks.org/android/bundle-in-android-with-example/
            // pass eventID to InfoUFragment using bundle
            Bundle args = new Bundle();
            args.putString("eventId", selectedEvent.getEventId());

            InfoUEventFragment infoUEventFragment = new InfoUEventFragment();
            infoUEventFragment.setArguments(args);

            // resource used: https://developer.android.com/guide/fragments/fragmentmanager
            // navigate to InfoUEventFragment
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, infoUEventFragment)
                    .addToBackStack(null)
                    .commit();
        });

        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new EventCreationFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        // Set SearchView query text listener
        SearchView searchView = root.findViewById(R.id.search_event);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.trim().isEmpty()) {
                    keywordSearchEvents("", eventList);
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                keywordSearchEvents(query.trim(), eventList);
                searchView.clearFocus();
                return true;
            }
        });
        return root;
    }

    /**
     * Filters the event list based on event name and description when a user searches for a keyword.
     * Event list returns to all events view when there is no keyword.
     * No such event text is displayed when no search results match.
     * @param searchKeyword keyword entered by user to search for
     * @param eventArrayList filtered event list
     */
        public void keywordSearchEvents(String searchKeyword, ArrayList<Event> eventArrayList) {
            String searchedQuery = searchKeyword.toLowerCase();
            eventArrayList.clear();

            if (searchedQuery.isEmpty()) {
                eventArrayList.addAll(allEventsList);
            } else {
                for (Event event : allEventsList) {
                    String searchedEventName = event.getName();
                    if (searchedEventName != null) {
                        searchedEventName = searchedEventName.toLowerCase();
                    } else {
                        searchedEventName = "";
                    }
                    String searchedEventDescription = event.getDescription();
                    if (searchedEventDescription != null) {
                        searchedEventDescription = searchedEventDescription.toLowerCase();
                    } else {
                        searchedEventDescription = "";
                    }
                    if (searchedEventName.contains(searchedQuery) || searchedEventDescription.contains(searchedQuery)) {
                        eventArrayList.add(event);
                    }
                }
            }
            eventsAdapter.notifyDataSetChanged();

            // show no such event text when there are no search results
            if (noEventText != null) {
                if (eventArrayList.isEmpty() && !searchedQuery.isEmpty()) {
                    noEventText.setVisibility(VISIBLE);
                } else {
                    noEventText.setVisibility(GONE);
                }
            }
        }
}