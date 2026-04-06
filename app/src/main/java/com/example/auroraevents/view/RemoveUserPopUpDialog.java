package com.example.auroraevents.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * A dialog to remove a user from an organizer
 * @author Won Koh (Original)
 * @author Sean Ross (Fixed + Logic)
 */

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

        //nightmare of a block to add user deadlines, no time to make something readable
        setDeadlineButton.setOnClickListener(v->{
            Calendar c = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                Calendar deadline = Calendar.getInstance();
                deadline.set(year, month, dayOfMonth, 23, 59, 59);
                Timestamp ts = new Timestamp(deadline.getTime());
                FirebaseFirestore.getInstance()
                        .collection("Events")
                        .document(currentEventID)
                        .update("userDeadlines." + selectedUserID, ts)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Deadline updated", Toast.LENGTH_SHORT).show();
                            dismiss();
                        });
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
        alertDialog.setOnShowListener(d ->
                alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent)
        );

        return alertDialog;
    }
}
