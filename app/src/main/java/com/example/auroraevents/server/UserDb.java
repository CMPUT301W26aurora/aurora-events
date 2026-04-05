package com.example.auroraevents.server;

import android.util.Log;

import com.example.auroraevents.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Singleton class for all Firestore operations on the "Users" collection.
 *
 * Usage:
 *   UserDb.getInstance().addUser(user, success -> { ... }, e -> { ... });
 */
public class UserDb {

    private static final String TAG             = "UserDb";
    private static final String COLLECTION_NAME = "Users";

    private static UserDb instance;
    private final  FirebaseFirestore db;

    // ── Callbacks ──────────────────────────────────────────────────────────

    public interface OnSuccessCallback  { void onSuccess(); }
    public interface OnFailureCallback  { void onFailure(Exception e); }
    public interface OnUserFetchedCallback { void onFetched(User user); }
    public interface OnUserListFetchedCallback { void onFetched(List<User> users); }

    // ── Singleton ──────────────────────────────────────────────────────────

    private UserDb() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized UserDb getInstance() {
        if (instance == null) {
            instance = new UserDb();
        }
        return instance;
    }

    public interface OnUsersLoadedListener {
        void onUsersUpdate(List<User> participants);
        void onError(Exception e);
    }

    // ── CREATE ─────────────────────────────────────────────────────────────

