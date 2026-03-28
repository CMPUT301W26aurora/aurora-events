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

public class PermanentWarningFragment extends DialogFragment {
    public interface Callback extends Serializable { void onCallback(); }

    public static PermanentWarningFragment newInstance(Callback callback) {
        Bundle args = new Bundle();
        args.putSerializable("callback", callback);

        PermanentWarningFragment fragment = new PermanentWarningFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.fragment_permanent_warning, null);

        view.findViewById(R.id.permanent_warning_back_button).setOnClickListener(v -> dismiss() /* from https://developer.android.com/guide/fragments/dialogs#lifecycle */);

        assert getArguments() != null;
        Callback callback = (Callback) getArguments().getSerializable("callback");
        assert callback != null;
        view.findViewById(R.id.permanent_warning_delete_button).setOnClickListener(v -> {
            callback.onCallback();
            // from https://developer.android.com/guide/fragments/dialogs#lifecycle
            dismiss();
        });

        return new AlertDialog.Builder(requireActivity(), R.style.DarkGlassDialog)
                .setView(view)
                .create();
    }
}
