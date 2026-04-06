package com.example.auroraevents.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.auroraevents.R;

import java.util.ArrayList;

/**
 * Converts a list of Event objects to rows
 * Each event row displays the event details i.e.
 * name, description
 * @author Alina Iqbal
 */
public class EventArrayAdapter extends ArrayAdapter <com.example.auroraevents.model.Event> {
    /**
     * Constructor for the array adapter
     *
     * @param context current context used to inflate layout
     * @param events: list of event objects
     */
    public EventArrayAdapter(Context context, ArrayList <com.example.auroraevents.model.Event> events){
        super(context, 0,events);
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
        eventName.setText(event.getName());
        eventDescription.setText(event.getDescription());
        return view;
    }
}
