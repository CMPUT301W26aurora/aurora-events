package com.example.auroraevents.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.auroraevents.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Renders a list of Notification objects for the organizer sent-notifications view.
 * Displays title, body, resolved recipient name, and timestamp.
 * @author Joshua Terry
 */
public class OrganizerNotificationAdapter
        extends RecyclerView.Adapter<OrganizerNotificationAdapter.ViewHolder> {
    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault());

    private final List<Notification> notifications = new ArrayList<>();

    public void setNotifications(List<Notification> updated) {
        notifications.clear();
        notifications.addAll(updated);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_organizer_notification, parent, false);
        return new ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification n = notifications.get(position);
        holder.title.setText(n.getTitle() != null ? n.getTitle() : "");
        holder.body.setText(n.getBody()   != null ? n.getBody()  : "");
        holder.recipient.setText("Sent to: " + (n.getRecipientName() != null ? n.getRecipientName() : "Unknown"));
        holder.timestamp.setText(n.getTimestamp() != null ? DATE_FMT.format(n.getTimestamp()) : "");
    }
    @Override
    public int getItemCount() {
        return notifications.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView body;
        final TextView recipient;
        final TextView timestamp;
        ViewHolder(@NonNull View v) {
            super(v);
            title     = v.findViewById(R.id.organizer_notif_title);
            body      = v.findViewById(R.id.organizer_notif_body);
            recipient = v.findViewById(R.id.organizer_notif_recipient);
            timestamp = v.findViewById(R.id.organizer_notif_timestamp);
        }
    }
}