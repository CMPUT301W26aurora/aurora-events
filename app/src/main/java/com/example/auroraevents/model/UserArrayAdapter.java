package com.example.auroraevents.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.auroraevents.R;
import com.example.auroraevents.view.FilterUserPopUpFragment;
import com.example.auroraevents.view.RemoveUserPopUpFragment;

import java.util.ArrayList;

public class UserArrayAdapter extends ArrayAdapter<User> {
    private Event currentEvent;
    private ImageButton deleteButton;
    public UserArrayAdapter(Context context, ArrayList<User> users, Event event) {
        super(context, 0, users);
        currentEvent = event;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertview, @NonNull ViewGroup parent) {
        View view;
        if (convertview == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.user_list_item, parent, false);
        } else {
            view = convertview;
        }
        User user = getItem(position);
        TextView userName = view.findViewById(R.id.user_name);
        TextView userStatus = view.findViewById(R.id.user_status);

        if (user != null) {
            userName.setText(user.getName());
            String status = user.getStatus();
            if (status != null) {
                userStatus.setText(status);
            }
        }

        // Delete user button TODO popup window, and getting the user id so the confirm button removes it
        deleteButton = view.findViewById(R.id.delete_user_button);
        deleteButton.setOnClickListener(v -> {
            //TODO
        });

        return view;
    }
}

// Resources used https://androidforbeginners.blogspot.com/2010/03/clicking-buttons-in-listview-row.html