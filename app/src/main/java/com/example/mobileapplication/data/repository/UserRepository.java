package com.example.mobileapplication.data.repository;

import android.util.Log;

import com.example.mobileapplication.data.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
                        user.setId(uid);

                        db.collection("users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firebase", " Korisnik sačuvan u Firestore");
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
        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser == null) {
            Log.w("UserRepository", "⚠️ Nema aktivnog korisnika (FirebaseAuth je null)");
            return null;
        }

        User user = new User();
        user.setId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
        user.setUsername(firebaseUser.getDisplayName() != null
                ? firebaseUser.getDisplayName()
                : firebaseUser.getEmail());
        user.setAvatar("default_avatar");

        db.collection("users").document(firebaseUser.getUid()).get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (doc.exists()) {
                        if (doc.contains("username")) user.setUsername(doc.getString("username"));
                        if (doc.contains("avatar")) user.setAvatar(doc.getString("avatar"));
                        if (doc.contains("xp")) user.setXp(doc.getLong("xp") != null ? doc.getLong("xp").intValue() : 0);
                        if (doc.contains("level")) user.setLevel(doc.getLong("level") != null ? doc.getLong("level").intValue() : 1);
                    }
                })
                .addOnFailureListener(e -> Log.e("UserRepository", "❌ Greška pri čitanju Firestore usera: " + e.getMessage()));

        return user;
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
