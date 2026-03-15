package com.example.auroraevents.view;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.auroraevents.R;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class CameraFragment extends Fragment {
    private final String TAG = "CameraFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {

                if(result.getContents() != null) {
                    String qr = result.getContents();
                    // put code here
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
}