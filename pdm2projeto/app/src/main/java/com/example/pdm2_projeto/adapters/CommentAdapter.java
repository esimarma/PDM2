package com.example.pdm2_projeto.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pdm2_projeto.R;
import com.example.pdm2_projeto.interfaces.FirestoreCallback;
import com.example.pdm2_projeto.models.Comment;
import com.example.pdm2_projeto.models.User;
import com.example.pdm2_projeto.repositories.UsersRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter class for displaying a list of comments in a RecyclerView.
 * Handles user comments, including displaying author details, timestamps, and deletion options.
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private Context context; // Context for accessing resources and UI components
    private List<Comment> commentsList; // List of comments to be displayed
    private OnCommentDeleteListener deleteListener; // Listener for handling comment deletions

    /**
     * Interface for handling comment deletion events.
     */
    public interface OnCommentDeleteListener {
        void onDeleteComment(Comment comment);
    }

    /**
     * Constructor for initializing the adapter with necessary dependencies.
     *
     * @param context        Application context.
     * @param comments       List of comments to be displayed.
     * @param deleteListener Listener for handling comment deletions.
     */
    public CommentAdapter(Context context, List<Comment> comments, OnCommentDeleteListener deleteListener) {
        this.context = context;
        this.commentsList = comments;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the comment item layout
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the comment object for the current position
        Comment comment = commentsList.get(position);
        holder.commentText.setText(comment.getComment()); // Set comment text

        // Fetch and display user details
        UsersRepository usersRepository = new UsersRepository();
        usersRepository.getUserById(comment.getUserId(), new FirestoreCallback<User>() {
            @Override
            public void onSuccess(User user) {
                holder.commentAuthor.setText(user.getName()); // Display user name

                // Load user profile image using Glide
                if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                    Glide.with(context)
                            .load(user.getProfilePictureUrl())
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .circleCrop()
                            .into(holder.commentProfileImage);
                } else {
                    holder.commentProfileImage.setImageResource(R.drawable.ic_profile);
                }
            }

            @Override
            public void onFailure(Exception e) {
                holder.commentProfileImage.setImageResource(R.drawable.ic_profile); // Default profile image on error
            }
        });

        // Convert Firestore timestamp to a formatted date
        Timestamp timestamp = comment.getCreatedAt();
        if (timestamp != null) {
            String formattedDate = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    .format(timestamp.toDate());
            holder.commentDate.setText(formattedDate); // Display formatted date
        } else {
            holder.commentDate.setText("Unknown Date"); // Handle missing timestamps
        }

        // Check if the logged-in user is the author of the comment
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        // Display delete button only if the logged-in user is the author of the comment
        if (currentUserId != null && currentUserId.equals(comment.getUserId())) {
            holder.btnDeleteComment.setVisibility(View.VISIBLE);
            holder.btnDeleteComment.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteComment(comment); // Trigger delete event
                }
            });
        } else {
            holder.btnDeleteComment.setVisibility(View.GONE); // Hide delete button for other users
        }
    }

    @Override
    public int getItemCount() {
        return commentsList.size(); // Return total number of comments in the list
    }

    /**
     * ViewHolder class to hold UI components for each comment item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView commentText, commentDate, commentAuthor;
        ImageView commentProfileImage, btnDeleteComment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            commentText = itemView.findViewById(R.id.comment_text); // Comment text field
            commentDate = itemView.findViewById(R.id.comment_date); // Comment date field
            commentAuthor = itemView.findViewById(R.id.comment_author); // Comment author field
            commentProfileImage = itemView.findViewById(R.id.profile_image); // Profile image view
            btnDeleteComment = itemView.findViewById(R.id.btn_delete_comment); // Delete button
        }
    }
}

