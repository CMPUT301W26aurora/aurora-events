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
import com.example.auroraevents.model.RegistrationList;
import com.example.auroraevents.model.SelectedUser;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserAdapter;
import com.example.auroraevents.model.UserAdapterWrapper;
import com.example.auroraevents.server.EventDb;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class FilterUserPopUpDialog extends DialogFragment {

    private Event currentEvent;
    private List<User> userList;
    private UserAdapter userAdapter;
    public static FilterUserPopUpDialog newInstance(Event currentEvent, List<User> userList, UserAdapter userAdapter) {
        FilterUserPopUpDialog dialog = new FilterUserPopUpDialog();
        dialog.currentEvent = currentEvent;
        dialog.userList = userList;
        dialog.userAdapter = userAdapter;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = getLayoutInflater().inflate(R.layout.user_filter_popup, null);

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
            List<UserAdapterWrapper> filteredWrappers = new ArrayList<>();
            RegistrationList reg = currentEvent.getRegistrationList();


            if (switchWaiting.isChecked()) {
                for (String id : reg.getWaitingList()) {
                    filteredWrappers.add(new UserAdapterWrapper(findUserFromPool(id), "Waiting", null));
                }
            }


            if (switchInvited.isChecked()) {
                for (SelectedUser su : reg.getSelectedList()) {
                    filteredWrappers.add(new UserAdapterWrapper(findUserFromPool(su.getUserId()), "Selected", su.getSelectedAt()));
                }
            }


            if (switchParticipating.isChecked()) {
                for (String id : reg.getAttendingList()) {
                    filteredWrappers.add(new UserAdapterWrapper(findUserFromPool(id), "Attending", null));
                }
            }


            userAdapter.setUserList(filteredWrappers);
            userAdapter.notifyDataSetChanged();

            alertDialog.dismiss();
        });


        alertDialog.setOnShowListener(d -> {
            assert alertDialog.getWindow() != null;
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        });

        return alertDialog;
    }

    /**
     * Helper Function that finds a user from the passed user pool by id
     * @param id the id to be queried
     * @return the user in question
     */
    private User findUserFromPool(String id){
        for (User u : userList){
            if(id.equals(u.getName())){
                return u;
            }

        }
        return null;
    }
}