package com.example.auroraevents.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.auroraevents.R;

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
    public abstract static class UserViewHolder extends RecyclerView.ViewHolder {
        UserViewHolder(@NonNull View itemView){
            super(itemView);
        }
        public abstract void bind(User user, OnUserInteractionListener listener);
    }
    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user, listener);
    }
    public static class  OrgViewHolder extends UserViewHolder{
        OrgViewHolder(View v) {
            super(v);
        }
        @Override
        public void bind(User user, OnUserInteractionListener listener) {

        }
    }

    public static class  AdminViewHolder extends UserViewHolder{
        AdminViewHolder(View v) {
            super(v);
        }

        @Override
        public void bind(User user, OnUserInteractionListener listener) {

        }
    }

    @Override
    public int getItemCount() {return userList == null ? 0 : userList.size();}

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list_admin, parent, false);
            return new AdminViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list_org, parent, false);
            return new OrgViewHolder(v);
        }
    }
    @Override
    public int getItemViewType(int position) {
        return inAdmin ? 1 : 0;
    }
}