    /**
     * Adds a new user document to Firestore.
     * The user's deviceId is used as the Firestore document ID.
     * If a document with that ID already exists it will be overwritten.
     *
     * @param user      The User object to persist.
     * @param onSuccess Called when the write succeeds.
     * @param onFailure Called with the exception if the write fails.
     */
    public void addUser(User user, OnSuccessCallback onSuccess, OnFailureCallback onFailure) {
        if (user.getDeviceId() == null || user.getDeviceId().isEmpty()) {
            onFailure.onFailure(new IllegalArgumentException("User deviceId must not be null or empty"));
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(user.getDeviceId())
                .set(user)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "User added: " + user.getDeviceId());
                    onSuccess.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add user: " + user.getDeviceId(), e);
                    onFailure.onFailure(e);
                });
    }

    // ── READ ───────────────────────────────────────────────────────────────

    /**
     * Fetches a single user by their deviceId.
     *
     * @param deviceId  The document ID to fetch.
     * @param onFetched Called with the User object, or null if not found.
     * @param onFailure Called with the exception if the read fails.
     */
    public void getUser(String deviceId, OnUserFetchedCallback onFetched, OnFailureCallback onFailure) {
        db.collection(COLLECTION_NAME)
                .document(deviceId)
                .get()
                .addOnSuccessListener((DocumentSnapshot snapshot) -> {
                    if (snapshot.exists()) {
                        User user = snapshot.toObject(User.class);
                        onFetched.onFetched(user);
                    } else {
                        Log.d(TAG, "No user found for deviceId: " + deviceId);
                        onFetched.onFetched(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch user: " + deviceId, e);
                    onFailure.onFailure(e);
                });
    }

    /**
     * Fetches all users in the collection.
     *
     * @param onFetched Called with the list of all User objects.
     * @param onFailure Called with the exception if the read fails.
     */
    public void getAllUsers(OnUserListFetchedCallback onFetched, OnFailureCallback onFailure) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = querySnapshot.toObjects(User.class);
                    onFetched.onFetched(users);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch all users", e);
                    onFailure.onFailure(e);
                });
    }

    /**
     * Fetches all users with a specific role (e.g. User.ROLE_ORGANIZER).
     *
     * @param role      The role string to filter by.
     * @param onFetched Called with the matching User list.
     * @param onFailure Called with the exception if the read fails.
     */
    public void getUsersByRole(String role, OnUserListFetchedCallback onFetched, OnFailureCallback onFailure) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("role", role)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = querySnapshot.toObjects(User.class);
                    onFetched.onFetched(users);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch users by role: " + role, e);
                    onFailure.onFailure(e);
                });
    }

    /**
     * Fetches a set of users from a list of ids
     *
     * @param ids       A list of user ids to be fetched
     * @param onFetched Called with matching user list
     * @param onFailure Called with exception on failure
     */
    public void fetchUsersByIds(List<String> ids, OnUserListFetchedCallback onFetched, OnFailureCallback onFailure) {
        if (ids == null || ids.isEmpty()) {
            onFetched.onFetched(new ArrayList<>());
            return;
        }
        db.collection("users")
                .whereIn(FieldPath.documentId(), ids)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = querySnapshot.toObjects(User.class);
                    onFetched.onFetched(users);
                })
                .addOnFailureListener(e -> {
                    Log.e("UserDb", "Batch fetch failed", e);
                    onFailure.onFailure(e);
                });
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────

    /**
     * Updates an existing user document.
     * Uses SetOptions.merge() so only provided fields are overwritten.
     *
     * @param user      The User object with updated values. deviceId must be set.
     * @param onSuccess Called when the update succeeds.
     * @param onFailure Called with the exception if the update fails.
     */
    public void updateUser(User user, OnSuccessCallback onSuccess, OnFailureCallback onFailure) {
        if (user.getDeviceId() == null || user.getDeviceId().isEmpty()) {
            onFailure.onFailure(new IllegalArgumentException("User deviceId must not be null or empty"));
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(user.getDeviceId())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "User updated: " + user.getDeviceId());
                    onSuccess.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update user: " + user.getDeviceId(), e);
                    onFailure.onFailure(e);
                });
    }

    /**
     * Adds a notification message to the user's notification history list.
     *
     * @param deviceId     The device ID of the user to update.
     * @param notification The notification string to append.
     * @param onSuccess    Called when the update succeeds.
     * @param onFailure    Called with the exception if the update fails.
     */
    public void addNotificationToUser(String deviceId, String notification,
                                      OnSuccessCallback onSuccess, OnFailureCallback onFailure) {
        db.collection(COLLECTION_NAME)
                .document(deviceId)
                .update("notificationHistory",
                        com.google.firebase.firestore.FieldValue.arrayUnion(notification))
                .addOnSuccessListener(unused -> onSuccess.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add notification for user: " + deviceId, e);
                    onFailure.onFailure(e);
                });
    }

    // ── DELETE ─────────────────────────────────────────────────────────────

    //https://firebase.google.com/docs/functions/1st-gen/auth-events
    //https://firebase.google.com/docs/firestore/manage-data/transactions
    //https://firebase.google.com/docs/functions/firestore-events
    //https://firebase.google.com/docs/firestore/manage-data/add-data
    //https://cloud.google.com/functions
    //Before you ask, I only kinda hate myself

    /**
     * Deletes a user document from Firestore.
     *
     * @param deviceId  The device ID of the user to delete.
     * @param onSuccess Called when the deletion succeeds.
     * @param onFailure Called with the exception if the deletion fails.
     */
    public void deleteUser(String deviceId, OnSuccessCallback onSuccess, OnFailureCallback onFailure) {
        db.collection(COLLECTION_NAME)
                .document(deviceId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "User deleted: " + deviceId);
                    onSuccess.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete user: " + deviceId, e);
                    onFailure.onFailure(e);
                });
    }

    //-- Listener -------------------------------------------------------------------------------------

    /**
     * A batch user listener for grabbing users from a specific event
     * WhereIn caps doc grabs at 30, so a thread safe loop is needed
     * to keep the event capacity from being low.
     *
     * @param allIds the list of ids to listen to
     * @param listener the callback listener
     * @return A list of Listener references
     */
    public void listenToParticipants(List<String> allIds, OnUsersLoadedListener listener) {

        if (allIds == null || allIds.isEmpty()) {
            listener.onUsersUpdate(new ArrayList<>());
            return ;
        }


        List<Task<QuerySnapshot>> snaps = new ArrayList<>();


        int batch = 30;
        int totalBatches = (int) Math.ceil((double) allIds.size() / batch);
        //https://firebase.google.com/docs/firestore/query-data/queries
        //https://oneuptime.com/blog/post/2026-02-17-how-to-set-up-real-time-listeners-for-live-data-updates-in-firestore/view

        for(int i = 0; i < allIds.size(); i= i + batch ){
            int end = Math.min(i+batch, allIds.size());
            List<String> sub = allIds.subList(i, end);
            snaps.add(db.collection(COLLECTION_NAME)
                    .whereIn(FieldPath.documentId(), sub)
                    .get());
        }

        Tasks.whenAllSuccess(snaps).addOnSuccessListener(results->{
            List<User> list = new ArrayList<>();
            for (Object result: results){
                QuerySnapshot querySnapshot = (QuerySnapshot) result;
                for (DocumentSnapshot doc : querySnapshot){
                    list.add(doc.toObject(User.class));
                }
            }
            listener.onUsersUpdate(list);

        }).addOnFailureListener(e->{
            Log.e(TAG, "Failed to batch load user", e);
            listener.onError(e);
        });



    }
}
