package com.example.auroraevents.view;

import static android.widget.Toast.LENGTH_LONG;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.auroraevents.R;
import com.example.auroraevents.server.EventDb;
import com.google.firebase.firestore.ListenerRegistration;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class CameraFragment extends Fragment {
    private final String TAG = "CameraFragment";
    private ListenerRegistration eventSnapshotListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {

                if(result.getContents() != null) {
                    String qr = result.getContents();

                    eventSnapshotListener = EventDb.getInstance().addSnapshotListenerForEvent(
                            qr,
                            event -> {
                                if (event != null) {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("eventId", qr);

                                    InfoUEventFragment infoUEventFragment = new InfoUEventFragment();
                                    infoUEventFragment.setArguments(bundle);

                                    getParentFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.fragment_container, infoUEventFragment)
                                            .addToBackStack(null)
                                            .commit();
                                } else {
                                    Log.d(TAG, "No such event exists");
                                    Toast.makeText(requireContext(), "Invalid QR code Scanned", LENGTH_LONG).show();
                                }
                            },
                            e -> {
                                Log.d(TAG, "Error fetching event:" + e);
                            }
                    );
                }
            });
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        Button scan = view.findViewById(R.id.scan);

        scan.setOnClickListener(v -> {

            //https://github.com/journeyapps/zxing-android-embedded has documentation
            // on how to use scan options by zxing team on github

            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan QR Code");
            options.setBeepEnabled(false);
            options.setOrientationLocked(false);
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);

            barcodeLauncher.launch(options);
        });

        return view;
    }
    @Override
    public void onStop() {
        super.onStop();

        if (eventSnapshotListener != null) {
            eventSnapshotListener.remove();
            eventSnapshotListener = null;
        }
    }
}