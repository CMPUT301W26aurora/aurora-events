package com.example.auroraevents.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.Organizer;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserArrayAdapter;

import java.util.ArrayList;

public class UserListFragment extends Fragment {
    private Event currentEvent;
    private Organizer eventOrganizer;
    private ArrayList<User> userList;
    private UserArrayAdapter userListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.user_list_fragment, container, false);

        userList = currentEvent.getListOfAllUsers();
        return view;
    }
}
