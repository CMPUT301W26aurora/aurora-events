package com.example.auroraevents.view;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
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
import com.example.auroraevents.model.EventFiltering;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Displays a list of all available events fetched form Firestore.
 * Allows users to view a list of all events.
 * Allows users to tap an event to view event details.
 * Implements US 01.01.03 - View list of events available for joining the waiting list.
 * Implements US 01.01.06 - Use keyword search with filtering to narrow event search.
 */
public class EventFragment extends Fragment {

    private static final String TAG = "EventFragment";
    private FloatingActionButton addEventButton;
    private UserViewModel userViewModel;
    private String userId;
    private ArrayList<Event> allEventsList;
    private TextView noEventText;
    private EventArrayAdapter eventsAdapter;
    private Boolean inAdmin;
    private ListView eventsListView;
    private final ArrayList<Event> filteredEventsList = new ArrayList<>();
    private final EventFiltering filteringHelper = new EventFiltering();
    private String filterQuery = "";
    private String locationFilter = "";
    private LocalDate startDateFilter = null;
    private LocalDate endDateFilter = null;
    private int maxCapacityFilter = 0;

    // resource used: https://stackoverflow.com/questions/51769944/android-studio-recylerview-in-fragment-using-data-from-firestore

    /**
     * @author Alina Iqbal
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.event_fragment, container, false);

        addEventButton = root.findViewById(R.id.eventAddButton);
        addEventButton.setVisibility(GONE);
        Bundle passed = getArguments();
        if (passed != null) {
            inAdmin = passed.getBoolean("inAdmin");
        }

        // Show add event button only if the user is an organizer
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        userViewModel.getSelectedItem().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                userId = user.getDeviceId();
            }

            Log.d(TAG, "user role = " + (user != null ? user.getRole() : "null"));
            if (user != null && (User.ROLE_ORGANIZER.equals(user.getRole()) || User.ROLE_ADMIN.equals(user.getRole()))) {
                addEventButton.setVisibility(VISIBLE);
            } else {
                addEventButton.setVisibility(GONE);
            }
        });

        eventsListView = root.findViewById(R.id.events_list);

        // Inflate and add the header
        View header = inflater.inflate(R.layout.header_event_fragment, eventsListView, false);
        eventsListView.addHeaderView(header, null, false);

        allEventsList = new ArrayList<>();
        // ArrayList<Event> eventList = new ArrayList<>();

        // create adapter with eventList
        eventsAdapter = new EventArrayAdapter(requireContext(), filteredEventsList, userId);
        eventsListView.setAdapter(eventsAdapter);

        noEventText = root.findViewById(R.id.no_event_found_text);

        // resource used: https://stackoverflow.com/questions/7309259/get-list-of-attributes-of-an-object-in-an-list
        // get all events from firestore
        EventDb.getInstance().getAllEvents(events -> {
            for (Event event : events) {
                Log.d(TAG, "Event" + event.getName() + " in " + event.getLocation());
                boolean isPrivate = event.isPrivate();
                if (!isPrivate) {
                    allEventsList.add(event);
                }
            }
            applyFilters();
        }, e -> Log.d(TAG, "Error fetching events" + e.getMessage()));

        // handle event taps by user to get the event's position
        eventsListView.setOnItemClickListener((parent, v, position, id) -> {
            Event selectedEvent = filteredEventsList.get(position - 1);

            // resource used: https://www.geeksforgeeks.org/android/bundle-in-android-with-example/
            // pass eventID to InfoUFragment using bundle
            Bundle args = new Bundle();
            args.putString("eventId", selectedEvent.getEventId());
            args.putString("userId", userId);
            args.putBoolean("inAdmin", inAdmin);

            Fragment eventFragment;
            if (userId.equals(selectedEvent.getOrganizerDeviceId())) {
                //TODO 5: open event edit
                eventFragment = new InfoUEventFragment();
                eventFragment.setArguments(args);
            } else {
                eventFragment = new InfoUEventFragment();
                eventFragment.setArguments(args);
            }
            // resource used: https://developer.android.com/guide/fragments/fragmentmanager
            // navigate to InfoUEventFragment
            getParentFragmentManager()
                    .beginTransaction()
                    .hide(this)
                    .add(R.id.fragment_container, eventFragment)
                    .addToBackStack(null)
                    .commit();
        });

        addEventButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new EventCreationFragment())
                        .addToBackStack(null)
                        .commit());

        // set SearchView query text listener
        SearchView searchView = root.findViewById(R.id.search_event);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                    filterQuery = newText.trim();
                    applyFilters();
                    return true;
            }
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterQuery = query.trim();
                applyFilters();
                searchView.clearFocus();
                return true;
            }
        });
        return root;
    }

    /**
     * keeps updating the filtered list whenever filters change to display
     * matching events
     */
    private void applyFilters() {
        filteredEventsList.clear();
        filteredEventsList.addAll(filteringHelper.applyAllFilters(allEventsList, filterQuery, locationFilter, startDateFilter, endDateFilter, maxCapacityFilter));
        eventsAdapter.notifyDataSetChanged();
        noEventsFoundText();
    }

