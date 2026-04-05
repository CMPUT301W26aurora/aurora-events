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
 * User adapter class for displaying users
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    public interface OnUserInteractionListener{
        void Onclick(User user);
    }
    private UserAdapter.OnUserInteractionListener listener;
    private List<UserAdapterWrapper> userList;
    private Boolean inAdmin;


    public UserAdapter(List<UserAdapterWrapper> user, Boolean inAdmin, OnUserInteractionListener listener){
        this.listener = listener;
        this.userList = user;
        this.inAdmin = inAdmin;

    }
    public abstract static class UserViewHolder extends RecyclerView.ViewHolder {
        UserViewHolder(@NonNull View itemView){
            super(itemView);
        }
        public abstract void bind(UserAdapterWrapper user, OnUserInteractionListener listener);
    }
    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, int position) {
        UserAdapterWrapper user = userList.get(position);
        holder.bind(user, listener);
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
        public void bind(UserAdapterWrapper user, OnUserInteractionListener listener) {
            userName.setText(user.getUser().getName());
            userStatus.setText(user.getStatus());
            if(!user.getStatus().equals("selected")){
                delete.setVisibility(View.GONE);
            }
            delete.setOnClickListener(v->{
                if(listener != null){
                    listener.Onclick(user.getUser());
                }
            });
        }
    }

    public static class  AdminViewHolder extends UserViewHolder{
        AdminViewHolder(View v) {
            super(v);
        }

        @Override
        public void bind(UserAdapterWrapper user, OnUserInteractionListener listener) {

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

