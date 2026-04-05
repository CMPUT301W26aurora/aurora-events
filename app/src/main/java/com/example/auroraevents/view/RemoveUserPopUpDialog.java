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
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.auroraevents.R;
import com.example.auroraevents.model.RegistrationList;
import com.example.auroraevents.model.UserAdapter;
import com.example.auroraevents.server.EventDb;

public class RemoveUserPopUpDialog extends DialogFragment {
    private String selectedUserID;
    private RegistrationList registrationList;
    private UserAdapter userAdapter;
    public static RemoveUserPopUpDialog newInstance(RegistrationList registrationList, String selectedUserID, UserAdapter userListAdapter) {
        RemoveUserPopUpDialog dialog = new RemoveUserPopUpDialog();
        dialog.selectedUserID = selectedUserID;
        dialog.registrationList = registrationList;
        dialog.userAdapter = userListAdapter;

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
        // When Organizer presses confirm, the selected user is cancelled from the event

        cancel.setOnClickListener(v->alertDialog.dismiss());

        confirmButton.setOnClickListener(v -> {
            registrationList.addToCancelledList(selectedUserID, new RegistrationList.OnDbUpdateListener() {
                        @Override
                        public void onSuccess() {
                            //do nothing
                        }

                        @Override
                        public void onFailure() {
                            //do Nothing
                        }

                        @Override
                        public void onComplete(RegistrationList.RegistrationResult result) {
                            switch (result) {
                                case SUCCESS:
                                    Toast.makeText(getContext(), "Removed User From Pool", Toast.LENGTH_SHORT).show();
                                    userAdapter.notifyDataSetChanged();
                                    alertDialog.dismiss();
                                    break;
                                case BLOCKED:
                                    Toast.makeText(getContext(), "User is already removed.", Toast.LENGTH_SHORT).show();
                                    alertDialog.dismiss();
                                    break;
                                case DATABASE_ERROR:
                                    Toast.makeText(getContext(), "Connection error. Try again.", Toast.LENGTH_SHORT).show();
                                    break;

                            }
                        }
                    }

                    , registrationList.getSelectedList(), EventDb.LIST_CANCELLED);
        });

        setDeadlineButton.setOnClickListener(v->{
            Toast.makeText(getContext(), "does nothing for now", Toast.LENGTH_SHORT).show();
        });

        alertDialog.setOnShowListener(d ->
                alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent)
        );

        return alertDialog;
    }
}
