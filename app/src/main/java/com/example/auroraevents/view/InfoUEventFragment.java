// resources used:
// https://firebase.google.com/docs/firestore/query-data/get-data
// https://firebase.google.com/docs/firestore/query-data/listen
// https://stackoverflow.com/questions/63312913/check-if-a-user-id-exists-in-arraylist
// https://www.c-sharpcorner.com/UploadFile/8836be/set-visibility-on-buttons-in-android/
package com.example.auroraevents.view;

import static android.app.Activity.RESULT_OK;
import static com.example.auroraevents.MainActivity.LOCATION_PERMISSION_REQUEST_CODE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.RadiusUtil;
import com.example.auroraevents.model.RegistrationList;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserViewModel;
import com.example.auroraevents.server.EventDb;
import com.example.auroraevents.server.UserDb;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays event details for the event tapped by the entrant or admin.
 * Gets the event details using the event ID.
 * Checks user role to determine which buttons to display.
 * Uses a snapshot listener to update waiting list count.
 * Implements US 01.01.01 - View event details.
 * Implements US 01.01.03 - Navigate from event list to event details.
 * Implements US 01.06.01 - View waiting list count.
 * Implements US 01.06.02 - Sign up for an event from event details.
 * Implements US 02.02.01 - Admin can view and delete events.
 */
public class InfoUEventFragment extends Fragment {

    private static final String TAG = "InfoUEventFragment";

    private String eventId;
    private String userId;
    private ListenerRegistration eventSnapshotListener;
    private UserViewModel userViewModel;
    private User user;
    private ImageButton backButton;
    private ImageView poster;
    private Button sampleButton, viewEntrantsButton, notificationButton, commentButton, editPosterButton;
    private TextView eventName, eventDateTime, eventOrganizer, eventPrice, eventLocation, eventDescription;
    private TextView reportedNum;
    private Button reportButton, deleteButton;
    private LinearLayout bottomBar, selectButtonSet, adminInfo;
    private TextView eventDeadline, waitingListCount, attendeesCount;
    private Button joinButton, leaveButton, acceptButton, declineButton;
    private TextView attendingLabel, cannotAttendLabel;
    private ImageButton infoButton;

    // Co-organizer management button (visible to the primary organizer only)
    private Button manageCoOrganizersButton;

    private FusedLocationProviderClient fusedLocationClient;
    private Event pendingJoinEvent;

    // Latitude and longitude for radius check
    private static final double EDMONTON_LAT = 53.5461;
    private static final double EDMONTON_LNG = -113.4938;
    private static final float EDMONTON_RADIUS_METERS = 15000f;

    // Edit poster
    private android.net.Uri image;
    private android.widget.ImageView dialogImageView;
    private android.net.Uri cameraImageUri;
    private Uri selectedImageUri = null;
    private View dialogView;

