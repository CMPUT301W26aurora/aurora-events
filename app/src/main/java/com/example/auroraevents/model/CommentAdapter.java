package com.example.auroraevents.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.RecyclerView;

import com.example.auroraevents.R;

import java.util.List;

/**
 * CommentAdapter to format comments in the comment fragment for RecyclerView
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    public interface OnCommentInteractionListener {
        void onReplyClicked(Comment comment);
        void onDeleteClicked(Comment comment);
    }
    private OnCommentInteractionListener listener;
    private List<Comment> commentList;
    private String currentUserId;
    private Boolean isAdmin, inAdmin, viewAll;
    private String eventOrganizerId;

    /**
     * Constructor for the comment adapter
     *
     * @param list the list to adapt
     * @param listener a listener for live updates
     */
    public CommentAdapter(List<Comment> list, OnCommentInteractionListener listener, String currentUserId,
                          Boolean isAdmin,String eventOrganizerId, Boolean inAdmin, Boolean viewAll) {
        this.listener = listener;
        this.commentList = list;
        this.currentUserId = currentUserId;
        this.isAdmin = isAdmin;
        this.eventOrganizerId = eventOrganizerId;
        this.inAdmin = inAdmin;
        this.viewAll = viewAll;
    }

    /**
     * ViewHolder for The recycler View
     */
    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        //resource used: https://developer.android.com/develop/ui/views/layout/recyclerview
        private final TextView comment_username;
        private  final TextView comment_body;
        private  final TextView comment_timestamp;
        private  final TextView comment_reply_button;
        private final TextView comment_delete_button;
        private final View rootLayout;
        private final View threadLine;
        private final Guideline guideline;

        /**
         * Constructor for the comment view holder
         * @param itemView the item in reference
         */
        public CommentViewHolder(@NonNull View itemView) {

            super(itemView);

            rootLayout = itemView;
            comment_username = itemView.findViewById(R.id.comment_username);
            comment_body = itemView.findViewById(R.id.comment_body);
            comment_timestamp = itemView.findViewById(R.id.comment_timestamp);
            comment_reply_button = itemView.findViewById(R.id.comment_button_reply);
            threadLine = itemView.findViewById(R.id.thread_line);
            guideline = itemView.findViewById(R.id.guideline_start);
            comment_delete_button = itemView.findViewById(R.id.comment_button_delete);

        }

        /**
         * Handles the logic for formatting the comments in the list
         * @param position position of the comment in the list
         * @param commentList the whole list of comments
         * @param comment the comment in reference
         * @param listener for live updates
         */
        public void bind(int position, List<Comment> commentList, Comment comment, OnCommentInteractionListener listener,
                         String currentUserId, Boolean isAdmin, String eventOrganizerId, Boolean inAdmin, Boolean viewAll) {
            //sets fields
            comment_username.setText(comment.getUsername());
            comment_body.setText(comment.getComment());

            //time posted
            long time = comment.getTimeStampLong();
            CharSequence relativeTime = android.text.format.DateUtils.getRelativeTimeSpanString(
                    time, System.currentTimeMillis(), android.text.format.DateUtils.MINUTE_IN_MILLIS);
            comment_timestamp.setText(relativeTime);

            //reply button, only t1 reply depth
            comment_reply_button.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReplyClicked(comment);
                }
            });

            boolean isCommentOwner = currentUserId != null && currentUserId.equals(comment.getUserId());
            boolean isEventOrganizer = currentUserId != null && currentUserId.equals(eventOrganizerId);

            if (isCommentOwner || isEventOrganizer || (isAdmin && inAdmin)) {
                comment_delete_button.setVisibility(View.VISIBLE);
                comment_delete_button.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteClicked(comment);
                    }
                });
            } else {
                comment_delete_button.setVisibility(View.GONE);
            }


            //checks if a comment has replies
            boolean hasReplyFollowing = (position + 1 < commentList.size()) && comment.getId().equals(commentList.get(position + 1).getParentId());

            //accounts for different resolutions
            float density = itemView.getContext().getResources().getDisplayMetrics().density;

            //edits the parameters of the guideline in xml
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guideline.getLayoutParams();

            //edits parameters of the RecyclerView item
            RecyclerView.LayoutParams rowParams = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            if (comment.getParentId() != null && !comment.getParentId().isEmpty()) {
                //if comment is a reply
                threadLine.setVisibility(View.VISIBLE);
                params.guideBegin = (int) (32 * density);
                comment_reply_button.setVisibility(View.GONE);
                rowParams.topMargin = 0;
                rowParams.bottomMargin = 0;
                rowParams.leftMargin = (int) (32 * density);
                rowParams.rightMargin = (int) (12 * density);
            } else {

                //if comment is a parent
                threadLine.setVisibility(View.GONE);
                rowParams.leftMargin = (int) (12 * density);
                rowParams.rightMargin = (int) (12 * density);
                params.guideBegin = (int) (8 * density);
                rowParams.topMargin = (int) (12 * density);
                //checks if it has replies and adjusts accordingly
                rowParams.bottomMargin = hasReplyFollowing ? 0 : (int) (12 * density);
                comment_reply_button.setVisibility(inAdmin && !viewAll ? View.GONE : View.VISIBLE);
            }

            //set parameters
            guideline.setLayoutParams(params);
            itemView.setLayoutParams(rowParams);
        }
    }

    /**
     * Inflates comment item layout and creates ViewHolder
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new comment ViewHolder containing the layout
     */
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    /**
     * Binds data to viewHolder and contains UI logic
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(position, commentList, commentList.get(position), listener, currentUserId, isAdmin, eventOrganizerId, inAdmin, viewAll);
    }

    /**
     * @return Total number of items in list, or 0 if null
     */
    @Override
    public int getItemCount() {
        return commentList == null ? 0 : commentList.size();
    }

    /**
     * Live updates the comments on new post
     * @param newList the new list to be updated
     */
    public void setComments(List<Comment> newList) {
        this.commentList = newList;
        notifyDataSetChanged();
    }

    public void setIsAdmin(Boolean isAdmin){
        this.isAdmin = isAdmin;
    }
    public void setInAdmin(Boolean inAdmin){
        this.inAdmin = inAdmin;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setEventOrganizerId(String eventOrganizerId) {
        this.eventOrganizerId = eventOrganizerId;
    }
}
