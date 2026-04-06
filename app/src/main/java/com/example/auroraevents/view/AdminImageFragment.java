package com.example.auroraevents.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.auroraevents.R;
import com.example.auroraevents.model.AdminImageAdapter;
import com.example.auroraevents.model.Event;
import com.example.auroraevents.server.EventDb;
import com.example.auroraevents.server.UserDb;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

/**
 * Allows admin to:
 * browse through images
 * click an image for enlarged view
 * delete image with confirmation dialog
 */
public class AdminImageFragment extends Fragment {

    private static final String TAG = "AdminImageFragment";
    private RecyclerView imageRecyclerView;
    private AdminImageAdapter imageAdapter;
    private final ArrayList<Event> imageEventList = new ArrayList<>();
    private ListenerRegistration imageListenerRegistration;

    /**
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     * @return fragment view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_images, container, false);

        imageRecyclerView = view.findViewById(R.id.recycler_view_images);

        imageAdapter = new AdminImageAdapter(imageEventList,
                event -> displayImageDetails(event));

        imageRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        imageRecyclerView.setAdapter(imageAdapter);
        startImageListener();
        return view;
    }
    private void startImageListener() {
        imageListenerRegistration = EventDb.getInstance().eventListenerAll(
                events -> {
                    imageEventList.clear();

                    for (Event event : events) {
                        // only include events that have a poster image uploaded
                        if (event.getPosterUrl() != null
                                && !event.getPosterUrl().trim().isEmpty()) {
                            imageEventList.add(event);
                        }
                    }
                    fetchUploaderNames();
                },
                e -> {
                    Log.e(TAG, "Failed to load images", e);
                    Toast.makeText(getContext(), "Failed to load images", Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void fetchUploaderNames() {
        imageAdapter.notifyDataSetChanged();

        for (int i = 0; i < imageEventList.size(); i++) {
            Event event = imageEventList.get(i);
            int position = i;

            UserDb.getInstance().getUser(
                    event.getOrganizerDeviceId(),
                    user -> {
                        // find the ViewHolder for this position
                        RecyclerView.ViewHolder viewHolder =
                                imageRecyclerView.findViewHolderForAdapterPosition(position);

                        if (viewHolder instanceof AdminImageAdapter.ImageViewHolder) {
                            AdminImageAdapter.ImageViewHolder imageViewHolder =
                                    (AdminImageAdapter.ImageViewHolder) viewHolder;

                            String uploaderName = (user != null && user.getName() != null) ? "Uploaded by: " + user.getName()
                                    : "Uploaded by: Unknown";

                            imageViewHolder.itemView.post(() ->
                                    imageViewHolder.setUploaderName(uploaderName));
                        }
                    },
                    e -> Log.e(TAG, "Failed to fetch uploader for event: "
                            + event.getEventId(), e)
            );
        }
    }

    /**
     * Displays dialog with:
     * enlarged image view
     * event name
     * image uploader name
     * delete image button
     * @param event event whose image is displayed
     */
    private void displayImageDetails(Event event) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_admin_image_details, null);

        ImageView enlargeImage  = dialogView.findViewById(R.id.image_enlarge_preview);
        TextView  eventName     = dialogView.findViewById(R.id.event_name_text);
        TextView  imageUploader = dialogView.findViewById(R.id.image_uploader_text);
        Button    deleteImageButton = dialogView.findViewById(R.id.delete_image_button);

        Glide.with(requireContext())
                .load(event.getPosterUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerInside()
                .into(enlargeImage);

        eventName.setText(event.getName());

        // fetch uploader name
        UserDb.getInstance().getUser(
                event.getOrganizerDeviceId(),
                user -> {
                    String uploaderName = (user != null && user.getName() != null
                            && !user.getName().isEmpty())
                            ? "Uploaded by: " + user.getName()
                            : "Uploaded by: Unknown";
                    imageUploader.post(() -> imageUploader.setText(uploaderName));
                },
                e -> imageUploader.post(() ->
                        imageUploader.setText("Failed to fetch uploader"))
        );

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        deleteImageButton.setOnClickListener(v -> {
            dialog.dismiss();
            confirmDeleteImage(event);
        });

        dialog.show();
    }

    /**
     * Shows confirmation dialog before deleting image
     * Delete selected image
     *
     * @param event event whose image is deleted
     */
    private void confirmDeleteImage(Event event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete image?")
                .setPositiveButton("Delete", (dialog, which) ->
                        EventDb.getInstance().deletePoster(
                                event.getEventId(),
                                () -> {
                                    imageRecyclerView.post(() -> Toast.makeText(getContext(), "Image Deleted", Toast.LENGTH_SHORT).show());
                                },
                                e -> {
                                    Log.e(TAG, "Failed to delete image", e);
                                    imageRecyclerView.post(() -> Toast.makeText(getContext(), "Failed to delete image.", Toast.LENGTH_SHORT).show());
                                }
                        )
                )
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (imageListenerRegistration != null) {
            imageListenerRegistration.remove();
            imageListenerRegistration = null;
        }
    }
}