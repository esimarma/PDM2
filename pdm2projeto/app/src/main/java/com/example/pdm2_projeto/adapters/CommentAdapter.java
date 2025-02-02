package com.example.pdm2_projeto.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pdm2_projeto.R;
import com.example.pdm2_projeto.interfaces.FirestoreCallback;
import com.example.pdm2_projeto.models.Comment;
import com.example.pdm2_projeto.models.User;
import com.example.pdm2_projeto.repositories.UsersRepository;
import com.google.firebase.Timestamp;

import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private Context context;
    private List<Comment> commentsList;

    public CommentAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.commentsList = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = commentsList.get(position);
        holder.commentText.setText(comment.getComment());

        UsersRepository usersRepository = new UsersRepository();
        usersRepository.getUserById(comment.getUserId(), new FirestoreCallback<User>() {
            @Override
            public void onSuccess(User user) {
                holder.commentAuthor.setText(user.getName());

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
                holder.commentProfileImage.setImageResource(R.drawable.ic_profile);
            }
        });

        // Convert Firestore Timestamp to a readable date format
        Timestamp timestamp = comment.getCreatedAt();
        if (timestamp != null) {
            String formattedDate = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    .format(timestamp.toDate());
            holder.commentDate.setText(formattedDate);
        } else {
            holder.commentDate.setText("Unknown Date");
        }
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView commentText, commentDate, commentAuthor;
        ImageView commentProfileImage; // Adicione essa linha

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            commentText = itemView.findViewById(R.id.comment_text);
            commentDate = itemView.findViewById(R.id.comment_date);
            commentAuthor = itemView.findViewById(R.id.comment_author);
            commentProfileImage = itemView.findViewById(R.id.profile_image);
        }
    }
}
