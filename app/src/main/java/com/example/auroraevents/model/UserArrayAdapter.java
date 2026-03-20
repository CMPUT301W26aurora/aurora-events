package com.example.auroraevents.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.auroraevents.R;

import java.util.ArrayList;

public class UserArrayAdapter extends ArrayAdapter<User> {
    public UserArrayAdapter(Context context, ArrayList<User> users) {
        super(context, 0, users);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertview, @NonNull ViewGroup parent) {
        View view;
        if (convertview == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.user_list_item, parent, false);
        } else {
            view = convertview;
        }
        User user = getItem(position);
        TextView userName = view.findViewById(R.id.user_name);
        TextView userEmail = view.findViewById(R.id.user_email);
        TextView userPhoneNumber = view.findViewById(R.id.user_phone_number);

        if (user != null) {
            userName.setText(user.getName());
            if (user.getEmail() != null) {
                userEmail.setText(user.getEmail());
            }
            if (user.getPhoneNumber() != null) {
                userPhoneNumber.setText(user.getPhoneNumber());
            }
        }
        return view;
    }
}

