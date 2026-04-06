package com.example.auroraevents.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.auroraevents.R;

import java.util.List;

/**
 * displays event poster images in the admin image list.
 */
public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.ImageViewHolder> {

    /**
     * opens the detail view for the selected event.
     */
    public interface OnImageInteractionListener {
        void onImageClicked(Event event);
    }

    private List<Event> eventList;
    private final OnImageInteractionListener listener;

    public AdminImageAdapter(List<Event> eventList, OnImageInteractionListener listener) {
        this.eventList = eventList;
        this.listener  = listener;
    }

    /**
     * Updates the event list
     *
     * @param events updated list of events that have poster images
     */
    public void setEvents(List<Event> events) {
        this.eventList = events;
        notifyDataSetChanged();
    }

    /**
     * Displays thumbnail, event name, and uploader name.
     */
    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        private final ImageView thumbnail;
        private final TextView  eventName;
        final TextView  imageUploader;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail     = itemView.findViewById(R.id.image_thumbnail);
            eventName     = itemView.findViewById(R.id.name_of_event);
            imageUploader = itemView.findViewById(R.id.image_uploader);
        }

        /**
         * @param event    the event whose poster to display
         * @param listener
         */
        public void bind(Event event, OnImageInteractionListener listener) {
            eventName.setText(event.getName());

            // load poster image
            Glide.with(itemView.getContext())
                    .load(event.getPosterUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(thumbnail);

            // open detail view when image is tapped
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImageClicked(event);
                }
            });
        }
        public void setUploaderName(String uploaderName) {
            imageUploader.setText(uploaderName);
        }
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.bind(eventList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return eventList == null ? 0 : eventList.size();
    }
}