    /**
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @author Alina Iqbal & Jared Strandlund
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.info_u_event_fragment, container, false);

        // get event ID and user ID from bundle
        Bundle args = getArguments();
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        if (args == null || args.getString("eventId") == null || args.getString("userId") == null) {
            Log.e(TAG, "Missing eventId argument");
            getParentFragmentManager().popBackStack();
            return view;
        } else {
            eventId = args.getString("eventId");
            userId = args.getString("userId");
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // get views to display event details
        backButton = view.findViewById(R.id.back_button);
        sampleButton = view.findViewById(R.id.sample_button);
        viewEntrantsButton = view.findViewById(R.id.view_entrants_button);
        commentButton = view.findViewById(R.id.comment_button);
        notificationButton = view.findViewById(R.id.notification_button);
        editPosterButton = view.findViewById(R.id.edit_poster_button);

        poster = view.findViewById(R.id.poster_image);
        eventName = view.findViewById(R.id.event_name);
        eventDateTime = view.findViewById(R.id.event_date_time);
        eventOrganizer = view.findViewById(R.id.event_organizer);
        eventPrice = view.findViewById(R.id.event_price);
        eventLocation = view.findViewById(R.id.event_location);
        eventDescription = view.findViewById(R.id.event_description);

        reportButton = view.findViewById(R.id.report_button);
        adminInfo = view.findViewById(R.id.admin_info);
        reportedNum = view.findViewById(R.id.reported_num);
        deleteButton = view.findViewById(R.id.delete_button);

        bottomBar = view.findViewById(R.id.bottom_bar);

        eventDeadline = view.findViewById(R.id.event_deadline);
        waitingListCount = view.findViewById(R.id.waiting_list_count);
        attendeesCount = view.findViewById(R.id.attendees_count);

        joinButton = view.findViewById(R.id.join_button);
        leaveButton = view.findViewById(R.id.leave_button);
        selectButtonSet = view.findViewById(R.id.select_button_set);
        acceptButton = view.findViewById(R.id.accept_button);
        declineButton = view.findViewById(R.id.decline_button);
        attendingLabel = view.findViewById(R.id.attending_label);
        cannotAttendLabel = view.findViewById(R.id.cannot_attend_label);
      
        infoButton                = view.findViewById(R.id.lottery_info_button);
        manageCoOrganizersButton  = view.findViewById(R.id.manage_co_organizers_button);

        // back button to return to events list
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        requireActivity().findViewById(R.id.nav_bar).setVisibility(View.GONE);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loadEventData();
        } else {
            Log.e(TAG, "Firebase user not signed in");
        }

        return view;
    }

    private void loadEventData() {
        userViewModel.getSelectedItem().observe(getViewLifecycleOwner(), u -> {
            if (u != null) {
                this.user = u;

                userViewModel.getAdminModeActive().observe(getViewLifecycleOwner(), adminMode -> {
                    if (adminMode != null) {
                        boolean activeAdmin = u.getIsAdmin() && adminMode;
                        startEventListener(activeAdmin);
                    }
                });
            }
        });
    }

    private void startEventListener(boolean activeAdmin) {
        if (eventSnapshotListener != null) eventSnapshotListener.remove();

        eventSnapshotListener = EventDb.getInstance().addSnapshotListenerForEvent(eventId, event -> {
            if (event != null) {
                renderCommonUI(event);

                if (activeAdmin) {
                    setupAdminUI(event);
                } else if (user != null && userId.equals(event.getOrganizerDeviceId())
                        && user.getRole().equals(User.ROLE_ORGANIZER)) {
                    // Primary organizer: full organizer UI including co-organizer management
                    setupOrganizerUI(event, true);
                } else if (user != null && event.isCoOrganizer(userId)) {
                    // Co-organizer: organizer UI but cannot manage co-organizers
                    setupOrganizerUI(event, false);
                } else {
                    setupEntrantUI(event);
                }
            } else {
                Log.e(TAG, "No such event available");
            }
        }, e -> {
            Log.e(TAG, "failed to fetch event");
        });
    }

    private void renderCommonUI(Event event) {
        if (event.getPosterUrl() == null) {
            poster.setVisibility(View.GONE);
        } else {
            if (event.getPosterUrl() != null) {
                poster.setVisibility(View.VISIBLE);
                Glide.with(requireContext())
                        .load(event.getPosterUrl())
                        .into(poster);
            }
        }
        eventName.setText(event.getName());
        eventDateTime.setText(event.getDateTime());

        // get organizer name
        String organizerText = getString(R.string.organized_by_text) + event.getOrganizerDeviceId();
        eventOrganizer.setText(organizerText);
        UserDb.getInstance().getUser(event.getOrganizerDeviceId(),
                u -> {
                    if (u != null) {
                        String organizerName = getString(R.string.organized_by_text) + u.getName() + " (" + event.getOrganizerDeviceId() + ")";
                        eventOrganizer.setText(organizerName);
                    }
                },
                e -> {
                }
        );
        eventPrice.setText(event.getPrice());
        eventLocation.setText(event.getLocation());
        eventDescription.setText(event.getDescription());

        // set info button functionality
        infoButton.setOnClickListener(v -> {
            LotteryInfoFragment infoFragment = new LotteryInfoFragment();
            infoFragment.show(requireActivity().getSupportFragmentManager(), "Lottery Info");
        });

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
    }

    private void setupAdminUI(Event event) {
        bottomBar.setVisibility(View.GONE);
        reportButton.setVisibility(View.GONE);
        adminInfo.setVisibility(View.VISIBLE);
        manageCoOrganizersButton.setVisibility(View.GONE);

        sampleButton.setVisibility(View.GONE);
        viewEntrantsButton.setVisibility(View.GONE);
        commentButton.setVisibility(View.VISIBLE);
        notificationButton.setVisibility(View.GONE);

        eventDeadline.setVisibility(View.VISIBLE);
        waitingListCount.setVisibility(View.VISIBLE);
        attendeesCount.setVisibility(View.VISIBLE);

        joinButton.setVisibility(View.GONE);
        leaveButton.setVisibility(View.GONE);
        selectButtonSet.setVisibility(View.GONE);
        attendingLabel.setVisibility(View.GONE);
        cannotAttendLabel.setVisibility(View.GONE);

        infoButton.setVisibility(View.VISIBLE);

        // set the number of people that reported this event grammatically
        String reportedNumText = "Reported by " + event.getNumReports();
        if (event.getNumReports() == 1) {
            reportedNumText += " person";
        } else {
            reportedNumText += " people";
        }
        reportedNum.setText(reportedNumText);

        // set delete button functionality
        deleteButton.setOnClickListener(v -> {
            PermanentWarningFragment fragment = PermanentWarningFragment.newInstance(() ->
                    EventDb.getInstance().deleteEvent(
                            event.getEventId(),
                            () -> {
                                Log.d(TAG, "Event deleted by admin");
                                getParentFragmentManager().popBackStack();
                            },
                            e -> Log.e(TAG, "Error deleting event: " + e)
                    )
            );
            fragment.show(requireActivity().getSupportFragmentManager(), "Confirm Event Delete");
        });
    }

    /**
     * Sets up the organizer UI.
     *
     * @param event            The current event.
     * @param isPrimaryOrganizer True if the current user is the primary organizer (not a co-organizer).
     *                           Only the primary organizer can open the co-organizer management screen.
     */
    private void setupOrganizerUI(Event event, boolean isPrimaryOrganizer) {
        bottomBar.setVisibility(View.GONE);
        reportButton.setVisibility(View.GONE);
        adminInfo.setVisibility(View.VISIBLE);

        editPosterButton.setVisibility(View.VISIBLE);
        sampleButton.setVisibility(View.VISIBLE);
        viewEntrantsButton.setVisibility(View.VISIBLE);
        commentButton.setVisibility(View.VISIBLE);
        notificationButton.setVisibility(View.VISIBLE);

        // Only the primary organizer can manage co-organizers
        if (isPrimaryOrganizer) {
            manageCoOrganizersButton.setVisibility(View.VISIBLE);
            manageCoOrganizersButton.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("eventId", event.getEventId());
                args.putString("organizerDeviceId", event.getOrganizerDeviceId());

                ManageCoOrganizersFragment fragment = new ManageCoOrganizersFragment();
                fragment.setArguments(args);

                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            });
        } else {
            manageCoOrganizersButton.setVisibility(View.GONE);
        }

        // set the number of people that reported this event grammatically
        String reportedNumText = "Reported by " + event.getNumReports();
        if (event.getNumReports() == 1) {
            reportedNumText += " person";
        } else {
            reportedNumText += " people";
        }
        reportedNum.setText(reportedNumText);

        // set delete button functionality
        deleteButton.setOnClickListener(v -> {
            PermanentWarningFragment fragment = PermanentWarningFragment.newInstance(() ->
                    EventDb.getInstance().deleteEvent(
                            event.getEventId(),
                            () -> {
                                Log.d(TAG, "Event deleted by organizer");
                                getParentFragmentManager().popBackStack();
                            },
                            e -> Log.e(TAG, "Error deleting event: " + e)
                    )
            );
            fragment.show(requireActivity().getSupportFragmentManager(), "Confirm Event Delete");
        });
        notificationButton.setOnClickListener(v -> {
            SendNotificationDialog dialog = SendNotificationDialog.newInstance(
                    event.getEventId(),
                    event.getName(),
                    event.getRegistrationList()
            );
            dialog.show(getParentFragmentManager(), "send_notification");
        });
        viewEntrantsButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("eventId", event.getEventId());
            OrganizerUserListFragment userListFragment = new OrganizerUserListFragment();
            userListFragment.setArguments(args);
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, userListFragment)
                    .addToBackStack(null)
                    .commit();
        });
        sampleButton.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sample_event, null);
            CheckBox deadlineToggle = dialogView.findViewById(R.id.deadline_checkbox);
            DatePicker datePicker = dialogView.findViewById(R.id.date_picker);
            deadlineToggle.setOnCheckedChangeListener((buttonView, isChecked) ->
                    datePicker.setVisibility(isChecked ? View.VISIBLE : View.GONE));
            datePicker.setMinDate(System.currentTimeMillis());
            new MaterialAlertDialogBuilder(requireContext())
                    .setView(dialogView)
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        long deadline = -1;
                        if (deadlineToggle.isChecked()) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), 23, 59, 59);
                            deadline = calendar.getTimeInMillis();
                        }

                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        editPosterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialogImage();
            }
        });


            });
    }

    private void setupEntrantUI(Event event) {
        reportButton.setVisibility(View.VISIBLE);
        adminInfo.setVisibility(View.GONE);
        bottomBar.setVisibility(View.VISIBLE);
        manageCoOrganizersButton.setVisibility(View.GONE);

        sampleButton.setVisibility(View.GONE);
        viewEntrantsButton.setVisibility(View.GONE);
        commentButton.setVisibility(View.VISIBLE);
        notificationButton.setVisibility(View.GONE);
        editPosterButton.setVisibility(View.GONE);

        // set report button functionality
        reportButton.setOnClickListener(v -> {
            ReportFragment fragment = ReportFragment.newInstance(() -> {
                event.addReport(userId);
                EventDb.getInstance().updateEvent(event,
                        () -> {
                            Log.d(TAG, "Event updated with new report");
                            Toast.makeText(getContext(), "Event reported.", Toast.LENGTH_SHORT).show();
                        },
                        e -> Toast.makeText(getContext(), "Database connection failed. Please try again.", Toast.LENGTH_SHORT).show());
            });
            fragment.show(requireActivity().getSupportFragmentManager(), "Confirm Event Report");
        });

        String deadlineText = "Sign up before " + event.getRegistrationTimeEnd();
        eventDeadline.setText(deadlineText);

        // set waiting count grammatically
        String waitingCountText = String.valueOf(event.getRegistrationList().getWaitingList().size());
        if (event.getRegistrationList().getWaitingCapacity() > -1) {
            waitingCountText += "/" + event.getRegistrationList().getWaitingCapacity();
        }
        if (waitingCountText.equals("1") && event.getRegistrationList().getWaitingCapacity() > -1) {
            waitingCountText += " person is waiting";
        } else {
            waitingCountText += " people are waiting";
        }
        waitingListCount.setText(waitingCountText);

        // set attendees count grammatically
        String attendeesCountText = String.valueOf(event.getRegistrationList().getAttendingList().size());
        if (event.getRegistrationList().getAttendingCapacity() > -1) {
            attendeesCountText += "/" + event.getRegistrationList().getAttendingCapacity();
        }
        if (attendeesCountText.equals("1") && event.getRegistrationList().getAttendingCapacity() > -1) {
            attendeesCountText += " person is participating";
        } else {
            attendeesCountText += " people are participating";
        }
        attendeesCount.setText(attendeesCountText);

        RegistrationList list = event.getRegistrationList();

        // A co-organizer for this event cannot join the entrant pool
        if (event.isCoOrganizer(userId)) {
            joinButton.setVisibility(View.GONE);
            leaveButton.setVisibility(View.GONE);
            selectButtonSet.setVisibility(View.GONE);
            attendingLabel.setVisibility(View.GONE);
            cannotAttendLabel.setVisibility(View.VISIBLE);
            cannotAttendLabel.setText(R.string.co_organizer_cannot_join);
            return;
        }

        if (list.getAttendingList().contains(userId)) {
            onAttending(event);
        } else if (list.getSelectedUserStrings().contains(userId)) {
            onSelected(event);
        } else if (list.getWaitingList().contains(userId)) {
            onWaiting(event);
        } else if (list.getRemovedList().contains(userId)) {
            onRemoved(event);
        } else if (list.getEmptySlotAmount() == 0) {
            onFull(event);
        } else if (list.getWaitingCapacity() > -1 && list.getWaitingList().size() >= list.getWaitingCapacity()) {
            onWaitFull(event);
        } else if (event.getRegistrationTimeEndAsDateTime().isBefore(LocalDateTime.now())) {
            onLate(event);
        } else {
            onJoin(event);
        }
    }

    private void onAttending(Event event) {
        eventDeadline.setVisibility(View.GONE);
        waitingListCount.setVisibility(View.GONE);
        attendeesCount.setVisibility(View.VISIBLE);

        joinButton.setVisibility(View.GONE);
        leaveButton.setVisibility(View.GONE);
        selectButtonSet.setVisibility(View.GONE);
        attendingLabel.setVisibility(View.VISIBLE);
        cannotAttendLabel.setVisibility(View.GONE);

        infoButton.setVisibility(View.GONE);
    }

    private void onSelected(Event event) {
        eventDeadline.setVisibility(View.VISIBLE);
        waitingListCount.setVisibility(View.VISIBLE);
        attendeesCount.setVisibility(View.VISIBLE);

        joinButton.setVisibility(View.GONE);
        leaveButton.setVisibility(View.GONE);
        selectButtonSet.setVisibility(View.VISIBLE);
        attendingLabel.setVisibility(View.GONE);
        cannotAttendLabel.setVisibility(View.GONE);

        infoButton.setVisibility(View.VISIBLE);

        // move user from selectedList to attendingList on acceptance
        acceptButton.setOnClickListener(v -> {
            v.setEnabled(false);
            EventDb.getInstance().userAcceptSelection(eventId, userId,
                    () -> Log.d(TAG, "Accepted Invitation"),
                    e -> {
                        Log.e(TAG, "Failed to accept", e);
                        v.setEnabled(true);
                    }
            );
        });
        // move user from selectedList to declinedList on decline
        declineButton.setOnClickListener(v -> {
            v.setEnabled(false);
            EventDb.getInstance().userDeclineSelection(eventId, userId,
                    () -> Log.d(TAG, "Declined Invitation"),
                    e -> {
                        Log.e(TAG, "Failed to Decline", e);
                        v.setEnabled(true);
                    }
            );
        });
    }

    private void onWaiting(Event event) {
        eventDeadline.setVisibility(View.VISIBLE);
        waitingListCount.setVisibility(View.VISIBLE);
        attendeesCount.setVisibility(View.VISIBLE);

        joinButton.setVisibility(View.GONE);
        leaveButton.setVisibility(View.VISIBLE);
        selectButtonSet.setVisibility(View.GONE);
        attendingLabel.setVisibility(View.GONE);
        cannotAttendLabel.setVisibility(View.GONE);

        infoButton.setVisibility(View.VISIBLE);

        leaveButton.setOnClickListener(v -> {
            EventDb.getInstance().leaveWaitlist(eventId, userId,
                    () -> {
                        v.setEnabled(false);
                        Log.d(TAG, "Successfully Left WaitList");
                    },
                    e -> {
                        Log.e(TAG, "Failed to leave Waitlist");
                        v.setEnabled(false);
                    });
        });
    }

    private void onRemoved(Event event) {
        eventDeadline.setVisibility(View.GONE);
        waitingListCount.setVisibility(View.GONE);
        attendeesCount.setVisibility(View.GONE);

        joinButton.setVisibility(View.GONE);
        leaveButton.setVisibility(View.GONE);
        selectButtonSet.setVisibility(View.GONE);
        attendingLabel.setVisibility(View.GONE);
        cannotAttendLabel.setVisibility(View.VISIBLE);

        infoButton.setVisibility(View.GONE);
    }

    private void onFull(Event event) {
        eventDeadline.setVisibility(View.GONE);
        waitingListCount.setVisibility(View.GONE);
        attendeesCount.setVisibility(View.VISIBLE);

        joinButton.setVisibility(View.GONE);
        leaveButton.setVisibility(View.GONE);
        selectButtonSet.setVisibility(View.GONE);
        attendingLabel.setVisibility(View.GONE);
        cannotAttendLabel.setVisibility(View.VISIBLE);

        infoButton.setVisibility(View.VISIBLE);
    }

    private void onWaitFull(Event event) {
        eventDeadline.setVisibility(View.GONE);
        waitingListCount.setVisibility(View.VISIBLE);
        attendeesCount.setVisibility(View.VISIBLE);

        joinButton.setVisibility(View.GONE);
        leaveButton.setVisibility(View.GONE);
        selectButtonSet.setVisibility(View.GONE);
        attendingLabel.setVisibility(View.GONE);
        cannotAttendLabel.setVisibility(View.VISIBLE);

        infoButton.setVisibility(View.VISIBLE);
    }

    private void onLate(Event event) {
        eventDeadline.setVisibility(View.VISIBLE);
        waitingListCount.setVisibility(View.GONE);
        attendeesCount.setVisibility(View.GONE);

        joinButton.setVisibility(View.GONE);
        leaveButton.setVisibility(View.GONE);
        selectButtonSet.setVisibility(View.GONE);
        attendingLabel.setVisibility(View.GONE);
        cannotAttendLabel.setVisibility(View.VISIBLE);

        infoButton.setVisibility(View.VISIBLE);
    }

    private void onJoin(Event event) {
        eventDeadline.setVisibility(View.VISIBLE);
        waitingListCount.setVisibility(View.VISIBLE);
        attendeesCount.setVisibility(View.VISIBLE);

        joinButton.setVisibility(View.VISIBLE);
        leaveButton.setVisibility(View.GONE);
        selectButtonSet.setVisibility(View.GONE);
        attendingLabel.setVisibility(View.GONE);
        cannotAttendLabel.setVisibility(View.GONE);

        infoButton.setVisibility(View.VISIBLE);

        joinButton.setOnClickListener(v -> {
            v.setEnabled(false);
            attemptJoinWaitingList(event, v);
        });
    }

    private void attemptJoinWaitingList(Event event, View joinBtn) {
        if (event.getGeolocationRequired()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Location Required")
                    .setMessage("This event requires your location to join the waiting list. Do you want to share your location?")
                    .setCancelable(false)
                    .setPositiveButton("Allow", (dialog, id) -> {
                        if (ContextCompat.checkSelfPermission(requireContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                            fetchLocationAndJoin(event);
                        } else {
                            // Store event, prompt for permission
                            pendingJoinEvent = event;
                            ActivityCompat.requestPermissions(
                                    requireActivity(),
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    LOCATION_PERMISSION_REQUEST_CODE
                            );
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, id) -> {
                        dialog.dismiss();
                        if (joinBtn != null) joinBtn.setEnabled(true);
                    })
                    .show();
        } else {
            // Join directly, geolocation not required
            joinWaitingList(event, null, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndJoin(pendingJoinEvent);
            } else {
                Toast.makeText(requireContext(),
                        "Location permission is required to join this event.",
                        Toast.LENGTH_LONG).show();
                // Do NOT join — entrant blocked until permission is granted
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchLocationAndJoin(Event event) {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        // Only check radius if the event has coordinates set
                        if (event.getLatitude() != 0 && event.getLongitude() != 0) {
                            boolean withinRange = RadiusUtil.isWithinRadius(
                                    EDMONTON_LAT, EDMONTON_LNG,
                                    location.getLatitude(), location.getLongitude(),
                                    EDMONTON_RADIUS_METERS
                            );
                            if (!withinRange) {
                                Toast.makeText(requireContext(),
                                        "You must be in Edmonton to join.",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        joinWaitingList(event, location.getLatitude(), location.getLongitude());
                    } else {
                        Toast.makeText(requireContext(),
                                "Unable to get location. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void joinWaitingList(Event event, Double latitude, Double longitude) {
        EventDb.getInstance().joinWaitlist(eventId, userId,
                () -> Log.d(TAG, "Joined waitlist"),
                e -> Log.e(TAG, "Failed to join Waitlist")
        );

        // Store coordinates separately if geolocation is enabled
        if (latitude != null && longitude != null) {
            Map<String, Object> locationEntry = new HashMap<>();
            locationEntry.put("latitude", latitude);
            locationEntry.put("longitude", longitude);
            FirebaseFirestore.getInstance()
                    .collection("Events")
                    .document(event.getEventId())
                    .collection("entrantLocations")
                    .document(userId)
                    .set(locationEntry)
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to save location", e));
        }
    }

    /**
     * Remove snapshot listener
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (eventSnapshotListener != null) {
            eventSnapshotListener.remove();
            Log.d(TAG, "Event snapshot listener detached");
        }
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

        header.setText("Edit Event Poster");

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
            }
        });

        btnConfirm.setOnClickListener(v -> {
            if (image == null) {
                Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show();
                return;
            }
            // Update and upload poster
            selectedImageUri = image;
            Glide.with(requireContext()).load(selectedImageUri).into(poster);
            EventDb.getInstance().compressAndUpload(requireContext(), selectedImageUri, eventId);
            dialog.dismiss();
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
}