    /**
     * Display no events found text when no matching events are found
     */
    private void noEventsFoundText() {
        if (noEventText == null) {
            return;
        }
        boolean filtersApplied = false;
        if (!filterQuery.isEmpty() || !locationFilter.isEmpty() ||
        startDateFilter != null || endDateFilter != null || maxCapacityFilter < 0) {
            filtersApplied = true;
        }

        if (filteredEventsList.isEmpty() && filtersApplied) {
            noEventText.setVisibility(VISIBLE);
        } else {
            noEventText.setVisibility(GONE);
        }
    }

    /**
     *
     * @param location required location to filter events for
     */
    public void setLocationFilter(@ Nullable String location) {
        if (location != null) {
            locationFilter = location.trim();
        } else {
            location = "";
        }
        applyFilters();
    }

    /**
     *
     * @param startDate earliest date to match events for
     * @param endDate latest date to match events for
     */
    public void setDateFilter(@Nullable LocalDate startDate, @Nullable LocalDate endDate) {
        startDateFilter = startDate;
        endDateFilter = endDate;
        applyFilters();
    }

    /**
     *
     * @param maxCapacity max waiting list capacity to filter events for
     */
    public void setMaxCapacityFilter(int maxCapacity) {
        maxCapacityFilter = maxCapacity;
        applyFilters();
    }

    /**
     * remove all filters when filters initially applied are removed to display all events
     */
    public void removeAllFilters() {
        filterQuery = "";
        locationFilter = "";
        startDateFilter = null;
        endDateFilter = null;
        maxCapacityFilter = 0;
        applyFilters();
    }

    /**
     * filter events by keywords like event name, description, location
     * @param searchKeyword keyword to search events for
     * @param eventList list of all events to search through
     * @return filtered list of public events that match the required keywords
     */
    public ArrayList<Event> filterKeywordEvents(String searchKeyword, ArrayList<com.example.auroraevents.model.Event> eventList) {
        return filteringHelper.filterKeywordEvents(searchKeyword, eventList);
    }

    /**
     * display required event list when keyword matches
     * displays message when no keyword matches
     * @param searchKeyword  keyword entered by user to search for
     * @param eventArrayList filtered event list
     */
    public void keywordSearchEvents(String searchKeyword, ArrayList<Event> eventArrayList) {
        ArrayList<Event> searchResults = filterKeywordEvents(searchKeyword, allEventsList);
        eventArrayList.clear();
        eventArrayList.addAll(searchResults);
        eventsAdapter.notifyDataSetChanged();

        // show no such event message when there are no search results
        if (noEventText != null) {
            if (eventArrayList.isEmpty() && !searchKeyword.trim().isEmpty()) {
                noEventText.setVisibility(VISIBLE);
            } else {
                noEventText.setVisibility(GONE);
            }
        }
    }
}