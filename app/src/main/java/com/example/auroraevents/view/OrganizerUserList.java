package com.example.auroraevents.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.auroraevents.R;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserAdapter;
import com.example.auroraevents.server.CommentDb;

import java.util.ArrayList;

/**
 * Displays list of all entrants with any and all status
 * Allows Organizers to filter and cancel entrants
 */
public class OrganizerUserList extends DialogFragment {
    private UserAdapter adapter;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer_user_list, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.entrants_list_org);
        ImageButton deleteButton = view.findViewById(R.id.delete_user_button_org_item);
        adapter = new UserAdapter(new ArrayList<>(), false, new UserAdapter.OnUserInteractionListener() {
            @Override
            public void Onclick(User user) {

            }
        });
    }

}
