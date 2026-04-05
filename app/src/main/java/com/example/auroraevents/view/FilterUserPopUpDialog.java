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
import java.util.ArrayList;
import java.util.List;

public class FilterUserPopUpDialog extends DialogFragment {
    public static FilterUserPopUpDialog newInstance() {
        FilterUserPopUpDialog dialog = new FilterUserPopUpDialog();
        return dialog;
    }
    public interface OnFilterAppliedListener {
        void onFilterApplied(List<String> selectedStatuses);
    }
    private OnFilterAppliedListener listener;
    public void setOnFilterAppliedListener(OnFilterAppliedListener listener) {
        this.listener = listener;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.user_filter_popup, null);
        Switch switchParticipating = view.findViewById(R.id.particpating_switch);
        Switch switchRejected      = view.findViewById(R.id.rejected_switch);
        Switch switchInvited       = view.findViewById(R.id.invited_switch);
        Switch switchWaiting       = view.findViewById(R.id.waiting_switch);
        Switch switchCancelled     = view.findViewById(R.id.cancelled_switch);
        Button confirmButton = view.findViewById(R.id.confirm_button);
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
        List<String> status = new ArrayList<>();
        confirmButton.setOnClickListener(v -> {
            if (switchWaiting.isChecked())       status.add("Waiting");
            if (switchInvited.isChecked())       status.add("Selected");
            if (switchParticipating.isChecked()) status.add("Accepted");
            if (switchCancelled.isChecked())     status.add("Cancelled");
            if (switchRejected.isChecked())      status.add("Removed");
            if (listener != null) {
                listener.onFilterApplied(status);
            }
            dismiss();
        });

        return alertDialog;
    }
}