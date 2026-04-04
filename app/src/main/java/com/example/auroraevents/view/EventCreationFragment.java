package com.example.auroraevents.view;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.example.auroraevents.LocationToggleListener;
import com.example.auroraevents.R;
import com.example.auroraevents.model.Organizer;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.widget.Toast;

/*
Location conversion to coordinates handled by Geocoder: https://developer.android.com/reference/android/location/Geocoder
Maps handled by Google Maps SDK:
 */
public class EventCreationFragment extends Fragment {
    //TODO 4: copy into edit event fragment
    private final String TAG = "EventCreationFragment";
    private ImageButton backButton;
    private Button addImageButton;
    private TextInputEditText eventNameInput;
    private TextInputEditText eventDescInput;
    private TextInputEditText eventPriceInput;
    private TextInputEditText eventCapInput;
    private TextInputEditText eventWaitingCapInput;
    private Button locationButton;
    private Button geolocationButton;
    private Button startDateButton;
    private Button endDateButton;
    private Button dateButton;
    private Button privateButton;
    private Button confirmButton;
    private String eventName;
    private String eventDescription;
    private String price;
    private String eventCap;
    private String waitingCap;
    private String location;
    private boolean geolocationRequired;
    private String registerStart;
    private String registerEnd;
    private String date;
    private boolean isPrivate;
    private Organizer organizer;
    private User user;
    private UserViewModel userViewModel;
    private double eventLat;
    private double eventLong;

    private android.net.Uri image;
    private android.widget.ImageView imageView;
    private android.widget.ImageView dialogImageView;
    private android.net.Uri cameraImageUri;
    private Bitmap uploadedImageUrl = null;
    private View dialogView;
    private com.google.firebase.storage.FirebaseStorage storage;
    private com.google.firebase.storage.StorageReference storageRef;

