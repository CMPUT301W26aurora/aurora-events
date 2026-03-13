package com.example.auroraevents.view;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.auroraevents.R;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserViewModel;

/**
 * This class controls the profile screen
 */
public class ProfileFragment extends Fragment {
    private EditText nameEdit;
    private EditText emailEdit;
    private EditText phoneEdit;
    private UserViewModel userViewModel;
    private Button adminToggle;
    private User user;
    private String TAG = "ProfileFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Controls the functionality of the screen
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        nameEdit = view.findViewById(R.id.user_name);
        emailEdit = view.findViewById(R.id.user_email);
        phoneEdit = view.findViewById(R.id.user_phone_number);
        adminToggle = view.findViewById(R.id.switch_to_admin_button);

        /* Get user information from the database */
        userViewModel.getSelectedItem().observe(requireActivity(), u -> {
            user = u;
            nameEdit.setText(user.getName());
            emailEdit.setText(user.getEmail());
            phoneEdit.setText(user.getPhoneNumber());
        });

        /* Update user information */
        view.findViewById(R.id.confirm_button).setOnClickListener(v -> {
            if (nameEdit.getText().toString().isEmpty() || emailEdit.getText().toString().isEmpty()) {
                Toast.makeText(view.getContext(), R.string.mandatory_fields_toast_text, Toast.LENGTH_SHORT).show();
            } else {
                /* Update user info */
                // check email
                if (!emailEdit.getText().toString().contains("@") || !emailEdit.getText().toString().contains("."))
                    Toast.makeText(view.getContext(), "Please provide a real email", Toast.LENGTH_SHORT).show();
                else {
                    // set new values
                    user.setName(nameEdit.getText().toString());
                    user.setEmail(emailEdit.getText().toString());
                    user.setPhoneNumber(phoneEdit.getText().toString());
                    userViewModel.selectItem(user);
                }
            }
        });

        /* Switch to admin mode */
        if (user == null || !user.getRole().equals(User.ROLE_ADMIN))
            adminToggle.setVisibility(GONE);
        else
            adminToggle.setVisibility(VISIBLE);

        adminToggle.setOnClickListener(v -> Toast.makeText(view.getContext(), "Admin screen not ready", Toast.LENGTH_SHORT).show());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        requireActivity().findViewById(R.id.nav_bar).setVisibility(View.VISIBLE);

        return view;
    }
}