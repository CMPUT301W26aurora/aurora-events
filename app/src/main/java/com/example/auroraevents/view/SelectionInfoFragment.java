package com.example.auroraevents.view;

import static androidx.core.content.ContextCompat.getColor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.auroraevents.R;

public class SelectionInfoFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.fragment_selection_info, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.DarkGlassDialog);
        return builder
                .setView(view)
                .setPositiveButton("Okay", null)
                .create();
    }

    // Source - https://stackoverflow.com/a/27913325
    // Posted by Jared Rummler
    // Retrieved 2026-03-27, License - CC BY-SA 3.0
    @Override
    public void onStart() {
        super.onStart();
        assert getDialog() != null;
        // with help from https://developer.android.com/reference/androidx/core/content/ContextCompat.html#getColor(android.content.Context,int)
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(requireContext(), R.color.purple));
    }
}