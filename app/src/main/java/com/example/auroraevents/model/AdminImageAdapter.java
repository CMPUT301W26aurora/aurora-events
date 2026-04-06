// Resources used:
//https://www.geeksforgeeks.org/android/how-to-build-an-image-gallery-android-app-with-recyclerview-and-glide/
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
import com.example.auroraevents.server.UserDb;
import com.example.auroraevents.view.AdminImageFragment;

import java.util.List;

/**
 * Displays images that the admin can browse through
 * Each image displays the event name and the user that uploaded it
 */
public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.ImageViewHolder> {

    /**
     * Allows admin to click images to view image details
     */
    public interface OnImageInteractionListener {
        void onImageClicked(Event event);
    }

    private List<Event> eventList;
    private final OnImageInteractionListener listener;

    public AdminImageAdapter(List<Event> eventList, OnImageInteractionListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    /**
     * @param events list of all the events that include images
     */
    public void setEvents(List<Event> events) {
        this.eventList = events;
        notifyDataSetChanged();
    }

    public abstract static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public abstract void bind(Event event, OnImageInteractionListener listener);
    }

    /**
     * Displays the thumbnail for the associated event, event name, and the user that uploaded it
     */
    public static class AdminImageViewHolder extends ImageViewHolder {
        private final ImageView thumbnail;
        private final TextView eventName;
        private final TextView imageUploader;

        AdminImageViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.image_thumbnail);
            eventName = itemView.findViewById(R.id.name_of_event);
            imageUploader = itemView.findViewById(R.id.image_uploader);
        }

        /**
         * Retrieves the poster image.
         * Retrieves organizer name to display name of image uploader.
         * @param event the event whose poster is displayed
         * @param listener
         */
        @Override
        public void bind(Event event, OnImageInteractionListener listener) {
            eventName.setText(event.getName());

            // fetch organizer name
            UserDb.getInstance().getUser(event.getOrganizerDeviceId(), user -> {
                        if (user != null && user.getName() != null) {
                            itemView.post(() -> imageUploader.setText("Image uploaded by: " + user.getName()));
                        } else {
                            itemView.post(() -> imageUploader.setText("Image uploaded by: Unknown"));
                        }
                    },
                    e -> itemView.post(() -> imageUploader.setText("Failed to fetch image uploader"))
            );
            // load image
            Glide.with(itemView.getContext())
                    .load(event.getPosterUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(thumbnail);

            // display image details when it is clicked
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImageClicked(event);
                }
            });
        }
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_image, parent, false);
        return new AdminImageViewHolder(view);
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

