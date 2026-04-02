package com.example.auroraevents.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.example.auroraevents.LocationToggleListener;
import com.example.auroraevents.R;
import com.example.auroraevents.model.Organizer;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import android.provider.Settings;
import android.widget.Toast;

public class EventCreationFragment extends Fragment {
    //TODO 4: copy into edit event fragment
    private ImageButton backButton;
    private Button addImageButton;
    private TextInputEditText eventNameInput;
    private TextInputEditText eventDescInput;
    private TextInputEditText eventCapInput;
    private Button locationButton;
    private Button geolocationButton;
    private Button startDateButton;
    private Button endDateButton;
    private Button dateButton;
    private Button confirmButton;
    private String eventName;
    private String eventDescription;
    private String price;
    private String eventCap;
    private String location;
    private boolean geolocationRequired;
    private String date;
    private String registerStart;
    private String registerEnd;
    private Organizer organizer;
    private User user;
    private UserViewModel userViewModel;

    private LocationToggleListener locationToggleListener;
    public boolean geolocationToggled;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof LocationToggleListener) {
            locationToggleListener = (LocationToggleListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement LocationToggleListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO 3: update to add price, geolocation requirement
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_creation, container, false);

        // Button and input setup
        backButton = view.findViewById(R.id.backButton);
        addImageButton = view.findViewById(R.id.btn_add_image);
        eventNameInput = view.findViewById(R.id.et_event_name);
        eventDescInput = view.findViewById(R.id.et_event_desc);
        eventCapInput = view.findViewById(R.id.et_event_capacity);
        locationButton = view.findViewById(R.id.btn_select_location);
        geolocationButton = view.findViewById(R.id.btn_geolocation_lock);
        startDateButton = view.findViewById(R.id.btn_start_date);
        endDateButton = view.findViewById(R.id.btn_end_date);
        dateButton = view.findViewById(R.id.btn_signup_deadline);
        confirmButton = view.findViewById(R.id.btn_confirm);

        // Hide nav bar
        requireActivity().findViewById(R.id.nav_bar).setVisibility(View.GONE);

        // Get organizer
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Fetch user
        confirmButton.setEnabled(false);
        confirmButton.setAlpha(0.5f);

        // Get device ID
        String deviceId = Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Fetch organizer to enable/disable button
        userViewModel.fetchOrganizer(deviceId);
        userViewModel.getOrganizer().observe(getViewLifecycleOwner(), org -> {
            if (org != null) {
                this.organizer = org;
                this.user = org;
                confirmButton.setEnabled(true);
                confirmButton.setAlpha(1.0f);
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

        startDateButton.setOnClickListener(v -> showDateTimePicker(startDateButton, val -> registerStart = val));
        endDateButton.setOnClickListener(v -> showDateTimePicker(endDateButton, val -> registerEnd = val));
        dateButton.setOnClickListener(v -> showDateTimePicker(dateButton, val -> date = val));

        geolocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!geolocationToggled) {

                    AlertDialog.Builder geolocationDialog = new AlertDialog.Builder(requireContext());
                    geolocationDialog.setTitle("Geolocation Services");
                    geolocationDialog.setMessage("Would you like to enable Geolocation?");
                    geolocationDialog.setCancelable(false);

                    geolocationDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            geolocationToggled = true;
                            Toast.makeText(requireContext(), "Geolocation enabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                    geolocationDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = geolocationDialog.create();
                    dialog.show();
                } else {
                    AlertDialog.Builder geolocationDialog = new AlertDialog.Builder(requireContext());
                    geolocationDialog.setTitle("Geolocation Services");
                    geolocationDialog.setMessage("Would you like to disable Geolocation?");
                    geolocationDialog.setCancelable(false);

                    geolocationDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            geolocationToggled = false;
                            Toast.makeText(requireContext(), "Geolocation disabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                    geolocationDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = geolocationDialog.create();
                    dialog.show();
                }

            }
        });

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

                // Validate int input
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
                            price,
                            date,
                            registerStart,
                            registerEnd,
                            location,
                            Integer.parseInt(eventCap),
                            geolocationToggled
                    );
                    getParentFragmentManager().popBackStack();
                }
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Show nav bar when leaving fragment
        View navBar = requireActivity().findViewById(R.id.nav_bar);
        if (navBar != null) {
            navBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Input dialog for location button
     * @param targetButton
     * Button to be updated
     * @param hint
     * Default text
     * @param onConfirm
     * Updated text on confirm
     */
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

    /**
     * Date and time selection
     * @param targetButton
     * Button to be updated
     * @param onConfirm
     * Updated timestamp on confirm
     */
    private void showDateTimePicker(Button targetButton, java.util.function.Consumer<String> onConfirm) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();

        // Pick date
        new android.app.DatePickerDialog(requireContext(), (dView, year, month, day) -> {

            // Pick time
            new android.app.TimePickerDialog(requireContext(), (tView, hour, minute) -> {

                // Format to match "yyyy-MM-dd HH:mm:ss"
                String selectedDateTime = String.format(java.util.Locale.getDefault(),
                        "%04d-%02d-%02d %02d:%02d:00",
                        year, month + 1, day, hour, minute);

                targetButton.setText(selectedDateTime);
                onConfirm.accept(selectedDateTime);

            }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), true).show();

        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }
}
