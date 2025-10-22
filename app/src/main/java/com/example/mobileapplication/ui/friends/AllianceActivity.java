package com.example.mobileapplication.ui.friends;


import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mobileapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldPath;

import java.util.List;

public class AllianceActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvAllianceName, tvLeader;
    private LinearLayout membersContainer;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance);

        db = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        tvAllianceName = findViewById(R.id.tvAllianceName);
        tvLeader = findViewById(R.id.tvLeader);
        membersContainer = findViewById(R.id.membersContainer);

        loadAllianceForUser();
    }

    private void loadAllianceForUser() {
        // 🔹 Nađi savez u kojem je trenutni korisnik član
        db.collection("alliances")
                .whereArrayContains("members", currentUid)
                .get()
                .addOnSuccessListener(qs -> {
                    if (qs.isEmpty()) {
                        Toast.makeText(this, "Nisi član nijednog saveza ⚔️", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentSnapshot allianceDoc = qs.getDocuments().get(0);
                    String name = allianceDoc.getString("name");
                    String leaderUid = allianceDoc.getString("leaderUid");
                    List<String> members = (List<String>) allianceDoc.get("members");

                    tvAllianceName.setText("⚔️ Savez: " + name);

                    // 🔹 Prikaz lidera
                    db.collection("users").document(leaderUid).get().addOnSuccessListener(leaderDoc -> {
                        String leaderName = leaderDoc.getString("username");
                        tvLeader.setText("👑 Vođa: " + leaderName);
                    });

                    // 🔹 Prikaz svih članova
                    if (members != null && !members.isEmpty()) {
                        db.collection("users")
                                .whereIn(FieldPath.documentId(), members)
                                .get()
                                .addOnSuccessListener(memberQs -> showMembers(memberQs.getDocuments()));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Greška pri učitavanju saveza", Toast.LENGTH_SHORT).show());
    }

    private void showMembers(List<DocumentSnapshot> docs) {
        membersContainer.removeAllViews();

        for (DocumentSnapshot doc : docs) {
            String username = doc.getString("username");
            String avatar = doc.getString("avatar");
            ImageView iv = new ImageView(this);
            iv.setLayoutParams(new LinearLayout.LayoutParams(120, 120));
            iv.setPadding(8, 8, 8, 8);

            if (avatar != null) {
                int resId = getResources().getIdentifier(avatar, "drawable", getPackageName());
                if (resId != 0) iv.setImageResource(resId);
                else Glide.with(this).load(avatar).into(iv);
            } else {
                iv.setImageResource(R.drawable.avatar1);
            }

            TextView tv = new TextView(this);
            tv.setText(username != null ? username : "(bez imena)");
            tv.setTextSize(16);
            tv.setPadding(10, 0, 0, 10);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.addView(iv);
            row.addView(tv);

            membersContainer.addView(row);
        }
    }
}

