package com.example.auroraevents.view;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.auroraevents.R;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * This class controls the profile screen
 * @author Jared Strandlund
 */
public class ProfileFragment extends Fragment {
    private EditText nameEdit;
    private EditText emailEdit;
    private EditText phoneEdit;
    private UserViewModel userViewModel;
    private Button adminToggle;
    private User user;
    private final String TAG = "ProfileFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Returns correctly formatted phone number
     * @param input Phone number to parse
     * @return empty string when invalid input
     */
    private String parsePhoneNumber(String input) {
        if (input.length() < 10) return "";
        List<String> intermediate = new ArrayList<>(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '*' || c == '#' || c == '+')
                intermediate.add(String.valueOf(Character.toLowerCase(c)));
            else if (c != '(' && c != ')' && c != '-' && c != ' ')
                return "";
        }

        if (intermediate.size() < 10) return "";
        String[] letters = {"abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};
        String[] numbers = {"2", "3", "4", "5", "6", "7", "8", "9"};
        StringBuilder output = new StringBuilder();
        for (String c : intermediate) {
            if (Character.isLetter(c.charAt(0))) {
                // convert to number
                for (int i = 0; i < letters.length; i++) {
                    if (letters[i].contains(c))
                        output.append(numbers[i]);
                }
            } else
                output.append(c);
        }

        if (output.length() < 10)
            return "";
        else
            return output.toString();
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
            String name = nameEdit.getText().toString();
            String email = emailEdit.getText().toString();
            String phoneNumber = phoneEdit.getText().toString();
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(view.getContext(), R.string.mandatory_fields_toast_text, Toast.LENGTH_SHORT).show();
            } else {
                /* Update user info */
                // check phone number
                boolean validPhoneNumber = true;
                if (!phoneNumber.isEmpty()) {
                    phoneNumber = parsePhoneNumber(phoneNumber);
                    validPhoneNumber = !phoneNumber.isEmpty();
                }
                if (!validPhoneNumber)
                    Toast.makeText(view.getContext(), "Please provide a valid phone number", Toast.LENGTH_SHORT).show();
                // check email
                else if (!email.contains("@") || !email.contains(".") || email.indexOf('@') > email.lastIndexOf('.'))
                    Toast.makeText(view.getContext(), "Please provide a real email", Toast.LENGTH_SHORT).show();
                else {
                    // set new values
                    user.setName(nameEdit.getText().toString());
                    user.setEmail(emailEdit.getText().toString());
                    user.setPhoneNumber(phoneNumber);
                    userViewModel.selectItem(user);
                }
            }
        });

        /* Delete Profile */
        view.findViewById(R.id.delete_profile_button).setOnClickListener(v -> {
            if (user.deleteUser() > 0)
                Log.e(TAG, "User delete failed");
            else
                userViewModel.selectItem(user);
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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
}