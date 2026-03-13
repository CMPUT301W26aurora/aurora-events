package com.example.auroraevents.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {
    private final MutableLiveData<User> selectedItem = new MutableLiveData<>();
    public void selectItem(User user) {
        selectedItem.setValue(user);
    }
    public LiveData<User> getSelectedItem() {
        return selectedItem;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Helper class to fetch user data on app startup
 */
public class UserViewModel extends ViewModel {
    private final MutableLiveData<Organizer> organizerLiveData = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<Organizer> getOrganizer() {
        return organizerLiveData;
    }

    public void fetchOrganizer(String deviceId) {
        // Only fetch if we don't already have the data
        if (organizerLiveData.getValue() != null) return;

        db.collection("Users").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Organizer org = documentSnapshot.toObject(Organizer.class);
                        organizerLiveData.setValue(org);
                    }
                });
    }
}
