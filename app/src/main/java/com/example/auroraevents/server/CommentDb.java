package com.example.auroraevents.server;

import com.example.auroraevents.model.Comment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CommentDb {
    private static final String TAG             = "CommentDb";
    private static final String COLLECTION_NAME = "Comments";
    private static CommentDb instance;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnSuccessCallback       { void onSuccess(); }
    public interface OnFailureCallback       { void onFailure(Exception e); }
    public interface OnCommentCreatedCallback { void onCreated(String commentId); }
    public interface OnCommentsFetchedCallback { void onFetched(List<Comment> comments); }

    private CommentDb() {}

    public static synchronized CommentDb getInstance() {
        if (instance == null) {
            instance = new CommentDb();
        }
        return instance;
    }

    public void postComment(Comment comment, OnCommentCreatedCallback callback, OnFailureCallback onFailure){

    }
}
