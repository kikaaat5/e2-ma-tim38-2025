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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public String getCurrentUserUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void createAlliance(String name, String leaderUid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> alliance = new HashMap<>();
        alliance.put("name", name);
        alliance.put("leaderUid", leaderUid);
        alliance.put("members", List.of(leaderUid));
        alliance.put("missionStarted", false);

        db.collection("alliances").add(alliance).addOnSuccessListener(docRef -> {
            // 🔹 kad se kreira savez, pozovi prijatelje
            sendInvitesToFriends(name, leaderUid, docRef.getId());
        });
    }

    private void sendInvitesToFriends(String allianceName, String senderUid, String allianceId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<String> friendIds = friendIdsLiveValue(); // uzmi prijatelje iz LiveData

        if (friendIds == null || friendIds.isEmpty()) return;

        for (String fid : friendIds) {
            Map<String, Object> invite = new HashMap<>();
            invite.put("senderUid", senderUid);
            invite.put("allianceName", allianceName);
            invite.put("allianceId", allianceId);
            invite.put("status", "pending");

            db.collection("notifications")
                    .document(fid)
                    .collection("invites")
                    .document(allianceId)
                    .set(invite);
        }
    }

    // helper
    private List<String> friendIdsLiveValue() {
        return friendIds.getValue() != null ? new ArrayList<>(friendIds.getValue()) : new ArrayList<>();
    }
}
