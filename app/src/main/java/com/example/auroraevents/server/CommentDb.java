package com.example.auroraevents.server;

import android.util.Log;

import com.example.auroraevents.model.Comment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
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

    // ── CREATE ─────────────────────────────────────────────────────────────

    /**
     * Adds a new comment document to Firestore with an auto-generated ID.
     * After creation, the generated ID is written back into comment.Id.
     * Also adds a timestamp to the comment based on server time written into
     * comment.Timestamp as a firebase object.
     * 
     * @param comment     The comment object to persist.
     * @param callback Called with the new auto-generated document ID.
     * @param onFailure Called with the exception if the write fails.
     */
    public void postComment(Comment comment, OnCommentCreatedCallback callback, OnFailureCallback onFailure){
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();

        comment.setId(docRef.getId());
        comment.setTimestamp(FieldValue.serverTimestamp());

        docRef.set(comment)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Comment Created" + docRef.getId());
                    callback.onCreated(docRef.getId());
                })
                .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to create comment", e);
                onFailure.onFailure(e);
                });
    }
}
