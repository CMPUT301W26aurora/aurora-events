package com.example.auroraevents.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.auroraevents.R;
import com.google.firebase.firestore.ListenerRegistration;

public class AdminProfileFragment extends Fragment {
    ListenerRegistration listenerRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profiles, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(listenerRegistration != null){
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        View navBar = getActivity().findViewById(R.id.nav_bar_admin);
        if (navBar != null) {
            navBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        View nav = getActivity().findViewById(R.id.nav_bar_admin);
        if (nav != null) {
            nav.setVisibility(View.GONE);
        }
    }
}
