package com.example.auroraevents.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

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
    private RegistrationList registrationList;

    UserAdapter(OnUserInteractionListener listener, List<User> user, Boolean inAdmin, RegistrationList registrationList){
        this.listener = listener;
        this.userList = user;
        this.inAdmin = inAdmin;
        this.registrationList = registrationList;

    }
    public abstract static class UserViewHolder extends RecyclerView.ViewHolder {
        UserViewHolder(@NonNull View itemView){
            super(itemView);
        }
        public abstract void bind(User user, OnUserInteractionListener listener, RegistrationList registrationList);
    }
    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user, listener, registrationList);
    }
    public static class  OrgViewHolder extends UserViewHolder{

        private final View rootLayout;
        private final ImageButton delete;
        private final TextView userName;
        private final TextView userStatus;
        OrgViewHolder(View v) {
            super(v);

            rootLayout =v;
            delete = rootLayout.findViewById(R.id.delete_user_button_org_item);
            userName = rootLayout.findViewById(R.id.user_name_list_org);
            userStatus = rootLayout.findViewById(R.id.user_status_list_org);
        }
        @Override
        public void bind(User user, OnUserInteractionListener listener, RegistrationList registrationList) {

        }
    }

    public static class  AdminViewHolder extends UserViewHolder{
        AdminViewHolder(View v) {
            super(v);
        }

        @Override
        public void bind(User user, OnUserInteractionListener listener, RegistrationList registrationList) {

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