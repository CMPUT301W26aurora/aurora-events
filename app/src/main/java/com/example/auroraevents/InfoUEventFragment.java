// resources used:
// https://firebase.google.com/docs/firestore/query-data/get-data
// https://firebase.google.com/docs/firestore/query-data/listen
// https://stackoverflow.com/questions/63312913/check-if-a-user-id-exists-in-arraylist
// https://www.c-sharpcorner.com/UploadFile/8836be/set-visibility-on-buttons-in-android/
package com.example.auroraevents;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

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

    private TextView eventName, eventDescription, eventLocation, eventDateTime;
    private TextView eventOrganizer, eventDeadline, waitingListCount, attendeesCount, attendingLabel;
    private ImageView poster;
    private Button backButton, joinButton, acceptButton, declineButton, deleteButton;

    /**
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return view displaying event information.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.info_u_event_fragment, container, false);

        // get eventId from EventFragment
        eventId = getArguments().getString("eventId");

        // get device id to identify user
        userId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("DEVICE_ID", "ANDROID_ID: " + userId);

        // get views to display event information
        eventName        = view.findViewById(R.id.event_name);
        eventDescription = view.findViewById(R.id.event_description);
        eventLocation    = view.findViewById(R.id.event_location);
        eventDateTime    = view.findViewById(R.id.event_date_time);
        eventOrganizer   = view.findViewById(R.id.event_organizer);
        eventDeadline    = view.findViewById(R.id.event_deadline);
        waitingListCount = view.findViewById(R.id.waiting_list_count);
        attendeesCount   = view.findViewById(R.id.attendees_count);
        attendingLabel   = view.findViewById(R.id.attending_label);
        poster           = view.findViewById(R.id.poster_image);
        backButton       = view.findViewById(R.id.back_button);
        joinButton       = view.findViewById(R.id.join_button);
        acceptButton     = view.findViewById(R.id.accept_button);
        declineButton    = view.findViewById(R.id.decline_button);
        deleteButton     = view.findViewById(R.id.delete_button);

        // back button returns to events list
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // sign in to firestore
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener(authResult -> {
                    Log.d(TAG, "Firebase sign in successful");
                    loadEventData();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase sign in failed", e);
                });

        return view;
    }

    /**
     * Checks user role and attaches snapshot listener
     */
    private void loadEventData() {
        UserDb.getInstance().getUser(
                userId,
                user -> {
                    // check if user is admin
                    boolean checkIfAdmin = false;
                    if (user != null) {
                        if (user.getRole().equals(User.ROLE_ADMIN)) {
                            checkIfAdmin = true;
                        }
                    }
                    final boolean userIsAdmin = checkIfAdmin;

                    // add snapshot listener
                    eventSnapshotListener = EventDb.getInstance().addSnapshotListenerForEvent(
                            eventId,
                            event -> {
                                if (event != null) {
                                    // display event details for all types of users
                                    eventName.setText(event.getName());
                                    eventDescription.setText(event.getDescription());
                                    eventLocation.setText(event.getLocation());
                                    eventDateTime.setText(event.getDateTime().toString());
                                    eventOrganizer.setText("Organizer: " + event.getOrganizerDeviceId());
                                    eventDeadline.setText("");
                                    poster.setVisibility(View.GONE);

                                    if (userIsAdmin) {
                                        // display event view for admin
                                        joinButton.setVisibility(View.GONE);
                                        acceptButton.setVisibility(View.GONE);
                                        declineButton.setVisibility(View.GONE);
                                        attendingLabel.setVisibility(View.GONE);
                                        waitingListCount.setVisibility(View.GONE);
                                        attendeesCount.setVisibility(View.GONE);
                                        deleteButton.setVisibility(View.VISIBLE);

                                        // delete event when admin clicks delete button
                                        deleteButton.setOnClickListener(v -> {
                                            EventDb.getInstance().deleteEvent(
                                                    event.getEventId(),
                                                    () -> {
                                                        Log.d(TAG, "Event deleted by admin");
                                                        getParentFragmentManager().popBackStack();
                                                    },
                                                    e -> Log.d(TAG, "Error deleting event: " + e)
                                            );
                                        });

                                    } else {
                                        // show waiting list count and attendees count for entrant
                                        waitingListCount.setVisibility(View.VISIBLE);
                                        attendeesCount.setVisibility(View.VISIBLE);
                                        waitingListCount.setText(event.getWaitingList().size() + " people are waiting ");
                                        attendeesCount.setText(event.getAttendingList().size() + " people are participating ");
                                        deleteButton.setVisibility(View.GONE);

                                        // check which list user is in and display corresponding buttons
                                        if (event.getAttendingList().contains(userId)) {
                                            // user is already attending event
                                            joinButton.setVisibility(View.GONE);
                                            acceptButton.setVisibility(View.GONE);
                                            declineButton.setVisibility(View.GONE);
                                            attendingLabel.setVisibility(View.VISIBLE);
                                            attendingLabel.setText("You are attending");

                                        } else if (event.getSelectedList().contains(userId)) {
                                            // user has been selected and needs to accept or decline
                                            joinButton.setVisibility(View.GONE);
                                            acceptButton.setVisibility(View.VISIBLE);
                                            declineButton.setVisibility(View.VISIBLE);
                                            attendingLabel.setVisibility(View.GONE);

                                            // move user from selectedList to attendingList on accept
                                            acceptButton.setOnClickListener(v -> {
                                                EventDb.getInstance().moveUserBetweenLists(
                                                        event.getEventId(),
                                                        EventDb.LIST_SELECTED,
                                                        EventDb.LIST_ATTENDING,
                                                        userId,
                                                        () -> Log.d(TAG, "User accepted invitation"),
                                                        e -> Log.d(TAG, "Error accepting invitation: " + e)
                                                );
                                            });

                                            // move user from selectedList to declinedList on decline
                                            declineButton.setOnClickListener(v -> {
                                                EventDb.getInstance().moveUserBetweenLists(
                                                        event.getEventId(),
                                                        EventDb.LIST_SELECTED,
                                                        EventDb.LIST_DECLINED,
                                                        userId,
                                                        () -> Log.d(TAG, "User declined invitation"),
                                                        e -> Log.d(TAG, "Error declining invitation: " + e)
                                                );
                                            });

                                        } else if (event.getWaitingList().contains(userId)) {
                                            // user is on waiting list
                                            joinButton.setVisibility(View.VISIBLE);
                                            joinButton.setText("Leave Pool");
                                            acceptButton.setVisibility(View.GONE);
                                            declineButton.setVisibility(View.GONE);
                                            attendingLabel.setVisibility(View.GONE);

                                            // remove user from waitingList when Leave Pool is clicked
                                            joinButton.setOnClickListener(v -> {
                                                EventDb.getInstance().removeUserFromList(
                                                        event.getEventId(),
                                                        EventDb.LIST_WAITING,
                                                        userId,
                                                        () -> Log.d(TAG, "User left waiting list"),
                                                        e -> Log.d(TAG, "Error leaving waiting list: " + e)
                                                );
                                            });

                                        } else {
                                            // user is not on any list so show Join Pool button
                                            joinButton.setVisibility(View.VISIBLE);
                                            joinButton.setText("Join Pool");
                                            acceptButton.setVisibility(View.GONE);
                                            declineButton.setVisibility(View.GONE);
                                            attendingLabel.setVisibility(View.GONE);

                                            // add user to waitingList when Join Pool is clicked
                                            joinButton.setOnClickListener(v -> {
                                                EventDb.getInstance().addUserToList(
                                                        event.getEventId(),
                                                        EventDb.LIST_WAITING,
                                                        userId,
                                                        () -> Log.d(TAG, "User joined waiting list"),
                                                        e -> Log.d(TAG, "Error joining waiting list: " + e)
                                                );
                                            });
                                        }
                                    }
                                } else {
                                    Log.d(TAG, "No such event available");
                                }
                            },
                            e -> Log.d(TAG, "Error fetching event: " + e)
                    );
                },
                e -> Log.d(TAG, "Error fetching user: " + e)
        );
    }

    /**
     * Detach the snapshot listener.
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