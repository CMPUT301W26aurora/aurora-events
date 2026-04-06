package com.example.auroraevents.view;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.api.IMapController;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.auroraevents.R;

import java.io.IOException;
import java.util.List;


/*
Maps handled by OSMDroid: https://github.com/osmdroid/osmdroid
 */

/**
 * Opens a map picker to select a location
 * @author Arron Rossa
 */
public class MapPickerFragment extends Fragment {

    private static final String TAG = "MapPickerFragment";
    private MapView mapView;
    private Marker selectedMarker;
    private Button confirmButton;
    private GeoPoint selectedPoint;
    private int navBarStatus;

    public interface OnLocationPickedListener {
        void onLocationPicked(String address, double lat, double lng);
    }
    private OnLocationPickedListener listener;

    public void setOnLocationPickedListener(OnLocationPickedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_picker, container, false);
        View navBar = requireActivity().findViewById(R.id.nav_bar);
        navBarStatus = navBar.getVisibility();

        confirmButton = view.findViewById(R.id.btn_confirm_location);
        confirmButton.setEnabled(false);

        // Initialize OSMDroid
        Configuration.getInstance().load(
                requireContext(),
                requireContext().getSharedPreferences("osmdroid", 0)
        );

        // Map setup
        mapView = view.findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Default camera position
        IMapController controller = mapView.getController();
        controller.setZoom(15.0);
        controller.setCenter(new GeoPoint(53.5461, -113.4938)); // Edmonton

        // Tap to place pin
        MapEventsOverlay tapOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                selectedPoint = p;

                // Remove marker
                if (selectedMarker != null) {
                    mapView.getOverlays().remove(selectedMarker);
                }

                // Place marker
                selectedMarker = new Marker(mapView);
                selectedMarker.setPosition(p);
                selectedMarker.setTitle("Event Location");
                selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(selectedMarker);
                mapView.invalidate();

                confirmButton.setEnabled(true);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) { return false; }
        });
        mapView.getOverlays().add(tapOverlay);

        // Confirm button — reverse geocode, return result
        confirmButton.setOnClickListener(v -> {
            if (selectedPoint != null && listener != null) {
                new Thread(() -> {
                    try {
                        Geocoder geocoder = new Geocoder(requireContext());
                        List<Address> addresses = geocoder.getFromLocation(
                                selectedPoint.getLatitude(),
                                selectedPoint.getLongitude(), 1
                        );
                        String address = (addresses != null && !addresses.isEmpty())
                                ? addresses.get(0).getAddressLine(0)
                                : selectedPoint.getLatitude() + ", " + selectedPoint.getLongitude();

                        // Return to main, update UI
                        requireActivity().runOnUiThread(() -> {
                            listener.onLocationPicked(
                                    address,
                                    selectedPoint.getLatitude(),
                                    selectedPoint.getLongitude()
                            );
                            getParentFragmentManager().popBackStack();
                        });
                    } catch (IOException e) {
                        Log.e(TAG, "Geocoder failed", e);
                        requireActivity().runOnUiThread(() -> {
                            listener.onLocationPicked(
                                    selectedPoint.getLatitude() + ", " + selectedPoint.getLongitude(),
                                    selectedPoint.getLatitude(),
                                    selectedPoint.getLongitude()
                            );
                            getParentFragmentManager().popBackStack();
                        });
                    }
                }).start();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        // Hide nav bar when in fragment
        View navBar = requireActivity().findViewById(R.id.nav_bar);
        navBarStatus = navBar.getVisibility();
        navBar.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Show nav bar when leaving fragment
        View navBar = requireActivity().findViewById(R.id.nav_bar);
        if (navBar != null) {
            navBar.setVisibility(navBarStatus);
        }
    }

}