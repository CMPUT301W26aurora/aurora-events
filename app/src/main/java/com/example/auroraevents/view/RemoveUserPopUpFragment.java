package com.example.auroraevents.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.example.auroraevents.R;
public class RemoveUserPopUpFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView= inflater.inflate(R.layout.delete_user_confirm_popup, null);
        builder.setPositiveButton(getView().findViewById(R.id.confirm_button), (dialog, which) -> {} );
        builder.setView(dialogView);
        builder.show();

        return builder.create();
    }
}
