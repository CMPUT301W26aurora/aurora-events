package com.example.auroraevents.view;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.bumptech.glide.Glide;
import com.example.auroraevents.R;
import com.example.auroraevents.model.Organizer;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.provider.Settings;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.UUID;

/*
Image loading handled by resource Glide:
https://github.com/bumptech/glide
 */
public class EventCreationFragment extends Fragment {
    private ImageButton backButton;
    private TextInputEditText eventNameInput;
    private TextInputEditText eventDescInput;
    private TextInputEditText eventCapInput;
    private Button locationButton;
    private Button startDateButton;
    private Button endDateButton;
    private Button dateButton;
    private Button confirmButton;
    private String eventName;
    private String eventDescription;
    private String eventCap;
    private String location;
    private String date;
    private String registerStart;
    private String registerEnd;
    private Organizer organizer;
    private User user;
    private UserViewModel userViewModel;


    // Image upload
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    private Button addImageButton;
    Uri image;
    ImageView imageView;
    private ImageView dialogImageView;
    private Uri cameraImageUri;
    private String uploadedImageUrl = null;
    private View dialogView;

    private Uri drawableToUri(int drawableResId) {
        return Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + drawableResId);
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_creation, container, false);

        // Button and input setup
        backButton = view.findViewById(R.id.backButton);
        addImageButton = view.findViewById(R.id.btn_add_image);
        eventNameInput = view.findViewById(R.id.et_event_name);
        eventDescInput = view.findViewById(R.id.et_event_desc);
        eventCapInput = view.findViewById(R.id.et_event_capacity);
        locationButton = view.findViewById(R.id.btn_select_location);
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

        // Fetch user
        confirmButton.setEnabled(false);
        confirmButton.setAlpha(0.5f);

        String deviceId = Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        userViewModel.fetchOrganizer(deviceId);
        userViewModel.getOrganizer().observe(getViewLifecycleOwner(), org -> {
            if (org != null) {
                this.organizer = org;
                this.user = org;
                confirmButton.setEnabled(true);
                confirmButton.setAlpha(1.0f);
            }
        });

        addMockImageToGallery();

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

        locationButton.setOnClickListener(v ->
                showInputDialog(locationButton, "Location", val -> location = val));

        startDateButton.setOnClickListener(v -> showDateTimePicker(startDateButton, val -> registerStart = val));
        endDateButton.setOnClickListener(v -> showDateTimePicker(endDateButton, val -> registerEnd = val));
        dateButton.setOnClickListener(v -> showDateTimePicker(dateButton, val -> date = val));

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
                            date,
                            registerStart,
                            registerEnd,
                            location,
                            Integer.parseInt(eventCap),
                            uploadedImageUrl
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
                uploadImage(image, dialog, v);
                dialogImageView.setVisibility(View.GONE);
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    /*
    Show input dialog for location input
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

    /*
    Upload image to firebase storage
    Based of resource: https://medium.com/@everydayprogrammer/
    uploading-files-to-firebase-storage-in-android-studio-using-java-63f43b4c8d72
     */
    private void uploadImage(Uri image, AlertDialog dialog, View view) {
        StorageReference reference = storageRef.child("images/" + UUID.randomUUID().toString());
        reference.putFile(image)
                .addOnSuccessListener(taskSnapshot ->
                        reference.getDownloadUrl().addOnSuccessListener(uri -> {
                            uploadedImageUrl = uri.toString();
                            Toast.makeText(requireContext(), "Image uploaded!", Toast.LENGTH_SHORT).show();

                            // Update main imageview
                            Glide.with(requireContext()).load(uploadedImageUrl).into(imageView);
                            addImageButton.setVisibility(view.GONE);

                            dialog.dismiss();
                        })
                )
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

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
