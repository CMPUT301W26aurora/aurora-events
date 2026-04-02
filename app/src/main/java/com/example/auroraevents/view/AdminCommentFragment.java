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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.auroraevents.R;
import com.example.auroraevents.model.Comment;
import com.example.auroraevents.model.CommentAdapter;
import com.example.auroraevents.server.CommentDb;

import java.util.ArrayList;

public class AdminCommentFragment extends Fragment {
    private final static String TAG = "AdminCommentFragment";
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_comment, container, false);
        userId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);


        return view;
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
}
