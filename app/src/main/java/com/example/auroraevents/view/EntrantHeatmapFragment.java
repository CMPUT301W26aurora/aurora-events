package com.example.auroraevents.view;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.auroraevents.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a heatmap of all entrants for an event
 * Based off: https://github.com/osmdroid/osmdroid/blob/master/OpenStreetMapViewer/src/main/java/org/osmdroid/samplefragments/data/HeatMap.java
 * @author Arron Rossa
 */
public class EntrantHeatmapFragment extends Fragment {

    private static final String TAG = "HeatmapFragment";
    private MapView mapView;
    private String eventId;
    private ImageButton backButton;

    private static final double EDMONTON_LAT = 53.5461;
    private static final double EDMONTON_LNG = -113.4938;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entrant_heatmap, container, false);

        // Retrieve eventId
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId");
        }

        // Initialize OSMDroid
        Configuration.getInstance().load(
                requireContext(),
                requireContext().getSharedPreferences("osmdroid", 0)
        );

        mapView = view.findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Center on Edmonton
        IMapController controller = mapView.getController();
        controller.setZoom(12.0);
        controller.setCenter(new GeoPoint(EDMONTON_LAT, EDMONTON_LNG));

        loadHeatmap();

        backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        return view;
    }

    /**
     * Load heatmap data from firebase
     */
    private void loadHeatmap() {
        FirebaseFirestore.getInstance()
                .collection("Events")
                .document(eventId)
                .collection("entrantLocations")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<GeoPoint> points = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");
                        if (lat != null && lng != null) {
                            points.add(new GeoPoint(lat, lng));
                        }
                    }
                    if (!points.isEmpty()) {
                        renderHeatmap(points);
                    } else {
                        // No locations yet
                        Toast.makeText(requireContext(),
                                "No entrant locations recorded yet.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch locations", e);
                    Toast.makeText(requireContext(),
                            "Failed to load heatmap data.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Render heatmap
     * @param points
     * Points to be rendered
     */
    private void renderHeatmap(List<GeoPoint> points) {
        for (GeoPoint point : points) {
            // Render entrants as semi-transparent circles on map
            Polygon circle = new Polygon();
            circle.setPoints(Polygon.pointsAsCircle(point, 100));
            circle.setFillColor(0x44FF0000); // Red seems to be the most distinguishable
            circle.setStrokeColor(0x00000000);
            circle.setStrokeWidth(0f);
            mapView.getOverlays().add(circle);
        }
        mapView.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        requireActivity().findViewById(R.id.nav_bar).setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}