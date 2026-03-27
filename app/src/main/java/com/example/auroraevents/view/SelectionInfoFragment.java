package com.example.auroraevents.view;

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
                .create();
    }
}
