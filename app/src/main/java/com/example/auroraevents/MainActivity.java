package com.example.auroraevents;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {

    String deviceId;

    public static void signIn(Context context) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Boolean> status = new AtomicReference<>(true);

        FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener(result -> latch.countDown())
                .addOnFailureListener(e -> {
                    Log.e("TEST", "signIn failed", e);
                    status.set(false);
                    latch.countDown();
                });

        try {
            assert latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Toast.makeText(context, R.string.database_error_toast_text, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        signIn(this);

        /* Check if user already in database */
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Toast.makeText(this, deviceId, Toast.LENGTH_SHORT).show();
        Log.d("debug", "started");
        CountDownLatch getLatch = new CountDownLatch(1);
        AtomicReference<Boolean> getStatus = new AtomicReference<>(true);
        UserDb.getInstance().getUser(deviceId,
                u -> getLatch.countDown(),
                e -> {
                    getStatus.set(false);
                    getLatch.countDown();
                }
        );
        try {
            assert getLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Toast.makeText(this, R.string.database_error_toast_text, Toast.LENGTH_LONG).show();
        }

        if (getStatus.get()) {
            Toast.makeText(this, "GO TO EVENT LIST", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "ENTER STUFF", Toast.LENGTH_LONG).show();
        }

        Button confirmButton = findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(view ->
                Toast.makeText(view.getContext(), "it does the button!", Toast.LENGTH_SHORT).show()
        );

//        /* Get user input */
//        Button confirmButton = findViewById(R.id.confirm_button);
//        confirmButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                TextView nameField = findViewById(R.id.user_name);
//                String name = nameField.getText().toString();
//                TextView emailField = findViewById(R.id.user_email);
//                String email = emailField.getText().toString();
//                TextView phoneNumberField = findViewById(R.id.user_phone_number);
//                String phoneNumber = phoneNumberField.getText().toString();
//                if ((name.isEmpty()) || (email.isEmpty())) {
//                    Toast.makeText(view.getContext(), R.string.mandatory_fields_toast_text, Toast.LENGTH_LONG).show();
//                } else {
//                    User user = new User(deviceId, name, email, phoneNumber, User.ROLE_ENTRANT);
//                    // add to database
//                    CountDownLatch setLatch = new CountDownLatch(1);
//                    AtomicReference<Boolean> setStatus = new AtomicReference<>(true);
//                    UserDb.getInstance().addUser(user,
//                            setLatch::countDown,
//                            e -> {
//                                setStatus.set(false);
//                                setLatch.countDown();
//                            }
//                    );
//                    try {
//                        assert setLatch.await(10, TimeUnit.SECONDS);
//                    } catch (InterruptedException e) {
//                        Toast.makeText(view.getContext(), R.string.database_error_toast_text, Toast.LENGTH_LONG).show();
//                    }
//
//                    if (setStatus.get()) {
//                        Toast.makeText(view.getContext(), "GO TO EVENT LIST", Toast.LENGTH_LONG).show();
//                    } else {
//                        Toast.makeText(view.getContext(), R.string.database_error_toast_text, Toast.LENGTH_LONG).show();
//                    }
//                }
//            }
//        });
    }
}