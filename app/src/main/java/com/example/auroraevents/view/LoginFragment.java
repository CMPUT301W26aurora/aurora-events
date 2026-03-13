package com.example.auroraevents.view;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.auroraevents.R;
import com.example.auroraevents.model.User;
import com.example.auroraevents.server.UserDb;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


public class LoginFragment extends Fragment {
    String deviceId;
    EditText nameEdit;
    EditText emailEdit;
    EditText phoneEdit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        deviceId = Settings.Secure.getString(view.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        nameEdit = view.findViewById(R.id.user_name);
        emailEdit = view.findViewById(R.id.user_email);
        phoneEdit = view.findViewById(R.id.user_phone_number);

        view.findViewById(R.id.confirm_button).setOnClickListener(v -> {
            if (nameEdit.getText().toString().isEmpty() || emailEdit.getText().toString().isEmpty()) {
                Toast.makeText(view.getContext(), R.string.mandatory_fields_toast_text, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(view.getContext(), "confirm", Toast.LENGTH_SHORT).show();
                /* Update user info */
                // create new User
                User user = new User(
                        deviceId,
                        nameEdit.getText().toString(),
                        emailEdit.getText().toString(),
                        phoneEdit.getText().toString(),
                        User.ROLE_ENTRANT
                );
                // update db
                CountDownLatch latch = new CountDownLatch(1);
                AtomicReference<Boolean> status = new AtomicReference<>(true);
                UserDb.getInstance().addUser(user,
                        latch::countDown,
                        e -> {
                            status.set(false);
                            latch.countDown();
                        }
                );

                try {
                    if (!latch.await(10, TimeUnit.SECONDS)) {
                        Log.w("LoginFragment", "user update timed out");
                    }
                } catch (InterruptedException e) {
                    Log.w("LoginFragment", "user update interrupted");
                }

                if (status.get()) {
                    Toast.makeText(view.getContext(), R.string.user_info_successfully_updated_toast_text, Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .remove(this)
                            .commit();
                } else {
                    // show network error
                    Toast.makeText(view.getContext(), R.string.database_error_toast_text, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }
}
