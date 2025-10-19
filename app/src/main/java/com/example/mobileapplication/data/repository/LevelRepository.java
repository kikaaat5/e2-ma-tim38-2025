package com.example.mobileapplication.data.repository;

import android.util.Log;
import com.example.mobileapplication.data.models.User;
import com.example.mobileapplication.domain.serviceImpl.LevelManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class LevelRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();


    public void addXpAndCheckLevelUp(int earnedXp) {
        if (auth.getCurrentUser() == null) {
            Log.w("LevelRepository", "⚠️ Nema ulogovanog korisnika, XP nije dodat.");
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        DocumentReference ref = db.collection("users").document(uid);

        ref.get().addOnSuccessListener(snapshot -> {
            User user;
            if (snapshot.exists()) {
                user = snapshot.toObject(User.class);
            } else {
                user = new User(uid); // novi korisnik ako ga još nema
            }

            int oldLevel = user.getLevel();
            user.setXp(user.getXp() + earnedXp);

            // ✅ Provera da li prelazi nivo
            LevelManager.checkLevelUp(user);

            // 🔥 Ako se nivo promenio, možemo dodati neki bonus / obaveštenje
            if (user.getLevel() > oldLevel) {
                Log.d("LevelRepository", "🎉 Korisnik prešao nivo " + oldLevel + " → " + user.getLevel());
            }

            // 🔄 Ažuriranje korisnika u Firestore
            ref.set(user)
                    .addOnSuccessListener(aVoid ->
                            Log.d("LevelRepository", "✅ XP ažuriran: +" + earnedXp + " (nivo " + user.getLevel() + ")"))
                    .addOnFailureListener(e ->
                            Log.e("LevelRepository", "❌ Greška pri ažuriranju: " + e.getMessage()));
        });
    }
}
