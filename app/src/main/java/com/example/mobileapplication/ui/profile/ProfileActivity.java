package com.example.mobileapplication.ui.profile;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobileapplication.R;
import com.example.mobileapplication.data.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ImageView ivAvatar, ivQrCode;
    private TextView tvUsername, tvLevel, tvTitle, tvXP, tvPP, tvCoins, tvBadges, tvEquipment;
    private Button btnChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ivAvatar = findViewById(R.id.ivAvatar);
        ivQrCode = findViewById(R.id.ivQrCode);
        tvUsername = findViewById(R.id.tvUsername);
        tvLevel = findViewById(R.id.tvLevel);
        tvTitle = findViewById(R.id.tvTitle);
        tvXP = findViewById(R.id.tvXP);
        tvPP = findViewById(R.id.tvPP);
        tvCoins = findViewById(R.id.tvCoins);
        tvBadges = findViewById(R.id.tvBadges);
        tvEquipment = findViewById(R.id.tvEquipment);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        btnChangePassword.setOnClickListener(v -> new ChangePasswordDialog(this).show());

        loadUserData();
    }

    private void loadUserData() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get().addOnSuccessListener(document -> {
            if (document.exists()) {
                User user = document.toObject(User.class);
                if (user != null) {
                    tvUsername.setText(user.getUsername());
                    tvLevel.setText("Nivo: " + user.getLevel());
                    tvTitle.setText("Titula: " + user.getTitle());
                    tvXP.setText("XP: " + user.getXp());
                    tvPP.setText("PP: " + user.getPp());
                    tvCoins.setText("Novčići: " + user.getCoins());
                    tvBadges.setText("Bedževi: " + user.getBadges());
                    tvEquipment.setText("Oprema: " + user.getEquipment());


                    String avatarName = user.getAvatar();


                    if (avatarName == null || avatarName.trim().isEmpty()) {
                        avatarName = "avatar1"; // ili "default_avatar" ako tako imenuješ u res/drawable
                    }


                    int resId = getResources().getIdentifier(avatarName, "drawable", getPackageName());


                    ivAvatar.setImageResource(resId == 0 ? R.drawable.avatar1 : resId);

                    generateQrCode(uid);
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("Profile", " Greška pri učitavanju profila: " + e.getMessage());
            Toast.makeText(this, "Greška pri učitavanju profila", Toast.LENGTH_SHORT).show();
        });
    }

    private void generateQrCode(String uid) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            int size = 512;
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(uid, BarcodeFormat.QR_CODE, size, size);
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            ivQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
