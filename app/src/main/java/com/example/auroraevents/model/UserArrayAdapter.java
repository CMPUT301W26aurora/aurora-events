package com.example.auroraevents.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.auroraevents.R;
import com.example.auroraevents.view.RemoveUserPopUpDialog;
import com.example.auroraevents.view.UserListFragment;

import java.util.List;

/**
 * Might switch this to a recyclerView
 */
public class UserArrayAdapter extends ArrayAdapter<User> {
    private Event currentEvent;
    private ImageButton deleteButton;
    private UserListFragment parentFragment;
    private UserArrayAdapter userListAdapter;
    public UserArrayAdapter(Context context, List<User> users, Event event, UserArrayAdapter userListAdapter, UserListFragment parentFragment) {
        super(context, 0, users);
        currentEvent = event;
        this.parentFragment = parentFragment;
        this.userListAdapter = userListAdapter;
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
        }

        // Remove user button
        deleteButton = view.findViewById(R.id.delete_user_button);
        deleteButton.setOnClickListener(v -> {
            RemoveUserPopUpDialog dialog = RemoveUserPopUpDialog.newInstance(
                    currentEvent.registrationList,
                    getItem(position).getDeviceId(),
                    userListAdapter
            );
            dialog.show(parentFragment.getParentFragmentManager(), getItem(position).getDeviceId());
        });

        return view;
    }
}