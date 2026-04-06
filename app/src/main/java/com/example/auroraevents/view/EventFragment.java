package com.example.auroraevents.view;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.EventArrayAdapter;
import com.example.auroraevents.model.RegistrationList;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserViewModel;
import com.example.auroraevents.server.EventDb;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

/**
 * Displays a list of all available events fetched form Firestore.
 * Allows users to view a list of all events.
 * Allows users to tap an event to view event details.
 * Implements US 01.01.03 - View list of events available for joining the waiting list.
 */
public class EventFragment extends Fragment {

    private static final String TAG = "EventFragment";
    private FloatingActionButton addEventButton;
    private ExtendedFloatingActionButton filterEventButton;
    private UserViewModel userViewModel;
    private String userId;
    private ArrayList<Event> allEventsList;
    private TextView noEventText;
    private EventArrayAdapter eventsAdapter;
    private boolean filterLocation = false;
    private boolean filterAvailableNow = false;
    private boolean filterWaitingList = false;
    private boolean filterCapacity = false;
    private boolean filterCreatedEvents = false;

    private double userLatitude = 0;
    private double userLongitude = 0;
    private static final float LOCATION_RADIUS = 50f;

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

        filterEventButton = root.findViewById(R.id.filter_button);

        // Show add event button only if the user is an organizer
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        userViewModel.getSelectedItem().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                userId = user.getDeviceId();
            }

            Log.d(TAG, "user role = " + (user != null ? user.getRole() : "null"));
            if (user != null && (User.ROLE_ORGANIZER.equals(user.getRole()))) {
                addEventButton.setVisibility(VISIBLE);
            } else {
                addEventButton.setVisibility(GONE);
            }
        });

        // Request location
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
            Location last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (last != null) {
                userLatitude = last.getLatitude();
                userLongitude = last.getLongitude();
            }
        }

        userViewModel.getAdminModeActive().observe(getViewLifecycleOwner(), isAdminMode -> {
            if(isAdminMode){
                addEventButton.setVisibility(GONE);
            }
        });

        ListView eventsListView = root.findViewById(R.id.events_list);

        // Inflate and add the header
        View header = inflater.inflate(R.layout.header_event_fragment, eventsListView, false);
        eventsListView.addHeaderView(header, null, false);

        allEventsList = new ArrayList<>();
        ArrayList<Event> eventList = new ArrayList<>();

        // create adapter with eventList
        eventsAdapter = new EventArrayAdapter(requireContext(), eventList, userId);
        eventsListView.setAdapter(eventsAdapter);

        noEventText = root.findViewById(R.id.no_event_found_text);

        // resource used: https://stackoverflow.com/questions/7309259/get-list-of-attributes-of-an-object-in-an-list
        // get all events from firestore
        EventDb.getInstance().getAllEvents(events -> {
            for (Event event : events) {
                Log.d(TAG, "Event" + event.getName() + " in " + event.getLocation());
                boolean isPrivate = event.isPrivate();
                if (
                        !isPrivate ||
                        Objects.equals(event.getOrganizerDeviceId(), userId) ||
                        event.getCoOrganizerDeviceIds().contains(userId) ||
                        event.getRegistrationList().getAttendingList().contains(userId) ||
                        event.getRegistrationList().getSelectedUserStrings().contains(userId)
                ) {
                    allEventsList.add(event);
                    eventList.add(event);
                }
            }
            eventsAdapter.notifyDataSetChanged();
        }, e -> Log.d(TAG, "Error fetching events" + e.getMessage()));

        // handle event taps by user to get the event's position
        eventsListView.setOnItemClickListener((parent, v, position, id) -> {
            Event selectedEvent = eventList.get(position - 1);

            Fragment eventFragment;
            Boolean adminMode = userViewModel.getAdminModeActive().getValue();
            if (userId != null && userId.equals(selectedEvent.getOrganizerDeviceId()) && adminMode != null && !adminMode) {
                Bundle args = new Bundle();
                args.putString("eventId", selectedEvent.getEventId());

                eventFragment = new EventEditFragment();
                eventFragment.setArguments(args);
            } else {
                // resource used: https://www.geeksforgeeks.org/android/bundle-in-android-with-example/
                // pass eventID to InfoUFragment using bundle
                Bundle args = new Bundle();
                args.putString("eventId", selectedEvent.getEventId());
                args.putString("userId", userId);

                if (userId == null) {
                    Toast.makeText(getContext(), "Loading user data, please wait...", Toast.LENGTH_SHORT).show();
                    return;
                }

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

        filterEventButton.setOnClickListener(v -> {
            BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext(), R.style.TransparentBottomSheet);
            View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter, null);
            bottomSheet.setContentView(sheetView);

            View bottomSheetContainer = bottomSheet.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheetContainer != null) {
                bottomSheetContainer.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            }

            SwitchMaterial toggleLocation = sheetView.findViewById(R.id.toggle_location);
            SwitchMaterial toggleAvailableNow = sheetView.findViewById(R.id.toggle_available_now);
            SwitchMaterial toggleWaiting = sheetView.findViewById(R.id.toggle_waiting_list);
            SwitchMaterial toggleCapacity = sheetView.findViewById(R.id.toggle_capacity);
            SwitchMaterial toggleCreated = sheetView.findViewById(R.id.toggle_created_events);

            // Restore previous toggle states
            toggleLocation.setChecked(filterLocation);
            toggleAvailableNow.setChecked(filterAvailableNow);
            toggleWaiting.setChecked(filterWaitingList);
            toggleCapacity.setChecked(filterCapacity);
            toggleCreated.setChecked(filterCreatedEvents);

            userViewModel.getSelectedItem().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    userId = user.getDeviceId();
                }

                Log.d(TAG, "user role = " + (user != null ? user.getRole() : "null"));
                if (user != null && (User.ROLE_ORGANIZER.equals(user.getRole()))) {
                    toggleCreated.setVisibility(VISIBLE);
                } else {
                    TextView text = sheetView.findViewById(R.id.text_created_events);
                    text.setVisibility(GONE);
                    toggleCreated.setVisibility(GONE);
                }
            });

            sheetView.findViewById(R.id.btn_confirm).setOnClickListener(confirmView -> {
                filterLocation    = toggleLocation.isChecked();
                filterAvailableNow = toggleAvailableNow.isChecked();
                filterWaitingList   = toggleWaiting.isChecked();
                filterCapacity = toggleCapacity.isChecked();
                filterCreatedEvents = toggleCreated.isChecked();

                boolean anyFilterActive = filterLocation || filterAvailableNow
                        || filterWaitingList || filterCapacity || filterCreatedEvents;

                // Apply filters - can be stacked
                if (!filterWaitingList && !filterCreatedEvents) {
                    ArrayList<Event> filtered = new ArrayList<>();
                    for (Event event : allEventsList) {
                        if (filterAvailableNow && !isRegistrationActive(event)) continue;
                        if (filterLocation && !isNearUser(event)) continue;
                        if (filterCapacity && !hasWaitingCapacity(event) && !hasAttendingCapacity(event)) continue;
                        filtered.add(event);
                    }
                    applyFilters(filtered.isEmpty() && !filterLocation && !filterAvailableNow
                            ? allEventsList : filtered);
                } else {
                    ArrayList<Event> filtered = new ArrayList<>();
                    int[] pending = {0};

                    if (filterWaitingList) pending[0]++;
                    if (filterCreatedEvents) pending[0]++;

                    Runnable onAllDone = () -> {
                        ArrayList<Event> finalFiltered = new ArrayList<>();
                        for (Event event : filtered) {
                            if (filterAvailableNow && !isRegistrationActive(event)) continue;
                            if (filterLocation && !isNearUser(event)) continue;
                            if (filterCapacity && !hasWaitingCapacity(event) && !hasAttendingCapacity(event)) continue;
                            finalFiltered.add(event);
                        }
                        applyFilters(finalFiltered);
                    };

                    if (filterWaitingList) {
                        EventDb.getInstance().getEventsForUser(userId, EventDb.LIST_WAITING,
                                events -> {
                                    filtered.addAll(events);
                                    pending[0]--;
                                    if (pending[0] == 0) onAllDone.run();
                                },
                                e -> Log.e(TAG, "Failed to fetch waiting list events", e));
                    }

                    if (filterCreatedEvents) {
                        EventDb.getInstance().getEventsByOrganizer(userId,
                                events -> {
                                    filtered.addAll(events);
                                    pending[0]--;
                                    if (pending[0] == 0) onAllDone.run();
                                },
                                e -> Log.e(TAG, "Failed to fetch created events", e));
                    }
                }

                bottomSheet.dismiss();
            });
            bottomSheet.show();
        });


        // set SearchView query text listener
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
     * filter events to match searched keywords
     * return all events when nothing is being searched
     * private events are never included in results
     *
     * @param searchKeyword keyword entered by user to search for
     * @param eventList original list of events to search through
     * @return filtered list of events matching the keyword
     */
    public ArrayList<Event> filterKeywordEvents(String searchKeyword, ArrayList<com.example.auroraevents.model.Event> eventList) {
        String searchedQuery = searchKeyword.toLowerCase();
        ArrayList<com.example.auroraevents.model.Event> filteredEventsList = new ArrayList<>();

        if (searchedQuery.isEmpty()) {
            // display all public events when search is cleared
            for (com.example.auroraevents.model.Event event : eventList) {
                if (!event.isPrivate()) {
                    filteredEventsList.add(event);
                }
            }
        } else {
            for (com.example.auroraevents.model.Event event : eventList) {
                // don't include private events for keyword search
                if (event.isPrivate()) continue;
                // convert event name to lower case
                String searchedEventName = event.getName();
                if (searchedEventName != null) {
                    searchedEventName = searchedEventName.toLowerCase();
                } else {
                    searchedEventName = "";
                }
                // convert event description to lower case
                String searchedEventDescription = event.getDescription();
                if (searchedEventDescription != null) {
                    searchedEventDescription = searchedEventDescription.toLowerCase();
                } else {
                    searchedEventDescription = "";
                }
                // add event to filtered list if keyword matches
                if (searchedEventName.contains(searchedQuery) || searchedEventDescription.contains(searchedQuery)) {
                    filteredEventsList.add(event);
                }
            }
        }
        return filteredEventsList;
    }

    /**
     * display required event list when keyword matches
     * displays message when no keyword matches
     *
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

    /**
     * Apply filtersfor events
     * @param filtered
     * List of filtered events
     */
    private void applyFilters(List<Event> filtered) {
        ArrayList<Event> publicOnly = new ArrayList<>();
        for (Event event : filtered) {
            if (!event.isPrivate()) {
                publicOnly.add(event);
            }
        }

        eventsAdapter.clear();
        eventsAdapter.addAll(publicOnly);
        eventsAdapter.notifyDataSetChanged();

        if (noEventText != null) {
            noEventText.setVisibility(publicOnly.isEmpty() ? VISIBLE : GONE);
        }
    }

    /**
     * Check if event registration time is active
     * @param event
     * Event to be checked
     * @return
     * Returns a boolean
     */
    private boolean isRegistrationActive(Event event) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = event.getRegistrationTimeStartAsDateTime();
            LocalDateTime end = event.getRegistrationTimeEndAsDateTime();
            return !now.isBefore(start) && !now.isAfter(end);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing registration time");
            return false;
        }
    }

    /**
     * Check if event is within the radius of the user or
     * if the event does not need geolocation
     * @param event
     * Event to be checked
     * @return
     * Returns a float
     */
    private boolean isNearUser(Event event) {
        if (!event.getGeolocationRequired()) return true;
        if (userLatitude == 0 && userLongitude == 0) return false;

        float[] results = new float[1];
        Location.distanceBetween(userLatitude, userLongitude,
                event.getLatitude(), event.getLongitude(),
                results
        );
        float distance = results[0] / 1000f;
        return distance <= LOCATION_RADIUS;
    }

    /**
     * Check if an event has available spots on waiting list
     * @param event
     * Event to be checked
     * @return
     * Returns a boolean
     */
    private boolean hasWaitingCapacity(Event event) {
        RegistrationList regList = event.getRegistrationList();
        if (regList == null) return false;
        int eventCapacity = regList.getWaitingCapacity();
        int currentCapacity = regList.getWaitingList().size();
        return eventCapacity <= 0 || currentCapacity < eventCapacity;
    }

    /**
     * Check if an event has available spots on waiting list
     * @param event
     * Event to be checked
     * @return
     * Returns a boolean
     */
    private boolean hasAttendingCapacity(Event event) {
        RegistrationList regList = event.getRegistrationList();
        if (regList == null) return false;
        int eventCapacity = regList.getAttendingCapacity();
        int currentCapacity = regList.getAttendingList().size();
        return eventCapacity <= 0 || currentCapacity < eventCapacity;
    }
}