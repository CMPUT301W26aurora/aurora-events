package com.example.auroraevents.model;

import android.location.Location;
public class RadiusUtil {
    /**
     * Returns true if entrant is within radius of event location
     * @author Arron Rossa
     */
    public static boolean isWithinRadius(
            double eventLat, double eventLng,
            double entrantLat, double entrantLng,
            float radiusMeters) {

        float[] results = new float[1];
        Location.distanceBetween(eventLat, eventLng, entrantLat, entrantLng, results);
        return results[0] <= radiusMeters;
    }
}
