package com.example.auroraevents.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.auroraevents.R;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserAdapter;
import com.example.auroraevents.model.UserAdapterWrapper;
import com.example.auroraevents.server.UserDb;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class AdminProfileFragment extends Fragment {
    ListenerRegistration listenerRegistration;
    private final String TAG = "AdminProfileFragment";
    private List<User> masterUserList;
    private List<User> createDisplayList;
    private UserAdapter adapter;



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
        RecyclerView recyclerView = view.findViewById(R.id.entrants_list_admin);
        ImageButton deleteButton  = view.findViewById(R.id.delete_user_button_admin_item);
        CheckBox filterButton       = view.findViewById(R.id.filter_organizers_checkbox);
        filterButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applyFilter(isChecked);
        });

        adapter = new UserAdapter(new ArrayList<>(), true, new UserAdapter.OnUserInteractionListener() {

            @Override
            public void Onclick(User user) {
                //womp
            }
            @Override
            public void OnNotify(User user) {
                //Insert Notification list code here
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        listenerRegistration = UserDb.getInstance().userSnapshotListener(users -> {
            masterUserList = users;
            applyFilter(filterButton.isChecked());
        }, e -> Log.e(TAG, "Error loading users", e));
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

    private void applyFilter(boolean onlyOrganizers) {
        if (masterUserList == null) return;

        List<UserAdapterWrapper> displayList = new ArrayList<>();
        if (onlyOrganizers) {
            for (User u : masterUserList) {
                if (u.getRole().equals(User.ROLE_ORGANIZER)) {
                    displayList.add(new UserAdapterWrapper(u, "", null));
                }
            }
        } else {
            for (User u: masterUserList){
                displayList.add(new UserAdapterWrapper(u, "", null));
            }
        }
        adapter.setUserList(displayList);
        adapter.notifyDataSetChanged();
    }
}


