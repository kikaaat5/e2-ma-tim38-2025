package com.example.mobileapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobileapplication.R;
import com.example.mobileapplication.data.models.User;
import com.example.mobileapplication.ui.auth.LoginActivity;
import com.example.mobileapplication.ui.equipment.EquipmentInventoryActivity;
import com.example.mobileapplication.ui.equipment.StoreActivity;
import com.example.mobileapplication.ui.profile.ProfileActivity;
import com.example.mobileapplication.ui.profile.StatisticsActivity;
import com.example.mobileapplication.ui.tasks.TaskListActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private TextView tvUsername, tvTitle, tvLevel, tvXp, tvPp, tvLogout, tvStore, tvEquipment;
    private ProgressBar progressXp;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ivAvatar = findViewById(R.id.ivAvatar);
        tvUsername = findViewById(R.id.tvUsername);
        tvTitle = findViewById(R.id.tvTitle);
        tvLevel = findViewById(R.id.tvLevel);
        tvXp = findViewById(R.id.tvXp);
        tvPp = findViewById(R.id.tvPp);
        progressXp = findViewById(R.id.progressXp);
        tvLogout = findViewById(R.id.tvLogout);
        tvStore = findViewById(R.id.btnStore);
        tvEquipment = findViewById(R.id.btnInventory);

        findViewById(R.id.btnTasks).setOnClickListener(v ->
                startActivity(new Intent(this, TaskListActivity.class)));
        findViewById(R.id.btnProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
        findViewById(R.id.btnStats).setOnClickListener(v ->
                startActivity(new Intent(this, StatisticsActivity.class)));
        findViewById(R.id.btnStore).setOnClickListener(v ->
                startActivity(new Intent(this, StoreActivity.class)));
        findViewById(R.id.btnInventory).setOnClickListener(v ->
                startActivity(new Intent(this, EquipmentInventoryActivity.class)));


        tvLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        loadUserData();
    }

    private void loadUserData() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        DocumentReference ref = db.collection("users").document(uid);
        ref.addSnapshotListener((snapshot, e) -> {
            if (e != null || snapshot == null || !snapshot.exists()) return;

            User user = snapshot.toObject(User.class);
            if (user == null) return;

            tvUsername.setText(user.getUsername() != null ? user.getUsername() : "Igrač");
            tvTitle.setText(user.getTitle() != null ? user.getTitle() : "Novajlija");
            tvLevel.setText("Lvl " + user.getLevel());
            tvXp.setText(user.getXp() + " / " + user.getNextLevelXp() + " XP");
            tvPp.setText("PP: " + user.getPp());

            progressXp.setMax(user.getNextLevelXp());
            progressXp.setProgress(user.getXp());

           if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                int resId = getResources().getIdentifier(
                        user.getAvatar(),  // npr. "avatar2"
                        "drawable",
                        getPackageName()
                );
                if (resId != 0) {
                    ivAvatar.setImageResource(resId);
                } else {
                    ivAvatar.setImageResource(R.drawable.avatar1); // fallback
                }
            } else {
                ivAvatar.setImageResource(R.drawable.avatar1);
            }
        });
    }
}
