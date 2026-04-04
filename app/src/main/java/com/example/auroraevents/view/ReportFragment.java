package com.example.auroraevents.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.auroraevents.R;

import java.io.Serializable;

public class ReportFragment extends DialogFragment {
    public interface Callback extends Serializable { void onCallback(); }

    public static ReportFragment newInstance(Callback callback) {
        Bundle args = new Bundle();
        args.putSerializable("callback", callback);

        ReportFragment fragment = new ReportFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.fragment_report_confirm, null);

        view.findViewById(R.id.cancel_button).setOnClickListener(v -> dismiss());

        assert getArguments() != null;
        Callback callback = (Callback) getArguments().getSerializable("callback");
        assert callback != null;
        view.findViewById(R.id.report_button).setOnClickListener(v -> {
            callback.onCallback();
            dismiss();
        });

        return new AlertDialog.Builder(requireActivity(), R.style.DarkGlassDialog)
                .setView(view)
                .create();
    }
}
