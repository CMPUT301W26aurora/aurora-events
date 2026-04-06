package com.example.auroraevents.view;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.auroraevents.LocationToggleListener;
import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.Organizer;
import com.example.auroraevents.model.RegistrationList;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserViewModel;
import com.example.auroraevents.server.EventDb;
import com.example.auroraevents.server.UserDb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
Location conversion to coordinates handled by Geocoder: https://developer.android.com/reference/android/location/Geocoder
 */
public class EventEditFragment extends Fragment {
    private final String TAG = "EventEditFragment";
    private ImageButton backButton;
    private Button commentButton, sampleButton, qrCodeButton, viewEntrantsButton, sendNotificationButton, inviteEntrantsButton, manageCoOrganizersButton, addImageButton;
    private EditText eventNameInput, eventDescInput, eventPriceInput, eventCapInput, eventWaitingCapInput;
    private Button locationButton, geolocationButton, startDateButton, endDateButton, dateButton, privateButton, confirmButton;
    private String eventName, eventDescription, price, eventCap, waitingCap, location, registerStart, registerEnd, date;
    private boolean geolocationRequired, isPrivate;
    private Organizer organizer;
    private User user;
    private List<String> allUsers;
    private Event event;
    private UserViewModel userViewModel;

    private android.net.Uri image;
    private android.widget.ImageView imageView;
    private android.widget.ImageView dialogImageView;
    private android.net.Uri cameraImageUri;
    private Uri selectedImageUri = null;
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
            throw new RuntimeException(context + " must implement LocationToggleListener");
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
        View view = inflater.inflate(R.layout.fragment_event_edit, container, false);

        allUsers = new ArrayList<>();
        UserDb.getInstance().getAllUsers(
                users -> {
                    for (User u : users) {
                        if (u != null && u.getDeviceId() != null && !event.getRegistrationList().getRemovedList().contains(u.getDeviceId())) {
                            allUsers.add(u.getDeviceId());
                        }
                    }
                },
                e -> Log.e(TAG, "Could not get list of users", e)
        );

        // Button and input setup
        backButton = view.findViewById(R.id.backButton);
        commentButton = view.findViewById(R.id.comment_button);

        sampleButton = view.findViewById(R.id.get_participants);
        qrCodeButton = view.findViewById(R.id.show_qr_code);
        viewEntrantsButton = view.findViewById(R.id.view_entrants);
        sendNotificationButton = view.findViewById(R.id.send_notification);
        inviteEntrantsButton = view.findViewById(R.id.invite_entrants);
        manageCoOrganizersButton = view.findViewById(R.id.manage_co_organizers_button);

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

        imageView = view.findViewById(R.id.iv_event_image);
        imageView.setVisibility(View.VISIBLE);

        addImageButton.setVisibility(View.VISIBLE);

