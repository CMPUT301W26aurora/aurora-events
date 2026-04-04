package com.example.auroraevents.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.auroraevents.R;
import com.example.auroraevents.view.RemoveUserPopUpDialog;
import com.example.auroraevents.view.UserListFragment;

import java.util.List;

/**
 *
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    public interface OnUserInteractionListener{
        void Onclick(User user);
    }
    private UserAdapter.OnUserInteractionListener listener;
    private List<User> userList;
    private Boolean inAdmin;
    UserAdapter(OnUserInteractionListener listener, List<User> user, Boolean inAdmin){
        this.listener = listener;
        this.userList = user;
        this.inAdmin = inAdmin;
    }
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private View rootLayout;
        UserViewHolder(@NonNull View itemView){
            super(itemView);

            rootLayout = itemView;
        }
        public void bind(User user, OnUserInteractionListener listener){

        }
    }
    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {return userList == null ? 0 : userList.size();}

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item, parent, false);
        return new UserViewHolder(view);
    }
    @Override
    public int getItemViewType(int position) {
        return inAdmin ? 1 : 0;
    }
}