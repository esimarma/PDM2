package com.example.pdm2_projeto.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pdm2_projeto.R;
import com.example.pdm2_projeto.models.Comment;
import com.google.firebase.Timestamp;
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

        // Convert Firestore Timestamp to a readable date format
        Timestamp timestamp = comment.getCreatedAt();
        if (timestamp != null) {
            String formattedDate = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    .format(timestamp.toDate()); // Convert timestamp to Date
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
        TextView commentText, commentDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            commentText = itemView.findViewById(R.id.comment_text);
            commentDate = itemView.findViewById(R.id.comment_date);
        }
    }
}
