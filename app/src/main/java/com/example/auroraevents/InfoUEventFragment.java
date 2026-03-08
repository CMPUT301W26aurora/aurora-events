// resources used:
// https://firebase.google.com/docs/firestore/query-data/get-data
// https://firebase.google.com/docs/firestore/query-data/listen
// https://stackoverflow.com/questions/63312913/check-if-a-user-id-exists-in-arraylist
//https://www.c-sharpcorner.com/UploadFile/8836be/set-visibility-on-buttons-in-android/
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

/**
 * Displays event details for the event tapped by the user.
 * Gets the event details using the event ID.
 * Implements US 01.01.01 - View event details.
 * Implements US 01.01.03
 * Implements US 01.06.02 - Sign up for an event from event details.
 * Implements US US 03.04.01 - Show admin view for events
 */

public class InfoUEventFragment extends Fragment {

    private static final String TAG = "InfoUEventFragment";

    private String eventId;
    private String userId;
    private TextView eventName, eventDescription, eventLocation, eventDateTime;
    private TextView eventOrganizer, eventDeadline, waitingListCount, attendeesCount, attendingLabel;
    private ImageView poster;
    private Button backButton, joinButton, acceptButton, declineButton, deleteButton;

    /**
     * Displays event details' screen UI.
     * Fetches the user's device ID.
     * Fetches event ID.
     * Displays required buttons for the event details screen.
     * Fetches the event from Firestore.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return view for this fragment displaying event details.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.info_u_event_fragment, container, false);

        // get eventId from EventFragment via bundle
        eventId = getArguments().getString("eventId");

        // get device id to identify user
        userId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

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

        // fetch event clicked by user from Firestore via EventDb to display its details
        EventDb.getInstance().getEvent(
                eventId,
                event -> {
                    if (event != null) {
                        // display common event details
                        eventName.setText(event.getName());
                        eventDescription.setText(event.getDescription());
                        eventLocation.setText(event.getLocation());
                        eventDateTime.setText(event.getDateTime().toString());
                        eventOrganizer.setText("Organizer: " + event.getOrganizerDeviceId());
                        eventDeadline.setText("");
                        poster.setVisibility(View.GONE);

                        // check user role using UserDb to determine which buttons to display
                        UserDb.getInstance().getUser(
                                userId,
                                user -> {
                                    if (user != null && user.getRole().equals(User.ROLE_ADMIN)) {
                                        // hide entrants buttons and show delete button for admin view
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
                                        // show waiting list count and attendees count for entrants view
                                        waitingListCount.setVisibility(View.VISIBLE);
                                        attendeesCount.setVisibility(View.VISIBLE);
                                        waitingListCount.setText(event.getWaitingList().size() + " people are waiting");
                                        attendeesCount.setText(event.getAttendingList().size() + " people are participating");
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
                                                        () -> {
                                                            acceptButton.setVisibility(View.GONE);
                                                            declineButton.setVisibility(View.GONE);
                                                            attendingLabel.setVisibility(View.VISIBLE);
                                                            attendingLabel.setText("You are attending");
                                                            Log.d(TAG, "User accepted invitation");
                                                        },
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
                                                        () -> {
                                                            acceptButton.setVisibility(View.GONE);
                                                            declineButton.setVisibility(View.GONE);
                                                            joinButton.setVisibility(View.VISIBLE);
                                                            joinButton.setText("Join Pool");
                                                            Log.d(TAG, "User declined invitation");
                                                        },
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
                                                        () -> {
                                                            joinButton.setText("Join Pool");
                                                            Log.d(TAG, "User left waiting list");
                                                        },
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
                                                        () -> {
                                                            joinButton.setText("Leave Pool");
                                                            Log.d(TAG, "User joined waiting list");
                                                        },
                                                        e -> Log.d(TAG, "Error joining waiting list: " + e)
                                                );
                                            });
                                        }
                                    }
                                },
                                e -> Log.d(TAG, "Error fetching user: " + e)
                        );
                    } else {
                        Log.d(TAG, "No such event available");
                    }
                },
                e -> Log.d(TAG, "Error fetching event: " + e)
        );

        return view;
    }
}