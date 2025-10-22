package com.example.mobileapplication.domain.serviceImpl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AllianceInviteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String allianceId = intent.getStringExtra("allianceId");
        String notifId = intent.getStringExtra("notifId");
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (uid == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if ("ACCEPT_INVITE".equals(action)) {
            db.collection("alliances").document(allianceId)
                    .update("members", com.google.firebase.firestore.FieldValue.arrayUnion(uid));
            db.collection("users").document(uid)
                    .update("allianceId", allianceId);
            db.collection("notifications").document(notifId)
                    .update("status", "ACCEPTED");

            Toast.makeText(context, "✅ Prihvatio si poziv u savez!", Toast.LENGTH_SHORT).show();

        } else if ("REJECT_INVITE".equals(action)) {
            db.collection("notifications").document(notifId)
                    .update("status", "REJECTED");

            Toast.makeText(context, "❌ Odbio si poziv u savez.", Toast.LENGTH_SHORT).show();
        }
    }
}
