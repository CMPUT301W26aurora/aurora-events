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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User adapter class for displaying users
 * Has two subclasses for specific views
 * @author Sean Ross
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

    /**
     * To be used by the organizer
     */
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

    /**
     * To be used by the Admin
     */
    public static class  AdminViewHolder extends UserViewHolder{
        private final View rootLayout;
        private final ImageButton delete, notify;
        private final TextView userName, userEmail, userPhone, userJoined, userHosted, acctime;
        AdminViewHolder(View v) {
            super(v);
            rootLayout =v;
            delete = rootLayout.findViewById(R.id.delete_user_button_admin_item);
            notify = rootLayout.findViewById(R.id.show_notif_button_admin_item);
            userName = rootLayout.findViewById(R.id.user_name_list_admin);
            userEmail = rootLayout.findViewById(R.id.user_email_list_admin);
            userPhone = rootLayout.findViewById(R.id.user_phone_list_admin);
            userJoined = rootLayout.findViewById(R.id.user_event_list_in_admin);
            userHosted = rootLayout.findViewById(R.id.user_event_list_own_admin);
            acctime = rootLayout.findViewById(R.id.user_account);
        }
        @Override
        public void bind(UserAdapterWrapper user, OnUserInteractionListener listener) {
            String name =user.getUser().getName();
            String email = user.getUser().getEmail();
            String phone = user.getUser().getPhoneNumber();
            String accAge = user.getUser().getFormattedAccountAge();
            String joined = formatJoined(user.getEventDataList(), user.getLookup());


            userHosted.setText("Loading");

            notify.setVisibility(View.GONE);
            userName.setText(name);
            userEmail.setText(email);

            userJoined.setText(joined);
            acctime.setText(accAge);
            if(phone != null && !phone.isEmpty()){
                userPhone.setText(phone);
            }else{
                userPhone.setVisibility(View.GONE);
            }
            if(user.getUser().getRole().equals(User.ROLE_ORGANIZER)){
                notify.setVisibility(VISIBLE);
                grabEventsFormat(user.getUser().getDeviceId(), userHosted);
                notify.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.OnNotify(user.getUser());
                    }
                });
            }

            delete.setOnClickListener(v->{
                if(listener!=null){
                    listener.Onclick(user.getUser());
                }
            });
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

    //https://www.geeksforgeeks.org/java/stringbuilder-class-in-java-with-examples/

    /**
     * A helper function that returns a string of joined events and their status in said event
     * @param statuses A map of event ids and a correlated status for a given user
     * @param lookup A map of a event ids and its correlated event name
     * @return A {@link String} to be used in formatting
     * @author Sean Ross
     */
    private static String formatJoined(Map<String, String> statuses, Map<String, String> lookup) {
        if (statuses == null || statuses.isEmpty()) return "No events joined.";
        StringBuilder sb = new StringBuilder();
        sb.append("Joined:").append("\n");
        for (Map.Entry<String, String> entry : statuses.entrySet()) {
            String eventId = entry.getKey();
            String status  = entry.getValue();
            String title   = lookup.getOrDefault(eventId, "Unknown Event");
            sb.append(title).append(": ").append(status).append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * A helper function that returns a string of created events
     * @param userId the user who is being queried
     * @param targetTextView The TextView to be updated
     * @author Sean Ross
     */
    private static void grabEventsFormat(String userId, TextView targetTextView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Events")
                .whereEqualTo("organizerDeviceId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        targetTextView.setText("No events hosted.");
                        return;
                    }
                    StringBuilder sb = new StringBuilder("Hosting:\n");
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        if(doc==null)continue;
                        String eventName = doc.getString("name");
                        if(eventName==null)continue;
                        sb.append(eventName).append("\n");
                    }
                    targetTextView.setText(sb.toString().trim());
                })
                .addOnFailureListener(e -> {
                    targetTextView.setText("Error loading hosted events.");
                });
    }

}