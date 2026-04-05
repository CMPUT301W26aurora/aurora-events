package com.example.auroraevents.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import com.example.auroraevents.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class FilterUserPopUpDialog extends DialogFragment {

    public static FilterUserPopUpDialog newInstance() {
        return new FilterUserPopUpDialog();
    }

    // ── Filter callback ────────────────────────────────────────────────────

    /**
     * Notified when the organizer confirms a filter selection.
     */
    public interface OnFilterAppliedListener {
        void onFilterApplied(List<String> selectedStatuses);
    }

    // ── Export callback ────────────────────────────────────────────────────

    /**
     * Receives the same status list that would be used for filtering so the
     * export always reflects whatever is currently toggled. If no toggles are on
     * the list will be empty, which the host fragment interprets as "export all".
     */
    public interface OnExportRequestedListener {
        void onExportRequested(List<String> selectedStatuses);
    }

    private OnFilterAppliedListener filterListener;
    private OnExportRequestedListener exportListener;

    public void setOnFilterAppliedListener(OnFilterAppliedListener listener) {
        this.filterListener = listener;
    }

    public void setOnExportRequestedListener(OnExportRequestedListener listener) {
        this.exportListener = listener;
    }

    // ── Dialog creation ────────────────────────────────────────────────────

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.user_filter_popup, null);

        SwitchMaterial switchParticipating = view.findViewById(R.id.particpating_switch);
        SwitchMaterial switchRejected      = view.findViewById(R.id.rejected_switch);
        SwitchMaterial switchInvited       = view.findViewById(R.id.invited_switch);
        SwitchMaterial switchWaiting       = view.findViewById(R.id.waiting_switch);
        SwitchMaterial switchCancelled     = view.findViewById(R.id.cancelled_switch);
        Button confirmButton               = view.findViewById(R.id.confirm_button);
        Button exportButton                = view.findViewById(R.id.export_to_csv);

        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
        alertDialog.setOnShowListener(d ->
                alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent)
        );

        confirmButton.setOnClickListener(v -> {
            List<String> status = buildStatusList(
                    switchWaiting, switchInvited, switchParticipating,
                    switchCancelled, switchRejected);
            if (filterListener != null) filterListener.onFilterApplied(status);
            dismiss();
        });

        exportButton.setOnClickListener(v -> {
            List<String> status = buildStatusList(
                    switchWaiting, switchInvited, switchParticipating,
                    switchCancelled, switchRejected);
            if (exportListener != null) exportListener.onExportRequested(status);
            dismiss();
        });

        return alertDialog;
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /**
     * Reads the current state of all five switches and returns the corresponding
     * status label list. Called by both the Confirm and Export buttons so the
     * mapping stays in one place.
     *
     * @param waiting       The "Waiting" switch.
     * @param invited       The "Invited / Selected" switch.
     * @param participating The "Participating / Accepted" switch.
     * @param cancelled     The "Cancelled" switch.
     * @param rejected      The "Rejected / Removed" switch.
     * @return A list of status label strings matching the checked switches.
     */
    private List<String> buildStatusList(SwitchMaterial waiting,
                                         SwitchMaterial invited,
                                         SwitchMaterial participating,
                                         SwitchMaterial cancelled,
                                         SwitchMaterial rejected) {
        List<String> status = new ArrayList<>();
        if (waiting.isChecked())       status.add("Waiting");
        if (invited.isChecked())       status.add("Selected");
        if (participating.isChecked()) status.add("Accepted");
        if (cancelled.isChecked())     status.add("Cancelled");
        if (rejected.isChecked())      status.add("Removed");
        return status;
    }
}