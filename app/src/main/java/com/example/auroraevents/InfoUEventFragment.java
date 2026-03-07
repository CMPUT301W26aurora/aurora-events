// resources used:
// https://firebase.google.com/docs/firestore/query-data/get-data#get_a_document
// https://stackoverflow.com/questions/63312913/check-if-a-user-id-exists-in-arraylist
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
 */

public class InfoUEventFragment extends Fragment {

    private static final String TAG = "InfoUEventFragment";

    private String eventId;
    private String userId;
    private TextView eventName, eventDescription, eventLocation, eventDateTime;
    private TextView eventOrganizer, eventDeadline, waitingListCount, attendeesCount, attendingLabel;
    private ImageView poster;
    private Button backButton, joinButton, acceptButton, declineButton;

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

        // get eventId from EventFragment
        eventId = getArguments().getString("eventId");

        // get device id to identify user
        userId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // get views to display event information
        eventName = view.findViewById(R.id.event_name);
        eventDescription = view.findViewById(R.id.event_description);
        eventLocation = view.findViewById(R.id.event_location);
        eventDateTime = view.findViewById(R.id.event_date_time);
        eventOrganizer = view.findViewById(R.id.event_organizer);
        eventDeadline = view.findViewById(R.id.event_deadline);
        waitingListCount = view.findViewById(R.id.waiting_list_count);
        attendeesCount = view.findViewById(R.id.attendees_count);
        attendingLabel = view.findViewById(R.id.attending_label);
        poster = view.findViewById(R.id.poster_image);
        backButton = view.findViewById(R.id.back_button);
        joinButton = view.findViewById(R.id.join_button);
        acceptButton = view.findViewById(R.id.accept_button);
        declineButton = view.findViewById(R.id.decline_button);

        // back button to return to events list when it is clicked
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // fetch event clicked by user from firestore using EventDb to display its details
            EventDb.getInstance().getEvent(eventId, event -> {
                if (event != null) {
                    // display event details on screen
                    eventName.setText(event.getName());
                    eventDescription.setText(event.getDescription());
                    eventLocation.setText(event.getLocation());
                    eventDateTime.setText(event.getDateTime().toString());
                    eventOrganizer.setText("Organizer: " + event.getOrganizerDeviceId());
                    waitingListCount.setText(event.getWaitingList().size() + " people are waiting ");
                    attendeesCount.setText(event.getAttendingList().size() + " people are participating ");
                    eventDeadline.setText("");
                    poster.setVisibility(View.GONE);

                    // check which list user is in and display corresponding buttons
                    if (event.getAttendingList().contains(userId)) {
                        // selected user that is attending event
                        joinButton.setVisibility(View.GONE);
                        acceptButton.setVisibility(View.GONE);
                        declineButton.setVisibility(View.GONE);
                        attendingLabel.setVisibility(View.VISIBLE);
                        attendingLabel.setText("You are attending");

                    } else if (event.getSelectedList().contains(userId)) {
                        // user selected to attend event
                        joinButton.setVisibility(View.GONE);
                        acceptButton.setVisibility(View.VISIBLE);
                        declineButton.setVisibility(View.VISIBLE);
                        attendingLabel.setVisibility(View.GONE);

                        // move user from selectedList to the attendingList upon accepting event invite
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
                                    e -> Log.d(TAG, "Error accepting invitation: " + e.getMessage()));
                        });
                        // move user from selectedList to declinedList upon declining event invite
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
                                    e -> Log.d(TAG, "Error declining invitation: " + e.getMessage()));
                        });

                    } else if (event.getWaitingList().contains(userId)) {
                        // user is on waitingList
                        joinButton.setVisibility(View.VISIBLE);
                        joinButton.setText("Leave Pool");
                        acceptButton.setVisibility(View.GONE);
                        declineButton.setVisibility(View.GONE);
                        attendingLabel.setVisibility(View.GONE);

                        // remove user from waitingList once the user clicks Leave Pool
                        joinButton.setOnClickListener(v -> {
                            EventDb.getInstance().removeUserFromList(
                                    event.getEventId(),
                                    EventDb.LIST_WAITING,
                                    userId,
                                    () -> {
                                        joinButton.setText("Join Pool");
                                        Log.d(TAG, "User left waiting list");
                                        },
                                    e -> Log.d(TAG, "Error leaving waiting list: " + e.getMessage()));
                        });

                    } else {
                        // show Join Pool button if user is not on any list
                        joinButton.setVisibility(View.VISIBLE);
                        joinButton.setText("Join Pool");
                        acceptButton.setVisibility(View.GONE);
                        declineButton.setVisibility(View.GONE);
                        attendingLabel.setVisibility(View.GONE);

                        // add user to the waitingList when clicked on Join Pool button
                        joinButton.setOnClickListener(v -> {
                            EventDb.getInstance().addUserToList(
                                    event.getEventId(),
                                    EventDb.LIST_WAITING,
                                    userId,
                                    () -> {
                                        joinButton.setText("Leave Pool");
                                        Log.d(TAG, "User joined waiting list");
                                        },
                                    e -> Log.d(TAG, "Error joining waiting list: " + e.getMessage()));
                                    });
                    }
                } else {
                    Log.d(TAG, "No such event available");
                }
            }, e -> Log.d(TAG, "Error fetching event: " + e.getMessage())
);
            return view;
    }
}