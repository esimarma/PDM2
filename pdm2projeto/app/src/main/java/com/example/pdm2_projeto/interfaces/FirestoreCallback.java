package com.example.pdm2_projeto.interfaces;

/**
 * Generic interface for handling Firestore asynchronous operations.
 * Provides callback methods for success and failure scenarios.
 *
 * @param <T> The type of data expected on success.
 */
public interface FirestoreCallback<T> {

    /**
     * Called when the Firestore operation is successful.
     *
     * @param result The result of the Firestore operation.
     */
    void onSuccess(T result);

    /**
     * Called when the Firestore operation fails.
     *
     * @param e The exception describing the failure.
     */
    void onFailure(Exception e);
}
