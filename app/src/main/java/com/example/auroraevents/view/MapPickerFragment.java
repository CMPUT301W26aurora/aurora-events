package com.example.auroraevents.view;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.auroraevents.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

/*
Maps handled by Google Maps SDK: https://developers.google.com/maps/documentation/android-sdk/overview?section=start
 */
public class MapPickerFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapPickerFragment";
    private GoogleMap googleMap;
    private Marker selectedMarker;
    private Button confirmButton;
    private LatLng selectedLatLng;

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

        confirmButton = view.findViewById(R.id.btn_confirm_location);
        confirmButton.setEnabled(false);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        confirmButton.setOnClickListener(v -> {
            if (selectedLatLng != null && listener != null) {
                // Reverse geocode the tapped point to get an address string
                Geocoder geocoder = new Geocoder(requireContext());
                try {
                    List<Address> addresses = geocoder.getFromLocation(
                            selectedLatLng.latitude, selectedLatLng.longitude, 1);
                    String address = (addresses != null && !addresses.isEmpty())
                            ? addresses.get(0).getAddressLine(0)
                            : selectedLatLng.latitude + ", " + selectedLatLng.longitude;
                    listener.onLocationPicked(address, selectedLatLng.latitude, selectedLatLng.longitude);
                } catch (IOException e) {
                    Log.e(TAG, "Geocoder failed", e);
                    listener.onLocationPicked(
                            selectedLatLng.latitude + ", " + selectedLatLng.longitude,
                            selectedLatLng.latitude,
                            selectedLatLng.longitude
                    );
                }
                getParentFragmentManager().popBackStack();
            }
        });

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Drop pin where organizer taps
        googleMap.setOnMapClickListener(latLng -> {
            selectedLatLng = latLng;
            if (selectedMarker != null) selectedMarker.remove();
            selectedMarker = googleMap.addMarker(
                    new MarkerOptions().position(latLng).title("Event Location")
            );
            confirmButton.setEnabled(true);
        });
    }
}