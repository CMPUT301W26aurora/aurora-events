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
 * Renders a list of Notification objects for the organizer sent-notifications view.
 * Displays title, body, resolved recipient name, and timestamp.
 */
public class OrganizerNotificationArrayAdapter extends ArrayAdapter<Notification> {

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault());

    private final LayoutInflater inflater;

    public OrganizerNotificationArrayAdapter(@NonNull Context context,
                                             @NonNull ArrayList<Notification> notifications) {
        super(context, 0, notifications);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_organizer_notification, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Notification n = getItem(position);
        if (n == null) return convertView;

        holder.title.setText(n.getTitle() != null ? n.getTitle() : "");
        holder.body.setText(n.getBody()   != null ? n.getBody()  : "");
        holder.recipient.setText("To: " + (n.getRecipientName() != null ? n.getRecipientName() : "Unknown"));
        holder.timestamp.setText(n.getTimestamp() != null ? DATE_FMT.format(n.getTimestamp()) : "");

        return convertView;
    }

    private static class ViewHolder {
        final TextView title;
        final TextView body;
        final TextView recipient;
        final TextView timestamp;

        ViewHolder(View v) {
            title     = v.findViewById(R.id.organizer_notif_title);
            body      = v.findViewById(R.id.organizer_notif_body);
            recipient = v.findViewById(R.id.organizer_notif_recipient);
            timestamp = v.findViewById(R.id.organizer_notif_timestamp);
        }
    }
}