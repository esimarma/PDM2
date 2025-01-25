package com.example.pdm2_projeto;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TESTE Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> user = new HashMap<>();
        user.put("name", "João");
        user.put("email", "joao@example.com");
        user.put("age", 25);

        db.collection("users")
                .add(user)
                .addOnSuccessListener(documentReference ->
                        Log.d("Firestore", "Documento criado com ID: " + documentReference.getId())
                )
                .addOnFailureListener(e ->
                        Log.w("Firestore", "Erro ao criar documento", e)
                );
    }
}