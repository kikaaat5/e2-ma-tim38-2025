package com.example.mobileapplication.data.repository;
import com.google.firebase.firestore.FieldValue;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllianceRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private String getUid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
    }

    // 🏰 Kreiraj savez
    public void createAlliance(String name, List<String> invitedFriends) {
        String leaderId = getUid();
        if (leaderId.isEmpty()) return;

        DocumentReference allianceRef = db.collection("alliances").document();
        Map<String, Object> data = new HashMap<>();
        data.put("id", allianceRef.getId());
        data.put("name", name);
        data.put("leaderId", leaderId);
        data.put("members", Arrays.asList(leaderId));
        data.put("missionActive", false);

        db.runTransaction(trx -> {
            trx.set(allianceRef, data);

            // ažuriraj lidera
            trx.update(db.collection("users").document(leaderId),
                    "allianceId", allianceRef.getId());

            // pozovi prijatelje
            for (String friendUid : invitedFriends) {
                Map<String, Object> notif = new HashMap<>();
                notif.put("type", "ALLIANCE_INVITE");
                notif.put("fromUser", leaderId);
                notif.put("toUser", friendUid);
                notif.put("allianceId", allianceRef.getId());
                notif.put("message", "Pozvani ste u savez '" + name + "'");
                notif.put("status", "PENDING");
                trx.set(db.collection("notifications").document(), notif);
            }
            return null;
        });
    }

    // ✅ Prihvati poziv
    public void acceptInvite(String allianceId) {
        String uid = getUid();
        db.collection("alliances").document(allianceId)
                .update("members", FieldValue.arrayUnion(uid));

        db.collection("users").document(uid)
                .update("allianceId", allianceId);
    }

    // ❌ Odbij poziv
    public void rejectInvite(String notifId) {
        db.collection("notifications").document(notifId)
                .update("status", "REJECTED");
    }
}
