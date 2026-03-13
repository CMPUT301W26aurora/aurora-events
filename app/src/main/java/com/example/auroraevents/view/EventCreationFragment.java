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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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
    private Button signUpDeadlineButton;
    private Button confirmButton;
    private Fragment eventListFragment;
    private String eventName;
    private String eventDescription;
    private String eventCap;
    private String location;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_creation, container, false);

        backButton = view.findViewById(R.id.backButton);
        addImageButton = view.findViewById(R.id.btn_add_image);
        eventNameInput = view.findViewById(R.id.et_event_name);
        eventDescInput = view.findViewById(R.id.et_event_desc);
        eventCapInput = view.findViewById(R.id.et_event_capacity);
        locationButton = view.findViewById(R.id.btn_select_location);
        geolocationLockButton = view.findViewById(R.id.btn_geolocation_lock);
        startDateButton = view.findViewById(R.id.btn_start_date);
        endDateButton = view.findViewById(R.id.btn_end_date);
        signUpDeadlineButton = view.findViewById(R.id.btn_signup_deadline);
        confirmButton = view.findViewById(R.id.btn_confirm);

        requireActivity().findViewById(R.id.nav_bar).setVisibility(View.GONE);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_input, null);
                builder.setView(dialogView);

                AlertDialog dialog = builder.create();

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                    params.gravity = Gravity.CENTER;
                    params.width = WindowManager.LayoutParams.MATCH_PARENT;
                    params.horizontalMargin = 0.1f;
                    dialog.getWindow().setAttributes(params);
                }

                // Get references to dialog views
                TextInputEditText input = dialogView.findViewById(R.id.dialog_input);
                TextInputLayout inputLayout = dialogView.findViewById(R.id.dialog_input_layout);
                Button btnCancel = dialogView.findViewById(R.id.dialog_btn_cancel);
                Button btnConfirm = dialogView.findViewById(R.id.dialog_btn_confirm);

                btnCancel.setOnClickListener(cancelView -> dialog.dismiss());

                btnConfirm.setOnClickListener(confirmView -> {
                    String enteredText = input.getText() != null ? input.getText().toString().trim() : "";

                    if (enteredText.isEmpty()) {
                        inputLayout.setError("This field is required");
                    } else {
                        inputLayout.setError(null);
                        location = enteredText;

                        locationButton.setText(enteredText);

                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        startDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_input, null);
                builder.setView(dialogView);

                AlertDialog dialog = builder.create();

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                    params.gravity = Gravity.CENTER;
                    params.width = WindowManager.LayoutParams.MATCH_PARENT;
                    params.horizontalMargin = 0.1f;
                    dialog.getWindow().setAttributes(params);
                }

                // Get references to dialog views
                TextInputEditText input = dialogView.findViewById(R.id.dialog_input);
                TextInputLayout inputLayout = dialogView.findViewById(R.id.dialog_input_layout);
                Button btnCancel = dialogView.findViewById(R.id.dialog_btn_cancel);
                Button btnConfirm = dialogView.findViewById(R.id.dialog_btn_confirm);

                btnCancel.setOnClickListener(cancelView -> dialog.dismiss());

                btnConfirm.setOnClickListener(confirmView -> {
                    String enteredText = input.getText() != null ? input.getText().toString().trim() : "";

                    if (enteredText.isEmpty()) {
                        inputLayout.setError("This field is required");
                    } else {
                        inputLayout.setError(null);
                        location = enteredText;

                        startDateButton.setText(enteredText);

                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        endDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_input, null);
                builder.setView(dialogView);

                AlertDialog dialog = builder.create();

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                    params.gravity = Gravity.CENTER;
                    params.width = WindowManager.LayoutParams.MATCH_PARENT;
                    params.horizontalMargin = 0.1f;
                    dialog.getWindow().setAttributes(params);
                }

                // Get references to dialog views
                TextInputEditText input = dialogView.findViewById(R.id.dialog_input);
                TextInputLayout inputLayout = dialogView.findViewById(R.id.dialog_input_layout);
                Button btnCancel = dialogView.findViewById(R.id.dialog_btn_cancel);
                Button btnConfirm = dialogView.findViewById(R.id.dialog_btn_confirm);

                btnCancel.setOnClickListener(cancelView -> dialog.dismiss());

                btnConfirm.setOnClickListener(confirmView -> {
                    String enteredText = input.getText() != null ? input.getText().toString().trim() : "";

                    if (enteredText.isEmpty()) {
                        inputLayout.setError("This field is required");
                    } else {
                        inputLayout.setError(null);
                        location = enteredText;

                        endDateButton.setText(enteredText);

                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        signUpDeadlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_input, null);
                builder.setView(dialogView);

                AlertDialog dialog = builder.create();

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                    params.gravity = Gravity.CENTER;
                    params.width = WindowManager.LayoutParams.MATCH_PARENT;
                    params.horizontalMargin = 0.1f;
                    dialog.getWindow().setAttributes(params);
                }

                // Get references to dialog views
                TextInputEditText input = dialogView.findViewById(R.id.dialog_input);
                TextInputLayout inputLayout = dialogView.findViewById(R.id.dialog_input_layout);
                Button btnCancel = dialogView.findViewById(R.id.dialog_btn_cancel);
                Button btnConfirm = dialogView.findViewById(R.id.dialog_btn_confirm);

                btnCancel.setOnClickListener(cancelView -> dialog.dismiss());

                btnConfirm.setOnClickListener(confirmView -> {
                    String enteredText = input.getText() != null ? input.getText().toString().trim() : "";

                    if (enteredText.isEmpty()) {
                        inputLayout.setError("This field is required");
                    } else {
                        inputLayout.setError(null);
                        location = enteredText;

                        signUpDeadlineButton.setText(enteredText);

                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventName = eventNameInput.getText().toString();
                eventDescription = eventDescInput.getText().toString();
                eventCap = eventCapInput.getText().toString();
            }
        });

        return view;
    }
}
