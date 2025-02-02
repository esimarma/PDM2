package com.example.pdm2_projeto.repositories;

import android.util.Log;
import com.example.pdm2_projeto.interfaces.FirestoreCallback;
import com.example.pdm2_projeto.models.Comment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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
                    queryDocumentSnapshots.forEach(document -> {
                        Comment comment = document.toObject(Comment.class);
                        comment.setId(document.getId()); // Assign Firestore document ID
                        comments.add(comment);
                    });
                    callback.onSuccess(comments);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
