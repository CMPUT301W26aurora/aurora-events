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

import com.example.auroraevents.R;
import com.example.auroraevents.model.RegistrationList;
import com.example.auroraevents.model.UserAdapter;

public class RemoveUserPopUpDialog extends DialogFragment {
    private RegistrationList registrationList;
    private String selectedUserID;
    private UserAdapter userListAdapter;
    public static RemoveUserPopUpDialog newInstance(RegistrationList registrationList, String selectedUserID, UserAdapter userListAdapter) {
        RemoveUserPopUpDialog dialog = new RemoveUserPopUpDialog();
        dialog.registrationList = registrationList;
        dialog.selectedUserID = selectedUserID;
        dialog.userListAdapter = userListAdapter;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.delete_user_confirm_popup, null);

        Button confirmButton = view.findViewById(R.id.confirm_button);

        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
        // When Organizer presses confirm, the selected user is cancelled from the event
        confirmButton.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        alertDialog.setOnShowListener(d ->
                alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent)
        );

        return alertDialog;
    }
}
