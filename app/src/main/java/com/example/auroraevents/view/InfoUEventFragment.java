// resources used:
// https://firebase.google.com/docs/firestore/query-data/get-data
// https://firebase.google.com/docs/firestore/query-data/listen
// https://stackoverflow.com/questions/63312913/check-if-a-user-id-exists-in-arraylist
// https://www.c-sharpcorner.com/UploadFile/8836be/set-visibility-on-buttons-in-android/
package com.example.auroraevents.view;

import android.os.Bundle;
import android.provider.Settings;
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

import com.example.auroraevents.R;
import com.example.auroraevents.model.User;
import com.example.auroraevents.server.EventDb;
import com.example.auroraevents.server.UserDb;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.time.LocalDateTime;

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

    private ImageButton backButton;
    private ImageView poster;
    private TextView eventName, eventDateTime, eventOrganizer, eventPrice, eventLocation, eventDescription;
    private Button reportButton, deleteButton;
    private LinearLayout bottomBar, selectButtonSet;
    private TextView eventDeadline, waitingListCount, attendeesCount;
    private Button joinButton, leaveButton, acceptButton, declineButton;
    private TextView attendingLabel, cannotAttendLabel;
    private ImageButton infoButton;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { //TODO 1: update to work with new UI
        View view = inflater.inflate(R.layout.info_u_event_fragment, container, false);

        // get event ID from bundle
        Bundle args = getArguments();
        if (args == null || args.getString("eventId") == null) {
            Log.e(TAG, "Missing eventId argument");
            getParentFragmentManager().popBackStack();
            return view;
        }

        eventId = args.getString("eventId");

        // get device Id to identify user
        userId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // get views to display event details
        backButton        = view.findViewById(R.id.back_button);
        poster            = view.findViewById(R.id.poster_image);
        eventName         = view.findViewById(R.id.event_name);
        eventDateTime     = view.findViewById(R.id.event_date_time);
        eventOrganizer    = view.findViewById(R.id.event_organizer);
        eventPrice        = view.findViewById(R.id.event_price);
        eventLocation     = view.findViewById(R.id.event_location);
        eventDescription  = view.findViewById(R.id.event_description);

        reportButton      = view.findViewById(R.id.report_button);         // TODO
        deleteButton      = view.findViewById(R.id.delete_button);

        bottomBar         = view.findViewById(R.id.bottom_bar);

        eventDeadline     = view.findViewById(R.id.event_deadline);
        waitingListCount  = view.findViewById(R.id.waiting_list_count);
        attendeesCount    = view.findViewById(R.id.attendees_count);

        joinButton        = view.findViewById(R.id.join_button);
        leaveButton       = view.findViewById(R.id.leave_button);
        selectButtonSet   = view.findViewById(R.id.select_button_set);
        acceptButton      = view.findViewById(R.id.accept_button);
        declineButton     = view.findViewById(R.id.decline_button);
        attendingLabel    = view.findViewById(R.id.attending_label);
        cannotAttendLabel = view.findViewById(R.id.cannot_attend_label);

        infoButton        = view.findViewById(R.id.selection_info_button);

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
        UserDb.getInstance().getUser(
                userId,
                user -> {
                    // check if user role is admin
                    final boolean userIsAdmin = user != null && User.ROLE_ADMIN.equals(user.getRole());
                    final boolean userIsOrganizer = user != null && User.ROLE_ORGANIZER.equals(user.getRole());

                    // attach snapshot listener to get event details
                    eventSnapshotListener = EventDb.getInstance().addSnapshotListenerForEvent(
                                    eventId,
                                    event -> { if (event != null) {
                                            // display event details for all user roles
                                            if (event.getPoster() == null)
                                                poster.setVisibility(View.GONE);
                                            else
                                                poster.setImageBitmap(event.getPoster());
                                            eventName.setText(event.getName());
                                            eventDateTime.setText(event.getDateTime());
                                            String organizerText = "organized by " + event.getOrganizerDeviceId();
                                            eventOrganizer.setText(organizerText);
                                            eventPrice.setText(event.getPrice());
                                            eventLocation.setText(event.getLocation());
                                            eventDescription.setText(event.getDescription());
                                            String deadlineText = "Sign up before " + event.getRegistrationTimeEnd();
                                            eventDeadline.setText(deadlineText);
                                            String waitingCountText;
                                            if (event.registrationList.getWaitingList().size() == 1) {
                                                waitingCountText = "1 person is waiting";
                                            } else {
                                                waitingCountText = event.registrationList.getWaitingList().size() + " people are waiting";
                                            }
                                            waitingListCount.setText(waitingCountText);
                                            String attendeesCountText = String.valueOf(event.registrationList.getAttendingList().size());
                                            if (event.getCapacity() != 0) {
                                                attendeesCountText += "/" + event.getCapacity();
                                            }
                                            attendeesCountText += " people are participating";
                                            attendeesCount.setText(attendeesCountText);

                                            // set info button functionality
                                            infoButton.setOnClickListener( v -> {
                                                    SelectionInfoFragment infoFragment = new SelectionInfoFragment();
                                                    infoFragment.show(requireActivity().getSupportFragmentManager(), "Lottery Info");
                                                }
                                            );

                                            if (userIsAdmin) {
                                                // display event details for admin view
                                                bottomBar.setVisibility(View.GONE);
                                                reportButton.setVisibility(View.GONE);
                                                deleteButton.setVisibility(View.VISIBLE);

                                                eventDeadline.setVisibility(View.VISIBLE);
                                                waitingListCount.setVisibility(View.VISIBLE);
                                                attendeesCount.setVisibility(View.VISIBLE);

                                                joinButton.setVisibility(View.GONE);
                                                leaveButton.setVisibility(View.GONE);
                                                selectButtonSet.setVisibility(View.GONE);
                                                attendingLabel.setVisibility(View.GONE);
                                                cannotAttendLabel.setVisibility(View.GONE);

                                                infoButton.setVisibility(View.VISIBLE);

                                                // allow admin to delete event by clicking delete button
                                                deleteButton.setOnClickListener(v -> {
                                                    PermanentWarningFragment fragment = PermanentWarningFragment.newInstance(
                                                            () -> EventDb.getInstance().deleteEvent(
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
                                            else if (userIsOrganizer && user.getDeviceId().equals(event.getOrganizerDeviceId())) {
                                                //TODO: when event edit opening is done, add:
                                                /*
                                                Log.e(TAG, "You shouldn't be here");
                                                 */
                                                bottomBar.setVisibility(View.GONE);
                                                reportButton.setVisibility(View.GONE);
                                                deleteButton.setVisibility(View.VISIBLE);

                                                deleteButton.setOnClickListener(v ->
                                                    EventDb.getInstance().deleteEvent(
                                                            event.getEventId(),
                                                            () -> {
                                                                Log.d(TAG, "Event deleted");
                                                                getParentFragmentManager().popBackStack();
                                                            },
                                                            e -> Log.e(TAG, "Error deleting event: " + e)
                                                    )
                                                );
                                            }
                                            else {
                                                reportButton.setVisibility(View.VISIBLE);
                                                deleteButton.setVisibility(View.GONE);
                                                bottomBar.setVisibility(View.VISIBLE);

                                                // set report button functionality
                                                reportButton.setOnClickListener(v -> { //TODO
                                                            Log.w(TAG, "Report button not implemented");
                                                            Toast.makeText(v.getContext(), "Report button not implemented", Toast.LENGTH_SHORT).show();
                                                        }
                                                );

                                                // check which list user is in and display corresponding content
                                                if (event.registrationList.getAttendingList().contains(userId)) {
                                                    eventDeadline.setVisibility(View.GONE);
                                                    waitingListCount.setVisibility(View.GONE);
                                                    attendeesCount.setVisibility(View.VISIBLE);
                                                    String attendeesText = event.registrationList.getAttendingList().size() + "/" + event.getCapacity() + " people are participating ";
                                                    attendeesCount.setText(attendeesText);

                                                    joinButton.setVisibility(View.GONE);
                                                    leaveButton.setVisibility(View.GONE);
                                                    selectButtonSet.setVisibility(View.GONE);
                                                    attendingLabel.setVisibility(View.VISIBLE);
                                                    cannotAttendLabel.setVisibility(View.GONE);

                                                    infoButton.setVisibility(View.GONE);
                                                }
                                                else if (event.registrationList.getSelectedList().contains(userId)) {
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
                                                    acceptButton.setOnClickListener(v ->
                                                        event.registrationList.addToAttendingList(userId)
                                                    );
                                                    // move user from selectedList to declinedList on decline
                                                    declineButton.setOnClickListener(v ->
                                                        event.registrationList.addToDeclinedList(userId)
                                                    );
                                                }
                                                else if (event.registrationList.getWaitingList().contains(userId)) {
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
                                                    leaveButton.setOnClickListener(v ->
                                                        event.registrationList.addToCancelledList(userId)
                                                    );
                                                } else if (event.registrationList.getRemovedList().contains(userId)) {
                                                    eventDeadline.setVisibility(View.GONE);
                                                    waitingListCount.setVisibility(View.GONE);
                                                    attendeesCount.setVisibility(View.GONE);

                                                    joinButton.setVisibility(View.GONE);
                                                    leaveButton.setVisibility(View.GONE);
                                                    selectButtonSet.setVisibility(View.GONE);
                                                    attendingLabel.setVisibility(View.GONE);
                                                    cannotAttendLabel.setVisibility(View.VISIBLE);

                                                    infoButton.setVisibility(View.GONE);
                                                } else if (event.getEmptySlotAmount() == 0) {
                                                    eventDeadline.setVisibility(View.GONE);
                                                    waitingListCount.setVisibility(View.GONE);
                                                    attendeesCount.setVisibility(View.VISIBLE);

                                                    joinButton.setVisibility(View.GONE);
                                                    leaveButton.setVisibility(View.GONE);
                                                    selectButtonSet.setVisibility(View.GONE);
                                                    attendingLabel.setVisibility(View.GONE);
                                                    cannotAttendLabel.setVisibility(View.VISIBLE);

                                                    infoButton.setVisibility(View.VISIBLE);
                                                } else if (event.getRegistrationTimeEndAsDateTime().isBefore(LocalDateTime.now())) {
                                                    eventDeadline.setVisibility(View.VISIBLE);
                                                    waitingListCount.setVisibility(View.GONE);
                                                    attendeesCount.setVisibility(View.GONE);

                                                    joinButton.setVisibility(View.GONE);
                                                    leaveButton.setVisibility(View.GONE);
                                                    selectButtonSet.setVisibility(View.GONE);
                                                    attendingLabel.setVisibility(View.GONE);
                                                    cannotAttendLabel.setVisibility(View.VISIBLE);

                                                    infoButton.setVisibility(View.VISIBLE);
                                                } else {
                                                    eventDeadline.setVisibility(View.VISIBLE);
                                                    waitingListCount.setVisibility(View.VISIBLE);
                                                    attendeesCount.setVisibility(View.VISIBLE);

                                                    joinButton.setVisibility(View.VISIBLE);
                                                    leaveButton.setVisibility(View.GONE);
                                                    selectButtonSet.setVisibility(View.GONE);
                                                    attendingLabel.setVisibility(View.GONE);
                                                    cannotAttendLabel.setVisibility(View.GONE);

                                                    infoButton.setVisibility(View.VISIBLE);

                                                    // add user to waitingList when Join Pool is clicked
                                                    joinButton.setOnClickListener(v -> {
                                                        int e = event.registrationList.addToWaitingList(userId);
                                                        if (e > 0) {
                                                            Log.e(TAG, "Could not add user to waiting list: " + e);
                                                        }
                                                    });
                                                }
                                            }
                                        } else {
                                            Log.e(TAG, "No such event available");
                                        }
                                    },
                                    e -> Log.e(TAG, "Error fetching event: " + e)
                            );
                },
                e -> Log.e(TAG, "Error fetching user" + e)
        );
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