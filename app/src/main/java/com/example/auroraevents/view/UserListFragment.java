package com.example.auroraevents.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.fragment.app.DialogFragment;

import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserAdapter;

import java.util.List;

/**
 * Displays list of all entrants with any and all status
 * Allows Organizers to filter and cancel entrants
 */
public class UserListFragment extends DialogFragment {
    private Event currentEvent;
    private List<User> userList;
    private UserAdapter userListAdapter;
    private ListView userListView;
    private Button doneButton, mapButton, filterButton, sortButton;
    private ImageButton deleteButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.user_list_fragment, container, false);
        return view;
    }

}
