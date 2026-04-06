package com.example.auroraevents.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * A user ViewModel that listens for updates to a user from FireBase
 * @author Arron Rossa
 * @author Sean Ross
 * @author Joshua Terry
 */

public class UserViewModel extends ViewModel {
    //https://developer.android.com/topic/libraries/architecture/viewmodel
    //https://developer.android.com/topic/libraries/architecture/livedata
    //https://firebase.google.com/docs/firestore/query-data/listen
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> adminModeActive = new MutableLiveData<>(false);
    private final MutableLiveData<User> selectedItem = new MutableLiveData<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration userListener;
    public void startUserListener(String deviceId) {
        if (userListener != null) return;
        userListener = db.collection("Users").document(deviceId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;
                    User user = snapshot.toObject(User.class);
                    currentUser.setValue(user);
                });
    }
    public LiveData<User> getCurrentUser() { return currentUser; }
    public LiveData<Boolean> getAdminModeActive() { return adminModeActive; }
    public void toggleAdminMode() {
        Boolean current = adminModeActive.getValue();
        adminModeActive.setValue(current != null && !current);
    }

    public void selectItem(User user) { selectedItem.setValue(user); }
    public LiveData<User> getSelectedItem() { return selectedItem; }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (userListener != null) userListener.remove();
    }
}
