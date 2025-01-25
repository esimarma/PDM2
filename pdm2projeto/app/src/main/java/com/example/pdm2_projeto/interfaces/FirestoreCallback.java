package com.example.pdm2_projeto.interfaces;

public interface FirestoreCallback<T> {
    void onSuccess(T result); // Método chamado em caso de sucesso
    void onFailure(Exception e); // Método chamado em caso de falha
}
