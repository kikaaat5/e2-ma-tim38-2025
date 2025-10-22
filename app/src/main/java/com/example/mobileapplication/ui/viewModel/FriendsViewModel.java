package com.example.mobileapplication.ui.friends;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mobileapplication.data.repository.FriendsRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FriendsViewModel extends ViewModel {
    private final FriendsRepository repo = new FriendsRepository();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // 🔹 Lista svih prijatelja kao Firestore dokumenti
    public MutableLiveData<List<DocumentSnapshot>> friendDocs = new MutableLiveData<>(new ArrayList<>());

    // 🔹 Prethodna lista UID-eva (ako ti treba interno)
    public MutableLiveData<List<String>> friendIds = new MutableLiveData<>(new ArrayList<>());

    public void loadFriends() {
        repo.getFriends().addOnSuccessListener(doc -> {
            if (doc.exists() && doc.contains("list")) {
                List<String> ids = (List<String>) doc.get("list");
                friendIds.setValue(ids);

                if (ids.isEmpty()) {
                    friendDocs.setValue(new ArrayList<>());
                    return;
                }

                // 🔹 Učitaj dokumente korisnika iz kolekcije "users" po njihovim ID-jevima
                db.collection("users")
                        .whereIn(FieldPath.documentId(), ids)
                        .get()
                        .addOnSuccessListener(qs -> friendDocs.setValue(qs.getDocuments()))
                        .addOnFailureListener(e ->
                                Log.e("FriendsVM", "❌ Greška pri učitavanju prijatelja: " + e.getMessage()));
            } else {
                friendIds.setValue(new ArrayList<>());
                friendDocs.setValue(new ArrayList<>());
            }
        });
    }

    public void addFriend(String uid) {
        repo.addFriend(uid);
        loadFriends();
    }

    public void searchUsers(String query, MutableLiveData<List<DocumentSnapshot>> results) {
        repo.searchUsers(query).addOnSuccessListener(qs -> results.setValue(qs.getDocuments()));
    }
}
