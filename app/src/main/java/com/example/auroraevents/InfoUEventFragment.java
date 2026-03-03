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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class InfoUEventFragment extends Fragment {

    private static final String TAG = "InfoUEventFragment";

    private String eventId;
    private String userId;
    private TextView eventName, eventDescription, eventLocation, eventDateTime;
    private TextView eventOrganizer, eventDeadline, waitingListCount, attendeesCount, attendingLabel;
    private ImageView poster;
    private Button backButton, joinButton, acceptButton, declineButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.info_u_event_fragment, container, false);

        // get eventId from bundle
        eventId = getArguments().getString("eventId");

        // get device id
        userId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // get views
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

        // back button
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // fetch event from firestore
        FirebaseFirestore eventsDb = FirebaseFirestore.getInstance();
        eventsDb.collection("Events")
                .document(eventId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                Event event = document.toObject(Event.class);
                                event.setEventId(document.getId());

                                // show event details on screen
                                eventName.setText(event.getName());
                                eventDescription.setText(event.getDescription());
                                eventLocation.setText(event.getLocation());
                                eventDateTime.setText(event.getDateTime().toString());
                                eventOrganizer.setText("Organizer: " + event.getOrganizerDeviceId());
                                waitingListCount.setText(event.getWaitingList().size() + "people are waiting");
                                attendeesCount.setText(event.getAttendingList().size() + " people are participating");
                                eventDeadline.setText("");
                                poster.setVisibility(View.GONE);

                                // check which list user is in
                                if (event.getAttendingList().contains(userId)) {
                                    joinButton.setVisibility(View.GONE);
                                    acceptButton.setVisibility(View.GONE);
                                    declineButton.setVisibility(View.GONE);
                                    attendingLabel.setVisibility(View.VISIBLE);
                                    attendingLabel.setText("You are attending");

                                } else if (event.getSelectedList().contains(userId)) {
                                    joinButton.setVisibility(View.GONE);
                                    acceptButton.setVisibility(View.VISIBLE);
                                    declineButton.setVisibility(View.VISIBLE);
                                    attendingLabel.setVisibility(View.GONE);

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
                                    joinButton.setVisibility(View.VISIBLE);
                                    joinButton.setText("Leave Pool");
                                    acceptButton.setVisibility(View.GONE);
                                    declineButton.setVisibility(View.GONE);
                                    attendingLabel.setVisibility(View.GONE);

                                    joinButton.setOnClickListener(v -> {
                                        EventDb.getInstance().removeUserFromList(
                                                event.getEventId(),
                                                EventDb.LIST_WAITING,
                                                userId,
                                                () -> {
                                                    joinButton.setText("Join Pool");
                                                    Log.d(TAG, "User left waiting list");
                                                },
                                                e -> Log.d(TAG, "Error leaving: " + e.getMessage()));
                                    });

                                } else {
                                    joinButton.setVisibility(View.VISIBLE);
                                    joinButton.setText("Join Pool");
                                    acceptButton.setVisibility(View.GONE);
                                    declineButton.setVisibility(View.GONE);
                                    attendingLabel.setVisibility(View.GONE);

                                    joinButton.setOnClickListener(v -> {
                                        EventDb.getInstance().addUserToList(
                                                event.getEventId(),
                                                EventDb.LIST_WAITING,
                                                userId,
                                                () -> {
                                                    joinButton.setText("Leave Pool");
                                                    Log.d(TAG, "User joined waiting list");
                                                },
                                                e -> Log.d(TAG, "Error joining: " + e.getMessage()));
                                    });
                                }
                            } else {
                                Log.d(TAG, "No such event available");
                            }
                        } else {
                            Log.d(TAG, "Error: " + task.getException().getMessage());
                        }
                    }
                });
        return view;
    }
}