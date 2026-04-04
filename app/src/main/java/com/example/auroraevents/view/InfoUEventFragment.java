// resources used:
// https://firebase.google.com/docs/firestore/query-data/get-data
// https://firebase.google.com/docs/firestore/query-data/listen
// https://stackoverflow.com/questions/63312913/check-if-a-user-id-exists-in-arraylist
// https://www.c-sharpcorner.com/UploadFile/8836be/set-visibility-on-buttons-in-android/
package com.example.auroraevents.view;

import static android.content.ContentValues.TAG;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.auroraevents.R;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.RegistrationList;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserViewModel;
import com.example.auroraevents.server.EventDb;
import com.example.auroraevents.server.UserDb;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private Button sampleButton, viewEntrantsButton, notificationButton, commentButton;
    private TextView eventName, eventDateTime, eventOrganizer, eventPrice, eventLocation, eventDescription;
    private TextView reportedNum;
    private Button reportButton, deleteButton;
    private LinearLayout bottomBar, selectButtonSet, adminInfo;
    private TextView eventDeadline, waitingListCount, attendeesCount;
    private Button joinButton, leaveButton, acceptButton, declineButton;
    private TextView attendingLabel, cannotAttendLabel;
    private ImageButton infoButton;
    private Boolean inAdmin;

    /**
     * @author Alina Iqbal & Jared Strandlund
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
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
            inAdmin = args.getBoolean("inAdmin");
        }

        // get views to display event details
        backButton         = view.findViewById(R.id.back_button);
        sampleButton       = view.findViewById(R.id.sample_button);
        viewEntrantsButton = view.findViewById(R.id.view_entrants_button);
        commentButton      = view.findViewById(R.id.comment_button);
        notificationButton = view.findViewById(R.id.notification_button);

        poster             = view.findViewById(R.id.poster_image);
        eventName          = view.findViewById(R.id.event_name);
        eventDateTime      = view.findViewById(R.id.event_date_time);
        eventOrganizer     = view.findViewById(R.id.event_organizer);
        eventPrice         = view.findViewById(R.id.event_price);
        eventLocation      = view.findViewById(R.id.event_location);
        eventDescription   = view.findViewById(R.id.event_description);

        reportButton       = view.findViewById(R.id.report_button);
        adminInfo          = view.findViewById(R.id.admin_info);
        reportedNum        = view.findViewById(R.id.reported_num);
        deleteButton       = view.findViewById(R.id.delete_button);

        bottomBar          = view.findViewById(R.id.bottom_bar);

        eventDeadline      = view.findViewById(R.id.event_deadline);
        waitingListCount   = view.findViewById(R.id.waiting_list_count);
        attendeesCount     = view.findViewById(R.id.attendees_count);

        joinButton         = view.findViewById(R.id.join_button);
        leaveButton        = view.findViewById(R.id.leave_button);
        selectButtonSet    = view.findViewById(R.id.select_button_set);
        acceptButton       = view.findViewById(R.id.accept_button);
        declineButton      = view.findViewById(R.id.decline_button);
        attendingLabel     = view.findViewById(R.id.attending_label);
        cannotAttendLabel  = view.findViewById(R.id.cannot_attend_label);

        infoButton         = view.findViewById(R.id.lottery_info_button);

        // back button to return to events list
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());


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
                    if(adminMode != null) {
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
            if(event != null){

                renderCommonUI(event);

                if (activeAdmin) {
                    setupAdminUI(event);
                } else if (userId.equals(event.getOrganizerDeviceId())) {
                    setupOrganizerUI(event);
                } else {
                    setupEntrantUI(event);
                }
            }else{
                Log.e(TAG, "No such event available");
            }
        }, e->{
            Log.e(TAG, "failed to fetch event");
        });
    }
    private void renderCommonUI(Event event){
        if (event.getPoster() == null) {
            poster.setVisibility(View.GONE);
        } else {
            poster.setImageBitmap(event.getPoster());
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
                e -> {}
        );
        eventPrice.setText(event.getPrice());
        eventLocation.setText(event.getLocation());
        eventDescription.setText(event.getDescription());

        // set info button functionality
        infoButton.setOnClickListener( v -> {
                    LotteryInfoFragment infoFragment = new LotteryInfoFragment();
                    infoFragment.show(requireActivity().getSupportFragmentManager(), "Lottery Info");
                }
        );

        commentButton.setOnClickListener(v->{
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

    private void setupAdminUI(Event event){
        bottomBar.setVisibility(View.GONE);
        reportButton.setVisibility(View.GONE);
        adminInfo.setVisibility(View.VISIBLE);

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

    private void setupOrganizerUI(Event event){
        Log.e(TAG, "You shouldn't be here");
        Toast.makeText(getContext(), "You shouldn't be here", Toast.LENGTH_LONG).show();

        bottomBar.setVisibility(View.GONE);
        reportButton.setVisibility(View.GONE);
        adminInfo.setVisibility(View.VISIBLE);

        sampleButton.setVisibility(View.VISIBLE);
        viewEntrantsButton.setVisibility(View.VISIBLE);
        commentButton.setVisibility(View.VISIBLE);
        notificationButton.setVisibility(View.VISIBLE);

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
        notificationButton.setOnClickListener(v -> {
            SendNotificationDialog dialog = SendNotificationDialog.newInstance(
                    event.getEventId(),
                    event.getName(),
                    event.registrationList
            );
            dialog.show(getParentFragmentManager(), "send_notification");
        });
        viewEntrantsButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("eventId", event.getEventId());
            UserListFragment userListFragment = new UserListFragment();
            userListFragment.setArguments(args);
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, userListFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
    private void setupEntrantUI(Event event){
        reportButton.setVisibility(View.VISIBLE);
        adminInfo.setVisibility(View.GONE);
        bottomBar.setVisibility(View.VISIBLE);

        sampleButton.setVisibility(View.GONE);
        viewEntrantsButton.setVisibility(View.GONE);
        commentButton.setVisibility(View.VISIBLE);
        notificationButton.setVisibility(View.GONE);

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
        String waitingCountText = String.valueOf(event.registrationList.getWaitingList().size());
        // don't display the capacity if there is unlimited capacity
        if (event.registrationList.getWaitingCapacity() > -1) {
            waitingCountText += "/" + event.registrationList.getWaitingCapacity();
        }
        if (waitingCountText.equals("1") && event.registrationList.getWaitingCapacity() > -1) {
            waitingCountText += " person is waiting";
        } else {
            waitingCountText += " people are waiting";
        }
        waitingListCount.setText(waitingCountText);

        // set attendees count grammatically
        String attendeesCountText = String.valueOf(event.registrationList.getAttendingList().size());
        // don't display the capacity if there is unlimited capacity
        if (event.registrationList.getAttendingCapacity() > -1) {
            attendeesCountText += "/" + event.registrationList.getAttendingCapacity();
        }
        if (attendeesCountText.equals("1") && event.registrationList.getAttendingCapacity() > -1) {
            attendeesCountText += " person is participating";
        } else {
            attendeesCountText += " people are participating";
        }
        attendeesCount.setText(attendeesCountText);

        RegistrationList list = event.registrationList;

        if (list.getAttendingList().contains(userId)) {
            onAttending(event);
        } else if (list.getSelectedList().contains(userId)) {
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

    private void onAttending(Event event){
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


    private void onSelected(Event event){
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
        acceptButton.setOnClickListener(v  ->{
            v.setEnabled(false);
                EventDb.getInstance().userAcceptSelection(eventId, userId,
                        () ->{
                            Log.d(TAG, "Accepted Invitation");
                        },
                        e -> {
                            Log.e(TAG, "Failed to accept", e);
                            v.setEnabled(true);}
                );
        });
        // move user from selectedList to declinedList on decline
        declineButton.setOnClickListener(v ->{
            v.setEnabled(false);
            EventDb.getInstance().userDeclineSelection(eventId, userId,
                    () ->{
                        Log.d(TAG, "Declined Invitation");
                    },
                    e -> {
                        Log.e(TAG, "Failed to Decline", e);
                        v.setEnabled(true);}
            );
        });
    }

    private void onWaiting(Event event){
        eventDeadline.setVisibility(View.VISIBLE);
        waitingListCount.setVisibility(View.VISIBLE);
        attendeesCount.setVisibility(View.VISIBLE);

        joinButton.setVisibility(View.GONE);
        leaveButton.setVisibility(View.VISIBLE);
        selectButtonSet.setVisibility(View.GONE);
        attendingLabel.setVisibility(View.GONE);
        cannotAttendLabel.setVisibility(View.GONE);

        infoButton.setVisibility(View.VISIBLE);

        // remove user from waitingList when Leave Pool is clicked
        leaveButton.setOnClickListener(v -> {
            EventDb.getInstance().leaveWaitlist(eventId,userId,()->{
                v.setEnabled(false);
                Log.d(TAG, "Successfully Left WaitList");
            }, e->{
                Log.e(TAG, "Failed to leave Waitlist");
                v.setEnabled(false);});
        });
    }

    private void onRemoved(Event event){
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

    private void onFull(Event event){
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

    private void onWaitFull(Event event){
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

    private void onLate(Event event){
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


    private void onJoin(Event event){
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
            EventDb.getInstance().joinWaitlist(eventId, userId,
                    () -> {
                        Log.d(TAG, "Joined waitlist");
                    },
                    e -> {
                        Log.d(TAG, "Failed to join Waitlist");
                        v.setEnabled(true);
                    }
            );
        });
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
    }
}