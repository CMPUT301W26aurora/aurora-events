package com.example.auroraevents;

public interface LocationToggleListener {
    void onLocationToggle(boolean isEnabled);
    void onLocationPermissionResult(boolean granted);
}
