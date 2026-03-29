package com.example.auroraevents.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.User;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;

public class FilterUserPopUpDialog extends DialogFragment {

    private Event currentEvent;
    private ArrayList<User> userList = new ArrayList<User>();

    private ListView userListView;
    public static FilterUserPopUpDialog newInstance(Event currentEvent, ArrayList<User> userList, ListView userListView) {
        FilterUserPopUpDialog dialog = new FilterUserPopUpDialog();
        dialog.currentEvent = currentEvent;
        dialog.userList = userList;
        dialog.userListView = userListView;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.user_filter_popup, null);

        SwitchMaterial switchParticipating = view.findViewById(R.id.particpating_switch);
        SwitchMaterial switchRejected      = view.findViewById(R.id.rejected_switch);
        SwitchMaterial switchInvited       = view.findViewById(R.id.invited_switch);
        SwitchMaterial switchWaiting       = view.findViewById(R.id.waiting_switch);
        SwitchMaterial switchCancelled     = view.findViewById(R.id.cancelled_switch);

        Button confirmButton = view.findViewById(R.id.confirm_button);

        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();

        // After pressing Confirm, Update userlist with the filter
        confirmButton.setOnClickListener(v -> {
            ArrayList<User> filteredUsers = new ArrayList<>();
            if (switchParticipating.isChecked()) filteredUsers.addAll(currentEvent.getAttendingListOfUsers());
            if (switchRejected.isChecked())      filteredUsers.addAll(currentEvent.getDeclinedListOfUsers());
            if (switchInvited.isChecked())       filteredUsers.addAll(currentEvent.getSelectedListOfUsers());
            if (switchWaiting.isChecked())       filteredUsers.addAll(currentEvent.getWaitingListOfUsers());
            if (switchCancelled.isChecked())     filteredUsers.addAll(currentEvent.getCancelledListOfUsers());
            userList.addAll(filteredUsers);
            userListView.deferNotifyDataSetChanged();
            alertDialog.dismiss();
                });

        alertDialog.setOnShowListener(d ->
                alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent)
        );

        return alertDialog;
    }
}