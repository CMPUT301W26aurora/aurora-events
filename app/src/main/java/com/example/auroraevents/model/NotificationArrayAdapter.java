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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Converts a list of Notification objects to rows.
 * Each row displays the notification title, body, and timestamp.
 */
public class NotificationArrayAdapter extends ArrayAdapter<Notification> {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault());

    public NotificationArrayAdapter(Context context, ArrayList<Notification> notifications) {
        super(context, 0, notifications);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_notification, parent, false);
        } else {
            view = convertView;
        }

        Notification notification = getItem(position);

        TextView title     = view.findViewById(R.id.notification_title);
        TextView body      = view.findViewById(R.id.notification_body);
        TextView timestamp = view.findViewById(R.id.notification_timestamp);

        assert notification != null;
        title.setText(notification.getTitle());
        body.setText(notification.getBody());
        timestamp.setText(notification.getTimestamp() != null
                ? DATE_FORMAT.format(notification.getTimestamp())
                : "");

        return view;
    }
}