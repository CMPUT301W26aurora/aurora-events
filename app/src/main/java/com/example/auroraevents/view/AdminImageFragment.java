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

import java.util.ArrayList;

/**
 * Allows admin to:
 * browse through images
 * click an image for enlarged view
 * delete image with confirmation dialog
 */
public class AdminImageFragment extends Fragment {
    private final String TAG = "AdminImageFragment";
    private RecyclerView imageRecyclerView;
    private AdminImageAdapter imageAdapter;
    private final ArrayList<Event> imageEventList = new ArrayList<>();

    /**
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return fragment view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_images, container, false);

        imageRecyclerView = view.findViewById(R.id.recycler_view_images);

        imageAdapter = new AdminImageAdapter(imageEventList, event -> displayImageDetails(event));

        imageRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        imageRecyclerView.setAdapter(imageAdapter);

        loadImages();

        return view;
    }

    /**
     * load images for events that contain images
     */
    private void loadImages() {
        EventDb.getInstance().getAllEvents(events -> {

            imageEventList.clear();

            for (Event event : events) {
                if (event.getPosterUrl() != null && !event.getPosterUrl().trim().isEmpty()) {
                    imageEventList.add(event);
                }
            }

            imageAdapter.notifyDataSetChanged();

        }, e -> {
            Log.e(TAG, "Failed to load images", e);
            Toast.makeText(getContext(), "Failed to load images", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Displays dialog
     * Includes:
     * enlarged image view
     * image uploader
     * delete image button
     * @param event chose event whose image is displayed
     */
    private void displayImageDetails(Event event) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_image_details, null);

        ImageView enlargeImage = dialogView.findViewById(R.id.image_enlarge_preview);
        TextView eventName = dialogView.findViewById(R.id.event_name_text);
        TextView imageUploader = dialogView.findViewById(R.id.image_uploader_text);
        Button deleteImageButton = dialogView.findViewById(R.id.delete_image_button);

        Glide.with(requireContext())
                .load(event.getPosterUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerInside()
                .into(enlargeImage);

        eventName.setText(event.getName());

        // get image uploader name
        UserDb.getInstance().getUser(
                event.getOrganizerDeviceId(),
                user -> {
                    if (user != null && user.getName() != null && !user.getName().isEmpty()) {
                        imageUploader.setText("Image uploaded by: " + user.getName());
                    } else {
                        imageUploader.setText("Image uploaded by: Unknown");
                    }
                },
                e -> imageUploader.setText("Failed to fetch uploader")
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
     * show confirmation dialog before deleting image
     * deletes image from firestore
     * reload image list
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
                                    Toast.makeText(getContext(), "Image Deleted", Toast.LENGTH_SHORT).show();
                                    loadImages();
                                },
                                e -> {
                                    Log.e(TAG, "Failed to delete image", e);
                                    Toast.makeText(getContext(), "Failed to delete image.", Toast.LENGTH_SHORT).show();
                                }
                        )
                )
                // cancel image deletion
                .setNegativeButton("Cancel", null)
                .show();
    }
}