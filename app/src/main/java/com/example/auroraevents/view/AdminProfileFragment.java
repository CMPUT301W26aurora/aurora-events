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
import com.example.auroraevents.model.Event;
import com.example.auroraevents.model.Organizer;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserAdapter;
import com.example.auroraevents.model.UserAdapterWrapper;
import com.example.auroraevents.server.UserDb;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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
        CheckBox filterButton = view.findViewById(R.id.filter_organizers_checkbox);
        filterButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applyFilter(isChecked);
        });

        adapter = new UserAdapter(new ArrayList<>(), true, new UserAdapter.OnUserInteractionListener() {
            @Override
            public void Onclick(User user) {
                if(user.getRole().equals(User.ROLE_ENTRANT)){
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle("Delete User?")
                            .setMessage("Are you sure you want to permanently delete " + user.getName() + "? This action cannot be undone.")
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .setPositiveButton("Delete", (dialog, which) -> {
                                UserDb.getInstance().deleteUser(user.getDeviceId(), ()->{
                                    Log.d(TAG, "Succesfully Deleted User");
                                    Toast.makeText(getContext(),"User is deleted", Toast.LENGTH_SHORT).show();
                                },e->{
                                    Log.e(TAG, "Firebase error",e);
                                    Toast.makeText(getContext(),"Error deleting user, try again", Toast.LENGTH_SHORT).show();;
                                });
                            })
                            .show();
                }else{
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle("Delete Organizer?")
                            .setMessage("Are you sure you want to permanently delete " + user.getName() + "? This action cannot be undone and will delete all their events")
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .setPositiveButton("Delete", (dialog, which) -> {
                                UserDb.getInstance().deleteUser(user.getDeviceId(), ()->{
                                    Log.d(TAG, "Succesfully Deleted User");
                                    Toast.makeText(getContext(),"User is deleted", Toast.LENGTH_SHORT).show();
                                },e->{
                                    Log.e(TAG, "Firebase error",e);
                                    Toast.makeText(getContext(),"Error deleting user, try again", Toast.LENGTH_SHORT).show();;
                                });
                            })
                            .show();
                }
            }
            @Override
            public void OnNotify(User user) {
                Bundle args = new Bundle();
                args.putString(OrganizerNotificationFragment.ARG_ORGANIZER_ID,    user.getDeviceId());
                args.putString(OrganizerNotificationFragment.ARG_ORGANIZER_NAME,  user.getName());
                args.putString(OrganizerNotificationFragment.ARG_ORGANIZER_EMAIL, user.getEmail());

                OrganizerNotificationFragment fragment = new OrganizerNotificationFragment();
                fragment.setArguments(args);

                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
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
    /**
     *
     * @param onlyOrganizers
     */
    private void applyFilter(boolean onlyOrganizers) {
        if (masterUserList == null) return;

        List<User> filteredUsers = new ArrayList<>();
        for (User u : masterUserList) {
            if (!onlyOrganizers || u.getRole().equals(User.ROLE_ORGANIZER)) {
                filteredUsers.add(u);
            }
        }
        if (filteredUsers.isEmpty()) {
            adapter.setUserList(new ArrayList<>());
            return;
        }
        List<UserAdapterWrapper> displayList = new ArrayList<>();
        AtomicInteger count = new AtomicInteger(0);
        for (User u : filteredUsers) {
            List<String> eventIds = getAllEventIds(u);
            Map<String, String> localNameMap = new HashMap<>();
            grabEventNamesForUser(eventIds, localNameMap, () -> {
                displayList.add(new UserAdapterWrapper(u, "", null,u.getEventsSigned() , eventIds, localNameMap));
                if (count.incrementAndGet() == filteredUsers.size()) {
                    adapter.setUserList(displayList);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    /**
     *
     * @param eventIds
     * @param nameMap
     * @param onComplete
     */
    public void grabEventNamesForUser(List<String> eventIds, Map<String, String> nameMap, Runnable onComplete) {
        if (eventIds == null || eventIds.isEmpty()) {
            onComplete.run();
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (int i = 0; i < eventIds.size(); i += 30) {
            List<String> chunk = eventIds.subList(i, Math.min(i + 30, eventIds.size()));

            db.collection("Events")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (DocumentSnapshot doc : querySnapshot) {
                            nameMap.put(doc.getId(), doc.getString("name"));
                        }
                        if (nameMap.size() >= eventIds.size() || querySnapshot.size() < 30) {
                            onComplete.run();
                        }
                    });
        }
    }
    /**
     *
     * @param user
     * @return
     */
    public List<String> getAllEventIds(User user) {
        Map<String, String> signedMap = user.getEventsSigned();
        List<String> ids = new ArrayList<>(signedMap.keySet());
        if (user instanceof Organizer) {
            Organizer org = (Organizer) user;
            if (org.getMyEvents() != null) {
                for (Event e : org.getMyEvents()) {
                    ids.add(e.getEventId());
                }
            }
        }
        return ids;
    }
}

