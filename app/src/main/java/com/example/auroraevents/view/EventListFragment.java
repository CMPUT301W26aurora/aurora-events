package com.example.auroraevents.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.auroraevents.R;

public class EventListFragment extends Fragment {
    private Button tempButton;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);
        tempButton = view.findViewById(R.id.temp_button);

        tempButton.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new EventCreationFragment())
                    .addToBackStack(null)
                    .commit();
        });

        requireActivity().findViewById(R.id.nav_bar).setVisibility(View.VISIBLE);

        return view;
    }
}