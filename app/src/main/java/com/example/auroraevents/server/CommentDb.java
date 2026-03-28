package com.example.auroraevents.server;

import android.util.Log;

import com.example.auroraevents.model.Comment;
import com.example.auroraevents.model.Event;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

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
    //--Read------------------------------------------------------------------------------------

    /**
     * Fetches the comments for a given event in ascending order of timestamps
     * IE most recent first.
     *
     * @param eventId The Event to fetch comments from
     * @param onFetched Called with Comment objects, or null if not found
     * @param onFailure Called with exception if read fails
     */
    public void getCommentsForEvent(String eventId, CommentDb.OnCommentsFetchedCallback onFetched, CommentDb.OnFailureCallback onFailure){
        db.collection(COLLECTION_NAME)
                .whereEqualTo("eventId", eventId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comment> comments = queryDocumentSnapshots.toObjects(Comment.class);

                    Log.d(TAG, "Fetched " + comments.size() + " comments for event: " + eventId);
                    onFetched.onFetched(comments);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch event comments: " + eventId, e);
                    onFailure.onFailure(e);
                });
    }

    /**
     *
     * @param onFetched Called with Fulled list of Comment Objects
     * @param onFailure Called with exception if read fails
     */
    public void getAllComments(CommentDb.OnCommentsFetchedCallback onFetched, CommentDb.OnFailureCallback onFailure){
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Comment> comments = querySnapshot.toObjects(Comment.class);
                    onFetched.onFetched(comments);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch all comments", e);
                    onFailure.onFailure(e);
                });
    }

    //--Delete----------------------------------------------------------------------------------
    /**
     * Deletes an comment document from Firestore and all of its children.
     *
     * @param id   The document ID of the comment to delete.
     * @param onSuccess Called when the deletion succeeds.
     * @param onFailure Called with the exception if the deletion fails.
     */
    public void deleteComment(String id, CommentDb.OnSuccessCallback onSuccess, CommentDb.OnFailureCallback onFailure) {
        //https://firebase.google.com/docs/reference/android/com/google/firebase/firestore/WriteBatch
        db.collection(COLLECTION_NAME)
                .whereEqualTo("parentId", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    //I guess I went a little overboard and thought replies were part of the user stories
                    //the backend is all there so, surprise if your reading this. I'm only nesting one tier
                    //as replies to replies requires more work

                    //batch allows for multiple commands in one database call
                    WriteBatch batch = db.batch();

                    batch.delete(db.collection(COLLECTION_NAME).document(id)); //add parent comment

                    for (QueryDocumentSnapshot doc :queryDocumentSnapshots){
                        batch.delete(doc.getReference());
                    } //iterate over children and add them to the batch

                    //commit the batch delete
                    batch.commit()
                            .addOnSuccessListener(unused -> {
                                Log.d(TAG, "Comment and Replies Deleted: " + id);
                                onSuccess.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to delete comment: " + id, e);
                                onFailure.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete comment: " + id, e);
                    onFailure.onFailure(e);
                });
    }
}
