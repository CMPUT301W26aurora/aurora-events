import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.auroraevents.R;
import com.example.auroraevents.model.Comment;
import com.example.auroraevents.model.CommentAdapter;
import com.example.auroraevents.model.User;
import com.example.auroraevents.model.UserViewModel;
import com.example.auroraevents.server.CommentDb;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class CommentFragment extends Fragment {
    private Comment selectedParentComment = null;
    private String eventId;
    private String userId;
    private User user;
    private String userName;
    private Boolean isAdmin;
    private String eventOrganizerId;
    private CommentAdapter adapter;
    private UserViewModel userViewModel;
    private com.google.firebase.firestore.ListenerRegistration commentListenerRegistration;
    private final String TAG = "CommentFragment";

    /**
     * Fragment logic for the commentFragment
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the view to be inflated
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @androidx.annotation.Nullable ViewGroup container, @androidx.annotation.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment, container, false);

        return view; //return view
    }

    @Override
    public void onViewCreated(@NonNull View view, @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        if (getArguments() != null) { //pass event id in args from infouevent
            eventId = getArguments().getString("eventId");
            eventOrganizerId = getArguments().getString("organizerId");
        }
        userId ="";
        isAdmin = false;
        setUp(view);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        userViewModel.getSelectedItem().observe(getViewLifecycleOwner(), u -> {
            //grab userid
            if(u != null){
                user = u;
                userId = u.getDeviceId();

                userName = user.getName(); //set user details
                isAdmin = user.getIsAdmin();
                adapter.setCurrentUserId(userId);
                adapter.setEventOrganizerId(eventOrganizerId);
                adapter.setIsAdmin(isAdmin);
                adapter.notifyDataSetChanged();
            }

        });
        userViewModel.getAdminModeActive().observe(getViewLifecycleOwner(), modeActive -> {
            if( modeActive!= null){
                adapter.setInAdmin(modeActive);
                adapter.notifyDataSetChanged();
            }
        });

        commentListenerRegistration = CommentDb.getInstance().commentListener(eventId, comments -> {
            if (comments != null) {

                adapter.setComments(comments);
            }
        }, e -> {
            Log.d(TAG, "Error fetching comments" + e); //log errors if any
        });
    }
    /**
     * Deletes listener on view destruction
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (commentListenerRegistration != null) {
            commentListenerRegistration.remove();
            commentListenerRegistration = null;
        }
    }

    /**
     * On fragment start, hides toolbar
     */
    @Override
    public void onStart() {
        super.onStart();
        toggleBottomBar(View.GONE);
    }

    /**
     * on fragment end, show toolbar
     */
    @Override
    public void onStop() {
        super.onStop();
        toggleBottomBar(View.VISIBLE);
    }


    /**
     * Sets the visibility of the toolbar
     * @param visibility The visibility value to be used
     */
    private void toggleBottomBar(int visibility) {
        if (getActivity() != null) {
            View navBar = getActivity().findViewById(R.id.nav_bar);
            View adminBar = getActivity().findViewById(R.id.nav_bar_admin);

            Boolean isAdminMode = userViewModel.getAdminModeActive().getValue();

            if (navBar != null && Boolean.FALSE.equals(isAdminMode)) {
                navBar.setVisibility(visibility);
            }else if(adminBar != null && Boolean.TRUE.equals(isAdminMode)) {
                adminBar.setVisibility(visibility);
            }
        }
    }

    private void setUp(View view){
        //initialize RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_comments);

        View replyIndicator = view.findViewById(R.id.reply_indicator);
        TextView replyText = view.findViewById(R.id.text_replying_to);
        View cancelReply = view.findViewById(R.id.button_cancel_reply);
        //initialize commentAdapter
        adapter = new CommentAdapter(new ArrayList<>(), new CommentAdapter.OnCommentInteractionListener() {
            @Override
            public void onReplyClicked(Comment comment) {
                selectedParentComment = comment;
                replyText.setText("Replying to @" + comment.getUsername());
                replyIndicator.setVisibility(View.VISIBLE);
            }
            @Override
            public void onDeleteClicked(Comment comment) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Comment")
                        .setMessage("Are you sure you want to remove this comment?")
                        .setPositiveButton("Delete", (dialog, which) ->
                                CommentDb.getInstance().deleteComment(comment.getId(),
                                        () -> Log.d(TAG, "Deleted successfully"),
                                        e -> Log.e(TAG, "Delete Failed")
                                ))
                        .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss())
                        .create()
                        .show();

            }
        }, userId, isAdmin, eventOrganizerId, false, true);
        cancelReply.setOnClickListener(v -> {
            selectedParentComment = null;
            replyIndicator.setVisibility(View.GONE);
        });

        //set recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        //back button
        view.findViewById(R.id.back_button).setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );

        //post button logic
        view.findViewById(R.id.button_post).setOnClickListener(v -> {
            EditText editComment = view.findViewById(R.id.edit_text_comment);
            String text = editComment.getText().toString().trim();

            if (text.isEmpty() || userName == null) return;
            String parentId = (selectedParentComment != null) ? selectedParentComment.getId() : null;

            Comment newComment = new Comment(text, userId, parentId, null, eventId, null, userName);

            CommentDb.getInstance().postComment(newComment, id -> {
                editComment.setText("");
                selectedParentComment = null;
                view.findViewById(R.id.reply_indicator).setVisibility(View.GONE);
            }, e -> Log.e(TAG, "Post failed", e));
        });
    }
}
