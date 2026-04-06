package com.example.auroraevents.view;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;


import com.bumptech.glide.Glide;
import com.example.auroraevents.LocationToggleListener;
import com.example.auroraevents.R;
import com.example.auroraevents.model.Organizer;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserViewModel;
import com.example.auroraevents.server.EventDb;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/*
Image loading handled by resource Glide:
https://github.com/bumptech/glide
 */


/*
Location conversion to coordinates handled by Geocoder: https://developer.android.com/reference/android/location/Geocoder
Maps handled by Google Maps SDK:
 */
public class EventCreationFragment extends Fragment {
    private ImageButton backButton;
    private final String TAG = "EventCreationFragment";
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

    private android.net.Uri image;
    private android.widget.ImageView imageView;
    private android.widget.ImageView dialogImageView;
    private android.net.Uri cameraImageUri;
    private Uri selectedImageUri = null;
    private View dialogView;

    private LocationToggleListener locationToggleListener;
    public boolean geolocationToggled;
    private double eventLat = 0;
    private double eventLng = 0;

    private Bitmap poster = null;
    private int waitingCapacity = 0;
    private int attendingCapacity = 0;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof LocationToggleListener) {
            locationToggleListener = (LocationToggleListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement LocationToggleListener");
        }
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null && result.getData().getData() != null) {
                        image = result.getData().getData();
                    } else if (cameraImageUri != null) {
                        image = cameraImageUri;
                    }

                    if (image != null) {
                        // Update image upload dialog
                        if (dialogImageView != null) {
                            dialogImageView.setVisibility(View.VISIBLE);
                            dialogView.findViewById(R.id.image_placeholder).setVisibility(View.GONE);
                            dialogImageView.post(() ->
                                    Glide.with(requireContext()).load(image).into(dialogImageView)
                            );
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show();
                }
            }
    );

    /**
     * Take photo via intent
     */
    private void dispatchTakePictureIntent() {
        // Create temp image URI
        java.io.File photoFile = new java.io.File(
                requireContext().getCacheDir(),
                "camera_photo_" + System.currentTimeMillis() + ".jpg"
        );
        cameraImageUri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                photoFile
        );

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        activityResultLauncher.launch(takePictureIntent);
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

        imageView = view.findViewById(R.id.iv_event_image);
        imageView.setVisibility(View.VISIBLE);

        addImageButton.setVisibility(view.VISIBLE);

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

        imageView = view.findViewById(R.id.iv_event_image);

        // Testing purposes
        //addMockImageToGallery();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialogImage();
            }
        });

        locationButton.setOnClickListener(v -> {
            MapPickerFragment mapPicker = new MapPickerFragment();
            mapPicker.setOnLocationPickedListener((address, lat, lng) -> {
                location = address;
                eventLat = lat;     // Store latitude and longitude
                eventLng = lng;
            });
            getParentFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, mapPicker)
                    .addToBackStack(null)
                    .commit();
        });

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
                waitingCapacity = capacityValue;
                attendingCapacity = capacityValue;

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
                            eventLat,
                            eventLng,
                            geolocationToggled,
                            waitingCapacity,
                            attendingCapacity,
                            poster,
                            eventId -> {
                                Log.d(TAG, "Callback reached, attempting popBackStack");
                                if (selectedImageUri != null) {
                                    EventDb.getInstance().compressAndUpload(requireContext(), selectedImageUri, eventId);
                                }
                                if (isAdded() && getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Log.d(TAG, "Running popBackStack on UI thread");
                                        getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                    });
                                } else {
                                    Log.d(TAG, "Fragment not added or activity null — skip pop");
                                }
                            }
                    );
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
     * Display input dialog for image selection
     */
    private void showInputDialogImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.image_upload, null);
        builder.setView((dialogView));

        dialogImageView = dialogView.findViewById(R.id.image_preview);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.gravity = Gravity.CENTER;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setAttributes(params);
        }

        Button btnCamera = dialogView.findViewById(R.id.btn_take_photo);
        Button btnGallery = dialogView.findViewById(R.id.btn_choose_gallery);
        Button btnCancel = dialogView.findViewById(R.id.btn_image_cancel);
        Button btnConfirm = dialogView.findViewById(R.id.btn_image_confirm);
        FrameLayout dialogImageFrame = dialogView.findViewById(R.id.image_preview_container);

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (image == null) {
                    Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show();
                    return;
                }
                selectedImageUri = image;
                Glide.with(requireContext()).load(selectedImageUri).into(imageView);
                addImageButton.setVisibility(View.GONE);
                dialog.dismiss();
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

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

    /**
     * For testing purposes, adds an image to the gallery
     */
    private void addMockImageToGallery() {
        // Copy drawable to external storage
        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeResource(
                getResources(), R.drawable.test_image);

        String fileName = "test_image_" + System.currentTimeMillis() + ".jpg";
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/");

        android.net.Uri uri = requireContext().getContentResolver()
                .insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            java.io.OutputStream outputStream = requireContext().getContentResolver()
                    .openOutputStream(uri);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            Toast.makeText(requireContext(), "Mock image added to gallery", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            android.util.Log.e("MockImage", "Failed to add mock image: " + e.getMessage());
        }
    }
}
