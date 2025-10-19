package com.example.mobileapplication.data.repository;

import android.util.Log;

import com.example.mobileapplication.data.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public UserRepository() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void registerUser(User user, String password, OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();
                        user.setId(uid); // ako User ima polje id

                        // 🔹 Snimi korisnika u Firestore
                        db.collection("users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firebase", " Korisnik sačuvan u Firestore");
                                    // Pozovi listener TEK nakon uspešnog snimanja
                                    listener.onComplete(task);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firebase", " Greška pri snimanju korisnika: " + e.getMessage());
                                    listener.onComplete(task);
                                });
                    } else {
                        Log.e("Firebase", " Registracija neuspešna: " + task.getException().getMessage());
                        listener.onComplete(task);
                    }
                });
    }


    public void loginUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    public void logoutUser() {
        auth.signOut();
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }


    public User getCurrentUser() {
        // TODO: povezati sa Firebase-om
        return null;
    }

    public void updateUser(User user) {
        if (user.getId() == null) return;

        db.collection("users").document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid ->
                        Log.d("Firebase", " Korisnik uspešno ažuriran"))
                .addOnFailureListener(e ->
                        Log.e("Firebase", " Greška pri ažuriranju korisnika: " + e.getMessage()));
    }

    public void getUserById(String uid, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection("users").document(uid).get().addOnCompleteListener(listener);
    }


}
