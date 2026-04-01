package com.example.auroraevents.view;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.auroraevents.R;
import com.example.auroraevents.model.Notification;
import com.example.auroraevents.model.NotificationArrayAdapter;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private static final String TAG = "NotificationList";

    private ListView                 listView;
    private NotificationArrayAdapter adapter;
    private ArrayList<Notification>  notifications;
    private UserViewModel            userViewModel;
    private User                     user;

    private Button btnOptIn;
    private Button btnOptOut;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (btnOptIn != null && btnOptOut != null) {
                            updateToggleButtons(isGranted);
                        }
                    }
            );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notifications = new ArrayList<>();
        adapter       = new NotificationArrayAdapter(requireContext(), notifications);
        listView      = view.findViewById(R.id.notifications_list);

        // Add header
        View header = LayoutInflater.from(requireContext())
                .inflate(R.layout.header_notification_list, listView, false);
        listView.addHeaderView(header, null, false);
        listView.setAdapter(adapter);

        // Opt In / Opt Out buttons
        btnOptIn  = view.findViewById(R.id.btn_opt_in);
        btnOptOut = view.findViewById(R.id.btn_opt_out);
        updateToggleButtons(areNotificationsEnabled());
        btnOptIn.setOnClickListener(v -> requestOptIn());
        btnOptOut.setOnClickListener(v -> openNotificationSettings());

        // Tap a notification to open its event
        listView.setOnItemClickListener((parent, v, position, id) -> {
            Notification selected = notifications.get(position - 1);
            if (selected.getEventId() == null) return;

            Bundle args = new Bundle();
            args.putString("eventId", selected.getEventId());

            InfoUEventFragment infoUEventFragment = new InfoUEventFragment();
            infoUEventFragment.setArguments(args);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, infoUEventFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Load user then fetch notifications — avoids null user crash
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        userViewModel.getSelectedItem().observe(getViewLifecycleOwner(), u -> {
            if (u != null) {
                user = u;
                loadNotifications(user.getDeviceId());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (btnOptIn != null && btnOptOut != null) {
            updateToggleButtons(areNotificationsEnabled());
        }
    }

    private boolean areNotificationsEnabled() {
        NotificationManager nm =
                (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return nm.areNotificationsEnabled();
        }
        return true;
    }

    private void updateToggleButtons(boolean notificationsEnabled) {
        if (notificationsEnabled) {
            btnOptIn.setVisibility(View.GONE);
            btnOptOut.setVisibility(View.VISIBLE);
        } else {
            btnOptIn.setVisibility(View.VISIBLE);
            btnOptOut.setVisibility(View.GONE);
        }
    }

    private void requestOptIn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // Already granted
                updateToggleButtons(true);
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // User denied once but didn't check "don't ask again" — show dialog
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                // Either first time, OR user checked "don't ask again" — send to settings
                openNotificationSettings();
            }
        } else {
            openNotificationSettings();
        }
    }

    private void openNotificationSettings() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", requireContext().getPackageName(), null));
        }
        startActivity(intent);
    }

    private void loadNotifications(String deviceId) {
        FirebaseFirestore.getInstance()
                .collection("Notifications")
                .whereEqualTo("deviceId", deviceId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> notificationHistory = new ArrayList<>(snapshot.size());
                    notifications.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Notification n = doc.toObject(Notification.class);
                        n.setNotificationId(doc.getId());
                        notifications.add(n);
                        notificationHistory.add(doc.getId());
                    }
                    adapter.notifyDataSetChanged();
                    user.setNotificationHistory(notificationHistory);
                    userViewModel.selectItem(user);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load notifications", e));
    }
}