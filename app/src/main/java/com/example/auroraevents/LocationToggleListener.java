package com.example.auroraevents;

/**
 * A listener for location Toggle
 * @author Arron Rossa
 */
public interface LocationToggleListener {
    void onLocationToggle(boolean isEnabled);
    void onLocationPermissionResult(boolean granted);
}
