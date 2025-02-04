package com.example.pdm2_projeto.repositories;

import com.example.pdm2_projeto.interfaces.FirestoreCallback;
import com.example.pdm2_projeto.models.Comment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing comments in Firestore.
 * Provides methods to add, retrieve, and delete comments from the Firestore database.
 */
public class CommentsRepository {

    /**
     * Reference to the Firestore collection where comments are stored.
     */
    private final CollectionReference commentsCollection;

    /**
     * Constructor that initializes the Firestore database and sets the reference to the "comments" collection.
     */
    public CommentsRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.commentsCollection = db.collection("comments");
    }

    /**
     * Adds a new comment to Firestore.
     *
     * @param comment  The Comment object to be added.
     * @param callback Callback to handle success or failure of the operation.
     */
    public void addComment(Comment comment, FirestoreCallback<Void> callback) {
        commentsCollection.add(comment)
                .addOnSuccessListener(documentReference -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Retrieves all comments associated with a specific location.
     *
     * @param locationId The ID of the location for which comments are to be fetched.
     * @param callback   Callback to handle the retrieved list of comments or failure.
     */
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
                    // Sort comments by creation timestamp in descending order (latest first)
                    comments.sort((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()));
                    callback.onSuccess(comments);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Deletes a comment from Firestore.
     *
     * @param comment  The Comment object to be deleted.
     * @param callback Callback to handle success or failure of the operation.
     */
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
