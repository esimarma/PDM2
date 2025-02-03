package com.example.pdm2_projeto.repositories;

import android.util.Log;
import com.example.pdm2_projeto.interfaces.FirestoreCallback;
import com.example.pdm2_projeto.models.Comment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CommentsRepository {
    private final CollectionReference commentsCollection;

    public CommentsRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.commentsCollection = db.collection("comments");
    }

    // Add comment to Firestore with Timestamp
    public void addComment(Comment comment, FirestoreCallback<Void> callback) {
        commentsCollection.add(comment)
                .addOnSuccessListener(documentReference -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    // Get comments for a specific location
    public void getCommentsByLocation(String locationId, FirestoreCallback<List<Comment>> callback) {
        commentsCollection.whereEqualTo("locationId", locationId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comment> comments = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Comment comment = document.toObject(Comment.class);
                        comment.setId(document.getId()); // Assign Firestore document ID
                        comments.add(comment);
                    }
                    comments.sort((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt())); // Sort by latest
                    callback.onSuccess(comments);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // Delete a comment from Firestore
    public void deleteComment(Comment comment, FirestoreCallback<Void> callback) {
        if (comment.getId() == null || comment.getId().isEmpty()) {
            callback.onFailure(new Exception("Comment ID is missing"));
            return;
        }

        commentsCollection.document(comment.getId())
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}
