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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_fragment, container, false);

        ArrayList<Event> eventList = new ArrayList<>();
        ListView eventsListView = view.findViewById(R.id.events_list);

        // get user's device ID to determine user's status for the event
        String userId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        EventArrayAdapter eventsAdapter = new EventArrayAdapter(requireContext(), eventList, userId);
        eventsListView.setAdapter(eventsAdapter);

        // get events from firestore
        FirebaseFirestore eventsDb = FirebaseFirestore.getInstance();
        eventsDb.collection("Events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    eventList.clear();
                    for (DocumentSnapshot document : querySnapshot) {
                        Event event = document.toObject(Event.class);
                        event.setEventId(document.getId());
                        eventList.add(event);
                    }
                    eventsAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Fetched " + eventList.size() + " events");
                })
                .addOnFailureListener(e -> Log.d(TAG, "Error: " + e.getMessage()));

        // handle event taps by user
        eventsListView.setOnItemClickListener((parent, v, position, id) -> {
            Event selectedEvent = eventList.get(position);
            Log.d(TAG, "Clicked: " + selectedEvent.getName());

            // resource used: https://www.geeksforgeeks.org/android/bundle-in-android-with-example/
            // pass eventID using bundle
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