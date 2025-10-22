package com.example.mobileapplication.domain.serviceImpl;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mobileapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

public class NotificationService {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final Context context;

    public NotificationService(Context context) {
        this.context = context;
    }

    public void listenForAllianceInvites() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        db.collection("notifications")
                .whereEqualTo("toUser", uid)
                .whereEqualTo("status", "PENDING")
                .addSnapshotListener((snap, e) -> {
                    if (snap == null) return;

                    for (DocumentChange dc : snap.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String message = dc.getDocument().getString("message");
                            String allianceId = dc.getDocument().getString("allianceId");
                            String notifId = dc.getDocument().getId();
                            showLocalNotification(message, allianceId, notifId);
                        }
                    }
                });
    }

    private void showLocalNotification(String message, String allianceId, String notifId) {
        String channelId = "alliance_invites";
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);

        // 🔹 Kreiranje kanala (samo jednom)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Pozivi u savez",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        // ✅ Dozvola za notifikacije (Android 13+)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return; // ako nema dozvole, prekidamo
        }

        // 🔹 Intent za "Prihvati"
        Intent acceptIntent = new Intent(context, AllianceInviteReceiver.class);
        acceptIntent.setAction("ACCEPT_INVITE");
        acceptIntent.putExtra("allianceId", allianceId);
        acceptIntent.putExtra("notifId", notifId);

        // 🔹 Intent za "Odbij"
        Intent rejectIntent = new Intent(context, AllianceInviteReceiver.class);
        rejectIntent.setAction("REJECT_INVITE");
        rejectIntent.putExtra("notifId", notifId);

        PendingIntent acceptPending = PendingIntent.getBroadcast(
                context,
                (int) System.currentTimeMillis(),
                acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        PendingIntent rejectPending = PendingIntent.getBroadcast(
                context,
                (int) (System.currentTimeMillis() + 1),
                rejectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Poziv u savez")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_check, "Prihvati", acceptPending)
                .addAction(R.drawable.ic_close, "Odbij", rejectPending);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
