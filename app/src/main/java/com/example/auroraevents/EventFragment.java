package com.example.auroraevents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EventFragment extends Fragment {

    // resource used: https://stackoverflow.com/questions/51769944/android-studio-recylerview-in-fragment-using-data-from-firestore

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.event_fragment, container, false);

        ArrayList<Event> eventList = new ArrayList<>();
        ListView eventsListView = view.findViewById(R.id.events_list);
        EventArrayAdapter eventsAdapter = new EventArrayAdapter(requireContext(), eventList);
        eventsListView.setAdapter(eventsAdapter);


    }
}
