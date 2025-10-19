package com.example.mobileapplication.ui.profile;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mobileapplication.R;
import com.example.mobileapplication.data.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LevelActivity extends AppCompatActivity {

    private TextView tvTitle, tvLevel, tvXP, tvPP;
    private ProgressBar progressXp;
    private FirebaseAuth auth;
    private FirebaseFirestore db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        tvTitle = findViewById(R.id.tvTitle);
        tvLevel = findViewById(R.id.tvLevel);
        tvXP = findViewById(R.id.tvXP);
        tvPP = findViewById(R.id.tvPP);
        progressXp = findViewById(R.id.progressXp);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadUserData();
    }

    private void loadUserData() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    tvTitle.setText("Titula: " + user.getTitle());
                    tvLevel.setText("Nivo: " + user.getLevel());
                    tvXP.setText("XP: " + user.getXp() + " / " + user.getNextLevelXp());
                    tvPP.setText("PP: " + user.getPp());
                    progressXp.setMax(user.getNextLevelXp());
                    progressXp.setProgress(user.getXp());
                }
            }
        });
    }
}
