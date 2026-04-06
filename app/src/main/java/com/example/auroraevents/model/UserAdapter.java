package com.example.auroraevents.model;

import static android.view.View.VISIBLE;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.auroraevents.R;

import java.util.Date;
import java.util.List;

/**
 * User adapter class for displaying users
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    public interface OnUserInteractionListener{
        void Onclick(User user);
        void OnNotify(User user);
    }


    private UserAdapter.OnUserInteractionListener listener;
    private List<UserAdapterWrapper> userList;
    private Boolean inAdmin;


    public UserAdapter(List<UserAdapterWrapper> user, Boolean inAdmin, OnUserInteractionListener listener){
        this.listener = listener;
        this.userList = user;
        this.inAdmin = inAdmin;

    }

    public void setUserList(List<UserAdapterWrapper> list){ this.userList=list;}
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
        private final TextView userName, userStatus, userEmail, userPhone, userTime;

        OrgViewHolder(View v) {
            super(v);

            rootLayout =v;
            delete = rootLayout.findViewById(R.id.delete_user_button_org_item);
            userName = rootLayout.findViewById(R.id.user_name_list_org);
            userStatus = rootLayout.findViewById(R.id.user_status_list_org);
            userEmail = rootLayout.findViewById(R.id.user_email_list_org);
            userPhone = rootLayout.findViewById(R.id.user_phone_list_org);
            userTime = rootLayout.findViewById(R.id.invite_duration_list_org);


        }
        @Override
        public void bind(UserAdapterWrapper user, OnUserInteractionListener listener) {
            String name =user.getUser().getName();
            String email = user.getUser().getEmail();
            String phone = user.getUser().getPhoneNumber();


            userName.setText(name);
            userEmail.setText(email);

            if(phone != null && !phone.isEmpty()){
                userPhone.setText(phone);
            }else{
                userPhone.setVisibility(View.GONE);
            }

            if (user.getStatus().equals("Selected")) {
                Date selectedDate = user.getDate();
                if(selectedDate != null){
                    String timeAgo = DateUtils.getRelativeTimeSpanString(selectedDate.getTime(),
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS).toString();
                    userTime.setText("Invited " + timeAgo);
                    delete.setVisibility(VISIBLE);
                }

            }else{
                userTime.setVisibility(View.GONE);
                delete.setVisibility(View.GONE);
            }
            userStatus.setText(user.getStatus());
            delete.setOnClickListener(v->{
                if(listener != null){
                    listener.Onclick(user.getUser());
                }
            });
        }
    }

    public static class  AdminViewHolder extends UserViewHolder{
        private final View rootLayout;
        private final ImageButton delete, notify;
        private final TextView userName, userEmail, userPhone;
        AdminViewHolder(View v) {

            super(v);

            rootLayout =v;
            delete = rootLayout.findViewById(R.id.delete_user_button_admin_item);
            notify = rootLayout.findViewById(R.id.show_notif_button_admin_item);
            userName = rootLayout.findViewById(R.id.user_name_list_admin);
            userEmail = rootLayout.findViewById(R.id.user_email_list_admin);
            userPhone = rootLayout.findViewById(R.id.user_phone_list_admin);

        }
        @Override
        public void bind(UserAdapterWrapper user, OnUserInteractionListener listener) {
            String name =user.getUser().getName();
            String email = user.getUser().getEmail();
            String phone = user.getUser().getPhoneNumber();
            notify.setVisibility(View.GONE);
            userName.setText(name);
            userEmail.setText(email);
            if(phone != null && !phone.isEmpty()){
                userPhone.setText(phone);
            }else{
                userPhone.setVisibility(View.GONE);
            }
            if(user.getUser().getRole().equals(User.ROLE_ORGANIZER)){
                notify.setVisibility(VISIBLE);
            }
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

