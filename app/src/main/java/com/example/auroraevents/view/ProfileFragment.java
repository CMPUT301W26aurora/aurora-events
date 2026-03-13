package com.example.auroraevents.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.auroraevents.R;
import com.example.auroraevents.model.User;

public class ProfileFragment extends Fragment {
    String deviceId;
    EditText nameEdit;
    EditText emailEdit;
    EditText phoneEdit;
    User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        deviceId = Settings.Secure.getString(view.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        nameEdit = view.findViewById(R.id.user_name);
        emailEdit = view.findViewById(R.id.user_email);
        phoneEdit = view.findViewById(R.id.user_phone_number);
        user = new User();
        user.setDeviceId(deviceId);
        if (!user.pull()) {
            Toast.makeText(view.getContext(), R.string.database_error_toast_text, Toast.LENGTH_SHORT).show();
        }
        nameEdit.setText(user.getName());
        emailEdit.setText(user.getEmail());
        phoneEdit.setText(user.getPhoneNumber());

        view.findViewById(R.id.confirm_button).setOnClickListener(v -> {
            if (nameEdit.getText().toString().isEmpty() || emailEdit.getText().toString().isEmpty()) {
                Toast.makeText(view.getContext(), R.string.mandatory_fields_toast_text, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(view.getContext(), "confirm", Toast.LENGTH_SHORT).show();
                /* Update user info */
                // set new values
                user.setName(nameEdit.getText().toString());
                user.setEmail(emailEdit.getText().toString());
                user.setPhoneNumber(phoneEdit.getText().toString());
                // update db
                if (!user.push()) {
                    Toast.makeText(view.getContext(), R.string.database_error_toast_text, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(view.getContext(), R.string.user_info_successfully_updated_toast_text, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
}