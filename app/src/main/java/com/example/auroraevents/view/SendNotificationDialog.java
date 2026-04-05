package com.example.auroraevents.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.auroraevents.R;
import com.example.auroraevents.model.RegistrationList;
import com.example.auroraevents.server.NotificationSender;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for organizers to send a custom push notification
 * to selected registration lists for their event.
 */
public class SendNotificationDialog extends DialogFragment {

    private static final String ARG_EVENT_ID   = "eventId";
    private static final String ARG_EVENT_NAME = "eventName";

    private RegistrationList registrationList;

    public static SendNotificationDialog newInstance(String eventId, String eventName, RegistrationList registrationList) {
        SendNotificationDialog dialog = new SendNotificationDialog();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        dialog.setArguments(args);
        dialog.registrationList = registrationList;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String eventId   = getArguments().getString(ARG_EVENT_ID);
        String eventName = getArguments().getString(ARG_EVENT_NAME);

        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_send_notification, null);

        // List toggles
        SwitchMaterial switchParticipating = view.findViewById(R.id.switch_participating);
        SwitchMaterial switchRejected      = view.findViewById(R.id.switch_rejected);
        SwitchMaterial switchInvited       = view.findViewById(R.id.switch_invited);
        SwitchMaterial switchWaiting       = view.findViewById(R.id.switch_waiting);
        SwitchMaterial switchCancelled     = view.findViewById(R.id.switch_cancelled);

        EditText messageField = view.findViewById(R.id.notification_message);
        Button   btnSend      = view.findViewById(R.id.btn_send);

        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();

        alertDialog.setOnShowListener(d ->
                alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent)
        );

        btnSend.setOnClickListener(v -> {
            String message = messageField.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
                return;
            }

            // Collect device IDs from selected lists
            List<String> recipients = new ArrayList<>();
            if (switchParticipating.isChecked()) recipients.addAll(registrationList.getAttendingList());
            if (switchRejected.isChecked())      recipients.addAll(registrationList.getDeclinedList());
            if (switchInvited.isChecked())       recipients.addAll(registrationList.getSelectedUserStrings());
            if (switchWaiting.isChecked())       recipients.addAll(registrationList.getWaitingList());
            if (switchCancelled.isChecked())     recipients.addAll(registrationList.getCancelledList());

            if (recipients.isEmpty()) {
                Toast.makeText(requireContext(), "No recipients selected", Toast.LENGTH_SHORT).show();
                return;
            }

            // Capture app context before dialog dismisses so Toast works in async callbacks
            Context appContext = requireContext().getApplicationContext();
            alertDialog.dismiss();

            NotificationSender.send(
                    recipients,
                    eventName,
                    message,
                    eventId,
                    () -> Toast.makeText(appContext, "Notifications sent!", Toast.LENGTH_SHORT).show(),
                    e  -> Toast.makeText(appContext, "Failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        });

        return alertDialog;
    }
}