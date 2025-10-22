package com.example.mobileapplication.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class FriendsRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private String getUid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
    }

    public Task<DocumentSnapshot> getFriends() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return FirebaseFirestore.getInstance()
                .collection("friends")   // ✅ koristimo kolekciju friends
                .document(uid)
                .get();
    }
    // 🔹 Pretraži druge korisnike po username-u
    public Task<QuerySnapshot> searchUsers(String query) {
        return db.collection("users")
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .get();
    }

    // 🔹 Dodaj novog prijatelja (obostrano)
    public void addFriend(String friendUid) {
        String uid = getUid();
        if (uid.isEmpty()) return;

        db.collection("friends").document(uid)
                .update("list", FieldValue.arrayUnion(friendUid))
                .addOnFailureListener(e ->
                        db.collection("friends").document(uid)
                                .set(new java.util.HashMap<String, Object>() {{
                                    put("list", java.util.List.of(friendUid));
                                }})
                );

        // 🔁 obostrano prijateljstvo
        db.collection("friends").document(friendUid)
                .update("list", FieldValue.arrayUnion(uid))
                .addOnFailureListener(e ->
                        db.collection("friends").document(friendUid)
                                .set(new java.util.HashMap<String, Object>() {{
                                    put("list", java.util.List.of(uid));
                                }})
                );
    }
}
