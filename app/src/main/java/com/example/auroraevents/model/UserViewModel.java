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
    }
}
