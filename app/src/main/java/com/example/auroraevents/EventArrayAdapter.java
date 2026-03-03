package com.example.auroraevents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * Converts a list of Event objects to rows
 * Each event row displays the event details i.e.
 * name, description, status tag
 */
public class EventArrayAdapter extends ArrayAdapter<Event> {
    /** get the user's deviceId*/
    private final String userId;

    /**
     *
     * @param context
     * @param events: list of event objects
     * @param userId: device id of the user to identify status tag
     */
    public EventArrayAdapter(Context context, ArrayList<Event> events, String userId){
        super(context, 0,events);
        this.userId = userId;
    }

    /**
     *
     * @param position The position of the item in the list
     * @param convertview The old view to reuse, if possible
     * @param parent The parent that this view will eventually be attached to
     * @return Event row display event details
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertview, @NonNull ViewGroup parent){
        View view;
        if (convertview == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.event_list_item, parent, false);
        } else {
            view = convertview;
        }
        Event event = getItem(position);
        TextView eventName = view.findViewById(R.id.event_name);
        TextView eventDescription = view.findViewById(R.id.event_description);
        TextView eventTag = view.findViewById(R.id.event_tag);

        eventName.setText(event.getName());
        eventDescription.setText(event.getDescription());

        // check user status and update status tag
        if (event.getAttendingList() != null && event.getAttendingList().contains(userId)) {
            eventTag.setText("Attending");
        } else if (event.getSelectedList() != null && event.getSelectedList().contains(userId)) {
            eventTag.setText("Invited");
        } else if (event.getWaitingList() != null && event.getWaitingList().contains(userId)) {
            eventTag.setText("Waiting");
        } else {
            eventTag.setText("");
        }
        return view;
    }
}