        // Get organizer
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        userViewModel.getSelectedItem().observe(getViewLifecycleOwner(), u -> {
            if (u != null) {
                user = u;
                if (u.getRole().equals(User.ROLE_ORGANIZER)) {
                    organizer = new Organizer(
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

        // Set current values
        Bundle args = getArguments();
        if (args == null || args.getString("eventId") == null) {
            Log.e(TAG, "Missing eventId argument");
            getParentFragmentManager().popBackStack();
            return view;
        } else {
            String eventId = args.getString("eventId");
            EventDb.getInstance().addSnapshotListenerForEvent(eventId, event -> {
                this.event = event;
                if (event != null) {
                    eventName = event.getName();
                    eventNameInput.setText(eventName);

                    eventDescription = event.getDescription();
                    eventDescInput.setText(eventDescription);

                    price = event.getPrice();
                    eventPriceInput.setText(price);

                    eventCap = String.valueOf(event.getRegistrationList().getAttendingCapacity());
                    eventCapInput.setText(eventCap);

                    waitingCap = String.valueOf(event.getRegistrationList().getWaitingCapacity());
                    eventWaitingCapInput.setText(waitingCap);

                    location = event.getLocation();

                    geolocationRequired = event.getGeolocationRequired();
                    if (geolocationRequired) {
                        geolocationButton.setText(R.string.geolocation_unlock_text);
                    } else {
                        geolocationButton.setText(R.string.geolocation_lock_text);
                    }

                    registerStart = event.getRegistrationTimeStart();
                    registerEnd = event.getRegistrationTimeEnd();
                    date = event.getDateTime();

                    isPrivate = event.isPrivate();
                    if (isPrivate) {
                        privateButton.setText(R.string.make_public_text);
                        inviteEntrantsButton.setVisibility(View.VISIBLE);
                        inviteEntrantsButton.setOnClickListener(v -> {
                            buildPickEntrantDialog(event);
                        });

                        qrCodeButton.setVisibility(View.GONE);
                    } else {
                        inviteEntrantsButton.setVisibility(View.GONE);
                        privateButton.setText(R.string.make_private_text);

                        qrCodeButton.setVisibility(View.VISIBLE);
                        qrCodeButton.setOnClickListener(v -> {
                            ImageView qrView = new ImageView(getContext());
                            qrView.setImageBitmap(event.generateQrCode());
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Scan this QR code\nto join this event")
                                    .setView(qrView)
                                    .show();
                        });
                    }

                    if (event.getPosterUrl() == null) {
                        imageView.setVisibility(View.GONE);
                    } else {
                        imageView.setVisibility(View.VISIBLE);
                        Glide.with(requireContext())
                                .load(event.getPosterUrl())
                                .into(imageView);
                    }

                    if (user != null && user.getDeviceId().equals(event.getOrganizerDeviceId()) && user.getRole().equals(User.ROLE_ORGANIZER)) {
                        manageCoOrganizersButton.setVisibility(View.VISIBLE);
                        manageCoOrganizersButton.setOnClickListener(v -> {
                            Bundle bundle = new Bundle();
                            bundle.putString("eventId", event.getEventId());
                            bundle.putString("organizerDeviceId", event.getOrganizerDeviceId());

                            ManageCoOrganizersFragment fragment = new ManageCoOrganizersFragment();
                            fragment.setArguments(bundle);

                            getParentFragmentManager()
                                    .beginTransaction()
                                    .add(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        });
                    } else {
                        manageCoOrganizersButton.setVisibility(View.GONE);
                    }
                } else {
                    Log.e(TAG, "No such event available");
                }
            }, e -> {
                Log.e(TAG, "failed to fetch event");
            });
        }

        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        commentButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", event.getEventId());
            bundle.putString("organizerId", event.getOrganizerDeviceId());

            CommentFragment commentFragment = new CommentFragment();
            commentFragment.setArguments(bundle);

            getParentFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            android.R.anim.slide_in_left,
                            android.R.anim.fade_out,
                            android.R.anim.fade_in,
                            android.R.anim.slide_out_right
                    )
                    .replace(R.id.fragment_container, commentFragment)
                    .addToBackStack(null)
                    .commit();
        });

        addImageButton.setOnClickListener(v -> showInputDialogImage());
        imageView.setOnClickListener(v -> showInputDialogImage());

        sampleButton.setOnClickListener(v -> event.getRegistrationList().performLottery(event.getRegistrationList().getEmptySlotAmount(),
                new RegistrationList.OnDbUpdateListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure() {

                    }

                    @Override
                    public void onComplete(RegistrationList.RegistrationResult result) {

                    }
        }));

        viewEntrantsButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", event.getEventId());
            OrganizerUserListFragment userListFragment = new OrganizerUserListFragment();
            userListFragment.setArguments(bundle);
            getParentFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, userListFragment)
                    .addToBackStack(null)
                    .commit();
        });

        sendNotificationButton.setOnClickListener(v -> {
            SendNotificationDialog dialog = SendNotificationDialog.newInstance(
                    event.getEventId(),
                    event.getName(),
                    event.getRegistrationList()
            );
            dialog.show(getParentFragmentManager(), "send_notification");
        });

        locationButton.setOnClickListener(v -> {
            MapPickerFragment mapPicker = new MapPickerFragment();
            mapPicker.setOnLocationPickedListener((address, lat, lng) -> {
                location = address;
                locationButton.setText(address);
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

            int waitingCapacity = -1;
            if (!waitingCap.isEmpty()) {
                try {
                    Integer.parseInt(waitingCap);
                } catch (NumberFormatException e) {
                    eventWaitingCapInput.setError("Please enter a valid number");
                    return;
                }
                waitingCapacity = Integer.parseInt(waitingCap);
            }

            //
            if (location == null || date == null) {
                return;
            }

            // update event
            if (organizer != null) {
                event.setName(eventName);
                event.setDescription(eventDescription);
                event.setPrice(price);
                event.setDateTime(date);
                event.setRegistrationTimeStart(registerStart);
                event.setRegistrationTimeEnd(registerEnd);
                event.setLocation(location);
                event.setGeolocationRequired(geolocationRequired);
                event.getRegistrationList().setAttendingCapacity(Integer.parseInt(eventCap));
                event.getRegistrationList().setWaitingCapacity(waitingCapacity);
                event.setPrivate(isPrivate);

                EventDb.getInstance().updateEvent(event,
                        () -> {
                            if (selectedImageUri != null) {
                                EventDb.getInstance().compressAndUpload(requireContext(), selectedImageUri, event.getEventId());
                            }
                            Log.d(TAG, "Event updated!");
                            Toast.makeText(getContext(), "Event updated successfully!", Toast.LENGTH_SHORT).show();
                        },
                        e -> Log.e(TAG, "Event not updated")
                );
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        View navBar = requireActivity().findViewById(R.id.nav_bar);
        if (navBar != null) {
            navBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Show nav bar when leaving fragment
        View navBar = requireActivity().findViewById(R.id.nav_bar);
        if (navBar != null) {
            navBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Input dialog for image selection
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
        TextView header = dialogView.findViewById(R.id.dialog_title);

        header.setText(R.string.edit_event_poster_title);

        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            activityResultLauncher.launch(intent);
        });

        btnConfirm.setOnClickListener(v -> {
            if (image == null) {
                Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show();
                return;
            }
            // Update and upload poster
            selectedImageUri = image;
            Glide.with(requireContext()).load(selectedImageUri).into(imageView);
            EventDb.getInstance().compressAndUpload(requireContext(), selectedImageUri, event.getEventId());
            dialog.dismiss();
        });

        btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 0);
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Upload image from camera or photo gallery
     */
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
        File photoFile = new File(
                requireActivity().getApplicationContext().getCacheDir(),
                "camera_photo_" + System.currentTimeMillis() + ".jpg"
        );

        /*
        Source - https://stackoverflow.com/a/58908053
        Posted by Pradeep Kumar, modified by community. See post 'Timeline' for change history
        Retrieved 2026-04-05, License - CC BY-SA 4.0
        */
        cameraImageUri = FileProvider.getUriForFile(
                requireActivity().getApplicationContext(),
                requireContext().getPackageName() + ".provider",
                photoFile
        );

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        activityResultLauncher.launch(takePictureIntent);
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
     * Builds and displays a dialog of eligible entrants using their names instead of IDs.
     */
    private void buildPickEntrantDialog(Event event) {
        List<String> eligible = allUsers;

        List<String> displayLabels = new ArrayList<>();
        int total = eligible.size();
        int[] loaded = {0};

        for (int i = 0; i < eligible.size(); i++) {
            String deviceId = eligible.get(i);
            displayLabels.add(deviceId);

            final int index = i;

            UserDb.getInstance().getUser(deviceId,
                    user -> {
                        if (user != null && user.getName() != null && !user.getName().isEmpty()) {
                            displayLabels.set(index, user.getName());
                        }

                        loaded[0]++;
                        if (loaded[0] == total) {
                            showDialogWithNames(eligible, displayLabels);
                        }
                    },
                    e -> {
                        loaded[0]++;
                        if (loaded[0] == total) {
                            showDialogWithNames(eligible, displayLabels);
                        }
                    }
            );
        }
    }

    private void showDialogWithNames(List<String> eligible, List<String> displayLabels) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Select an Entrant")
                .setItems(displayLabels.toArray(new String[0]), (dialog, which) ->
                        confirmAndAddEntrant(eligible.get(which)))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmAndAddEntrant(String deviceId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Add Entrant")
                .setMessage("Invite this entrant to your private event?")
                .setPositiveButton("Confirm", (dialog, which) ->
                    event.getRegistrationList().addToSelectedList(deviceId, new RegistrationList.OnDbUpdateListener() {
                        @Override
                        public void onSuccess() {}
                        @Override
                        public void onFailure() {}
                        @Override
                        public void onComplete(RegistrationList.RegistrationResult result) {}
                    })
                )
                .setNegativeButton("Cancel", null)
                .show();
    }
}
