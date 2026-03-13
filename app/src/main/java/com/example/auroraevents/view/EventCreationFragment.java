package com.example.auroraevents.view;

import static androidx.core.util.TypedValueCompat.dpToPx;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.Organizer;
import com.example.auroraevents.model.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import android.provider.Settings;

public class EventCreationFragment extends Fragment {
    private ImageButton backButton;
    private Button addImageButton;
    private TextInputEditText eventNameInput;
    private TextInputEditText eventDescInput;
    private TextInputEditText eventCapInput;
    private Button locationButton;
    private Button geolocationLockButton;
    private Button startDateButton;
    private Button endDateButton;
    private Button dateButton;
    private Button confirmButton;
    private Fragment eventListFragment;
    private String eventName;
    private String eventDescription;
    private String eventCap;
    private String location;
    private String date;
    private String registerStart;
    private String registerEnd;
    private int capacity;
    private User user;
    private Organizer organizer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_creation, container, false);

        // Get device ID
        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Button and input setup
        backButton = view.findViewById(R.id.backButton);
        addImageButton = view.findViewById(R.id.btn_add_image);
        eventNameInput = view.findViewById(R.id.et_event_name);
        eventDescInput = view.findViewById(R.id.et_event_desc);
        eventCapInput = view.findViewById(R.id.et_event_capacity);
        locationButton = view.findViewById(R.id.btn_select_location);
        geolocationLockButton = view.findViewById(R.id.btn_geolocation_lock);
        startDateButton = view.findViewById(R.id.btn_start_date);
        endDateButton = view.findViewById(R.id.btn_end_date);
        dateButton = view.findViewById(R.id.btn_signup_deadline);
        confirmButton = view.findViewById(R.id.btn_confirm);

        // Hide nav bar
        requireActivity().findViewById(R.id.nav_bar).setVisibility(View.GONE);

        // Get organizer
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if (User.ROLE_ORGANIZER.equals(role)) {
                            organizer = documentSnapshot.toObject(Organizer.class);
                            user = organizer;
                        }
                    }
                });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        locationButton.setOnClickListener(v ->
                showInputDialog(locationButton, "Location", val -> location = val));

        startDateButton.setOnClickListener(v ->
                showInputDialog(startDateButton, "Sign-Up Start", val -> registerStart = val));

        endDateButton.setOnClickListener(v ->
                showInputDialog(endDateButton, "Sign-Up End", val -> registerEnd = val));

        dateButton.setOnClickListener(v ->
                showInputDialog(dateButton, "Event Date", val -> date = val));

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get values from text input
                eventName = eventNameInput.getText().toString().trim();
                eventDescription = eventDescInput.getText().toString().trim();
                eventCap = eventCapInput.getText().toString().trim();

                // Validate required inputs
                if (eventName.isEmpty()) {
                    eventNameInput.setError("Event name is required");
                    return;
                }

                if (eventCap.isEmpty()) {
                    eventCapInput.setError("Capacity is required");
                    return;
                }

                // Validate numbers
                int capacityValue;
                try {
                    capacityValue = Integer.parseInt(eventCap);
                } catch (NumberFormatException e) {
                    eventCapInput.setError("Please enter a valid number");
                    return;
                }

                //
                if (location == null || date == null) {
                    return;
                }

                // Create and add event
                if (organizer != null) {
                    organizer.CreateEvent(
                            organizer.getDeviceId(),
                            eventName,
                            eventDescription,
                            date,
                            registerStart,
                            registerEnd,
                            location,
                            capacityValue
                    );

                    // Navigate back if successful
                    getParentFragmentManager().popBackStack();
                } else {
                    // Firestore still loading/failed
                    android.widget.Toast.makeText(getContext(),
                            "User data not loaded yet. Please try again.",
                            android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Show nav bar again when leaving this fragment
        View navBar = requireActivity().findViewById(R.id.nav_bar);
        if (navBar != null) {
            navBar.setVisibility(View.VISIBLE);
        }
    }

    private void showInputDialog(Button targetButton, String hint, java.util.function.Consumer<String> onConfirm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_input, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.gravity = Gravity.CENTER;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setAttributes(params);
        }

        TextInputEditText input = dialogView.findViewById(R.id.dialog_input);
        TextInputLayout inputLayout = dialogView.findViewById(R.id.dialog_input_layout);
        Button btnCancel = dialogView.findViewById(R.id.dialog_btn_cancel);
        Button btnConfirm = dialogView.findViewById(R.id.dialog_btn_confirm);

        inputLayout.setHint(hint);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String enteredText = input.getText() != null ? input.getText().toString().trim() : "";
            if (enteredText.isEmpty()) {
                inputLayout.setError("This field is required");
            } else {
                targetButton.setText(enteredText);
                onConfirm.accept(enteredText);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