    private LocationToggleListener locationToggleListener;

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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_creation, container, false);

        // Button and input setup
        backButton = view.findViewById(R.id.backButton);
        addImageButton = view.findViewById(R.id.btn_add_image);
        eventNameInput = view.findViewById(R.id.et_event_name);
        eventDescInput = view.findViewById(R.id.et_event_desc);
        eventPriceInput = view.findViewById(R.id.et_event_price);
        eventCapInput = view.findViewById(R.id.et_event_capacity);
        eventWaitingCapInput = view.findViewById(R.id.et_event_waiting_capacity);
        locationButton = view.findViewById(R.id.btn_select_location);
        geolocationButton = view.findViewById(R.id.btn_geolocation_lock);
        startDateButton = view.findViewById(R.id.btn_start_date);
        endDateButton = view.findViewById(R.id.btn_end_date);
        dateButton = view.findViewById(R.id.btn_signup_deadline);
        privateButton = view.findViewById(R.id.btn_is_private);
        confirmButton = view.findViewById(R.id.btn_confirm);

        // Hide nav bar
        requireActivity().findViewById(R.id.nav_bar).setVisibility(View.GONE);

        // Get organizer
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Set organizer
        userViewModel.getSelectedItem().observe(getViewLifecycleOwner(), u -> {
            if (u != null) {
                this.user = u;
                if (u.getRole().equals(User.ROLE_ORGANIZER)) {
                    this.organizer = new Organizer(
                            u.getDeviceId(),
                            u.getName(),
                            u.getEmail(),
                            u.getPhoneNumber(),
                            u.getRole(),
                            u.getIsAdmin()
                    );
                }
            }
        });

        // Get device ID
        String deviceId = Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Fetch firebase cloud storage
        storage = com.google.firebase.storage.FirebaseStorage.getInstance("gs://aurora-events.firebasestorage.app");
        storageRef = storage.getReference();
        imageView = view.findViewById(R.id.iv_event_image);

        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        locationButton.setOnClickListener(v -> {
            MapPickerFragment mapPicker = new MapPickerFragment();
            mapPicker.setOnLocationPickedListener((address, lat, lng) -> {
                location = address;
                eventLat = lat;     // Store latitude and longitude
                eventLong = lng;
                locationButton.setText(address);
                requireActivity().findViewById(R.id.nav_bar).setVisibility(View.GONE);
            });
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, mapPicker)
                    .addToBackStack(null)
                    .commit();
        });

        startDateButton.setOnClickListener(v -> showDateTimePicker(startDateButton, val -> registerStart = val));
        endDateButton.setOnClickListener(v -> showDateTimePicker(endDateButton, val -> registerEnd = val));
        dateButton.setOnClickListener(v -> showDateTimePicker(dateButton, val -> date = val));

        geolocationButton.setOnClickListener(v -> {
            if (location == null) {
                Toast.makeText(getContext(), "Please input a location first", Toast.LENGTH_SHORT).show();
            } else {
                if (!geolocationRequired) {

                    AlertDialog.Builder geolocationDialog = new AlertDialog.Builder(requireContext());
                    geolocationDialog.setTitle("Geolocation Services");
                    geolocationDialog.setMessage("Would you like to enable Geolocation?");
                    geolocationDialog.setCancelable(false);

                    geolocationDialog.setPositiveButton("Confirm", (dialog, id) -> {
                        geolocationRequired = true;
                        geolocationButton.setText(R.string.geolocation_unlock_text);
                        Toast.makeText(requireContext(), "Geolocation enabled", Toast.LENGTH_SHORT).show();
                    });
                    geolocationDialog.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

                    AlertDialog dialog = geolocationDialog.create();
                    dialog.show();
                } else {
                    AlertDialog.Builder geolocationDialog = new AlertDialog.Builder(requireContext());
                    geolocationDialog.setTitle("Geolocation Services");
                    geolocationDialog.setMessage("Would you like to disable Geolocation?");
                    geolocationDialog.setCancelable(false);

                    geolocationDialog.setPositiveButton("Confirm", (dialog, id) -> {
                        geolocationRequired = false;
                        geolocationButton.setText(R.string.geolocation_lock_text);
                        Toast.makeText(requireContext(), "Geolocation disabled", Toast.LENGTH_SHORT).show();
                    });
                    geolocationDialog.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

                    AlertDialog dialog = geolocationDialog.create();
                    dialog.show();
                }
            }
        });

        privateButton.setOnClickListener(v -> {
            if (!isPrivate) {
                AlertDialog.Builder privateDialog = new AlertDialog.Builder(requireContext());
                privateDialog.setMessage("Would you like to make this event private?");
                privateDialog.setCancelable(false);

                privateDialog.setPositiveButton("Confirm", (dialog, id) -> {
                    isPrivate = true;
                    privateButton.setText(R.string.make_public_text);
                    Toast.makeText(requireContext(), "Event will be private", Toast.LENGTH_SHORT).show();
                });
                privateDialog.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

                AlertDialog dialog = privateDialog.create();
                dialog.show();
            } else {
                AlertDialog.Builder privateDialog = new AlertDialog.Builder(requireContext());
                privateDialog.setMessage("Would you like to make this event public?");
                privateDialog.setCancelable(false);

                privateDialog.setPositiveButton("Confirm", (dialog, id) -> {
                    isPrivate = false;
                    privateButton.setText(R.string.make_private_text);
                    Toast.makeText(requireContext(), "Event will be public", Toast.LENGTH_SHORT).show();
                });
                privateDialog.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

                AlertDialog dialog = privateDialog.create();
                dialog.show();
            }
        });

        confirmButton.setOnClickListener(v -> {
            // Get values from text input
            eventName = eventNameInput.getText().toString().trim();
            eventDescription = eventDescInput.getText().toString().trim();
            price = eventPriceInput.getText().toString().trim();
            eventCap = eventCapInput.getText().toString().trim();
            waitingCap = eventWaitingCapInput.getText().toString().trim();

            // Validate required inputs
            if (eventName.isEmpty()) {
                eventNameInput.setError("Event name is required");
                return;
            }

            if (eventDescription.isEmpty()) {
                eventDescInput.setError("Event name is required");
                return;
            }

            if (price.isEmpty()) {
                eventPriceInput.setError("Event name is required");
                return;
            }

            if (eventCap.isEmpty()) {
                eventCapInput.setError("Capacity is required");
                return;
            }

            // Validate int input
            try {
                Integer.parseInt(eventCap);
            } catch (NumberFormatException e) {
                eventCapInput.setError("Please enter a valid number");
                return;
            }

            try {
                Integer.parseInt(waitingCap);
            } catch (NumberFormatException e) {
                eventWaitingCapInput.setError("Please enter a valid number");
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
                        geolocationRequired,
                        Integer.parseInt(eventCap),
                        Integer.parseInt(eventCap),
                        isPrivate,
                        uploadedImageUrl
                );
                getParentFragmentManager().popBackStack();
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
