package com.example.auroraevents.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.auroraevents.R;
import com.example.auroraevents.model.User;
import com.example.auroraevents.server.UserDb;

import java.util.concurrent.atomic.AtomicReference;

public class ProfileFragment extends Fragment {
    String deviceId;
    EditText nameEdit;
    EditText emailEdit;
    EditText phoneEdit;
    User user;
    String TAG = "ProfileFragment";

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

        /* Get user information from the database */
        AtomicReference<User> userRef = new AtomicReference<>(null);
        UserDb.getInstance().getUser(deviceId,
                u -> {
                    userRef.set(u);
                    Log.d(TAG, "User got!");
                },
                e -> Log.e(TAG, "User get failed")
        );


        if (userRef.get() == null) {
            Toast.makeText(view.getContext(), R.string.database_error_toast_text, Toast.LENGTH_SHORT).show();
            user = new User(deviceId, null, null, null, User.ROLE_ENTRANT);
        } else {
            user = userRef.get();
            nameEdit.setText(user.getName());
            emailEdit.setText(user.getEmail());
            phoneEdit.setText(user.getPhoneNumber());
        }

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
                UserDb.getInstance().updateUser(user,
                        () -> Log.d(TAG, "user updated!"),
                        e -> Log.e(TAG, "user update failed")
                );
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