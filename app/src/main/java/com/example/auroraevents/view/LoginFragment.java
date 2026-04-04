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
import androidx.lifecycle.ViewModelProvider;

import com.example.auroraevents.R;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserViewModel;
import com.example.auroraevents.server.UserDb;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


public class LoginFragment extends Fragment {
    String deviceId;
    EditText nameEdit;
    EditText emailEdit;
    EditText phoneEdit;

    UserViewModel userViewModel;

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

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        view.findViewById(R.id.confirm_button).setOnClickListener(v -> {
            if (nameEdit.getText().toString().isEmpty() || emailEdit.getText().toString().isEmpty()) {
                Toast.makeText(view.getContext(), R.string.mandatory_fields_toast_text, Toast.LENGTH_SHORT).show();
            } else {
                v.setEnabled(false);
                Toast.makeText(view.getContext(), "confirm", Toast.LENGTH_SHORT).show();
                /* Update user info */
                // create new User
                User user = new User(
                        deviceId,
                        nameEdit.getText().toString(),
                        emailEdit.getText().toString(),
                        phoneEdit.getText().toString(),
                        User.ROLE_ENTRANT,
                        false
                );
                //https://medium.com/@yossisegev/understanding-activity-runonuithread-e102d388fe93
                UserDb.getInstance().addUser(user, ()->{
                    requireActivity().runOnUiThread(()->{
                        Toast.makeText(view.getContext(), R.string.user_info_successfully_updated_toast_text, Toast.LENGTH_SHORT).show();

                        userViewModel.selectItem(user);


                        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                            getParentFragmentManager().popBackStack();
                        } else {
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new EventFragment())
                                    .commit();
                        }

                    });
                }, e->{
                    requireActivity().runOnUiThread(() -> {
                        v.setEnabled(true);
                        Toast.makeText(getContext(), R.string.database_error_toast_text, Toast.LENGTH_SHORT).show();
                    });
                });
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }
}
