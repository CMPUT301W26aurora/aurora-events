package com.example.auroraevents;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

/**
 * Displays a list of all available events fetched form Firestore.
 * Allows users to view a list of all events.
 * Allows users to tap an event to view event details.
 * Implements US 01.01.03 - View list of events available for joining the waiting list.
 */
public class EventFragment extends Fragment {

    private static final String TAG = "EventFragment";

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
        View view = inflater.inflate(R.layout.event_fragment, container, false);

        ArrayList<Event> eventList = new ArrayList<>();
        ListView eventsListView = view.findViewById(R.id.events_list);

        // get user's device ID to determine user's status for the event
        String userId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        // create adapter with eventList
        EventArrayAdapter eventsAdapter = new EventArrayAdapter(requireContext(), eventList, userId);
        eventsListView.setAdapter(eventsAdapter);

        // resource used: https://stackoverflow.com/questions/7309259/get-list-of-attributes-of-an-object-in-an-list
        // get all events from firestore
        EventDb.getInstance().getAllEvents(events -> {
            for (Event event : events) {
                Log.d(TAG, "Event" +  event.getName() + " in " + event.getLocation());
                eventList.add(event);
            }
            eventsAdapter.notifyDataSetChanged();
        }, e -> Log.d(TAG, "Error fetching events" + e.getMessage())
        );

        // handle event taps by user to get the event's position
        eventsListView.setOnItemClickListener((parent, v, position, id) -> {
            Event selectedEvent = eventList.get(position);

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
                    .replace(R.id.main, infoUEventFragment)
                    .addToBackStack(null)
                    .commit();
        });
        return view;
    }
}