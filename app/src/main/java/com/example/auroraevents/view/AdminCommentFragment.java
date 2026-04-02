package com.example.auroraevents.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class AdminCommentFragment extends Fragment {
    private final static String TAG = "AdminCommentFragment";
    private String userId;
    private Boolean isAdmin;
    private CommentAdapter adapter;
    private UserViewModel userViewModel;
    private User user;
    private com.google.firebase.firestore.ListenerRegistration commentListenerRegistrationAll;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_comment, container, false);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        isAdmin = false;
        setUp(view);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        //https://developer.android.com/guide/fragments/lifecycle
        userViewModel.getSelectedItem().observe(getViewLifecycleOwner(), u -> {
            user = u;

            isAdmin = user.getAdmin();
            adapter.setIsAdmin(isAdmin);
            adapter.notifyDataSetChanged();

        });

        commentListenerRegistrationAll = CommentDb.getInstance().commentListenerAll( comments ->{
            if (comments != null) {
                adapter.setComments(comments);
            }
        }, e -> {
            Log.d(TAG, "Error fetching comments" + e); //log errors if any
        });

    }

    /**
     * helper function to setup view
     * @param view the view to setup the ui on
     */
    private void setUp(View view){
        //initialize RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_comments_admin);

        //initialize commentAdapter
        adapter = new CommentAdapter(new ArrayList<>(), new CommentAdapter.OnCommentInteractionListener() {
            @Override
            public void onReplyClicked(Comment comment) {/*do nothing here*/}
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
        }, userId, isAdmin, null, true);
        //set recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);


    }

    /**
     * Deletes listener on view destruction
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (commentListenerRegistrationAll != null) {
            commentListenerRegistrationAll.remove();
            commentListenerRegistrationAll = null;
        }
    }
}
