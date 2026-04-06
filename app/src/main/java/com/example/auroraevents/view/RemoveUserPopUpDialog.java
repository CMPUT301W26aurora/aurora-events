package com.example.auroraevents.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.auroraevents.R;
import com.example.auroraevents.model.RegistrationList;
import com.example.auroraevents.model.UserAdapter;
import com.example.auroraevents.server.EventDb;

public class RemoveUserPopUpDialog extends DialogFragment {
    private String selectedUserID;
    private String currentEventID;
    private RegistrationList registrationList;
    private UserAdapter userAdapter;
    public static RemoveUserPopUpDialog newInstance(RegistrationList registrationList, String selectedUserID, String currentEventId ) {
        RemoveUserPopUpDialog dialog = new RemoveUserPopUpDialog();
        dialog.selectedUserID = selectedUserID;
        dialog.registrationList = registrationList;
        dialog.currentEventID = currentEventId;

        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.delete_user_confirm_popup, null);

        Button confirmButton = view.findViewById(R.id.confirm_button);
        Button setDeadlineButton = view.findViewById(R.id.set_deadline_button);
        ImageButton cancel = view.findViewById(R.id.cancel_button);



        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
        // When Organizer presses confirm, the selected user is removed from the event

        cancel.setOnClickListener(v->alertDialog.dismiss());

        confirmButton.setOnClickListener(v ->
                EventDb.getInstance().moveUserBetweenLists(
                    currentEventID,
                    EventDb.LIST_SELECTED,
                    EventDb.LIST_REMOVED,
                    selectedUserID,
                    alertDialog::dismiss,
                    e -> Log.e("Dialog", "Move failed", e)
                )
        );

        setDeadlineButton.setOnClickListener(v-> Toast.makeText(getContext(), "does nothing for now", Toast.LENGTH_SHORT).show());

        alertDialog.setOnShowListener(d ->
                alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent)
        );

        return alertDialog;
    }
}
