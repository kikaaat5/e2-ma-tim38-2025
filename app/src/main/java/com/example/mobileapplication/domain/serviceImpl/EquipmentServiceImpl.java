package com.example.mobileapplication.domain.serviceImpl;


import android.content.Context;

import androidx.lifecycle.LiveData;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.mobileapplication.data.AppDatabase;
import com.example.mobileapplication.data.models.EquipmentEntity;
import com.example.mobileapplication.data.models.User;
import com.example.mobileapplication.data.repository.EquipmentRepository;
import com.example.mobileapplication.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.net.UnknownServiceException;
import java.util.List;

public class EquipmentServiceImpl {

    private final EquipmentRepository repo;
    private final UserRepository userRepo;
    private final FirebaseAuth auth;
    private final Context context;


    public EquipmentServiceImpl(Context context) {
        this.context = context;
        this.repo = new EquipmentRepository(AppDatabase.get(context));
        this.userRepo = new UserRepository();
        this.auth = FirebaseAuth.getInstance();
    }

    public LiveData<List<EquipmentEntity>> getUserEquipment() {
        return repo.getUserEquipment();
    }

    public LiveData<List<EquipmentEntity>> getActiveEquipment() {
        return repo.getActiveEquipment();
    }
//    public void buyItem(EquipmentEntity item) {
//        repo.buyItem(item);
//    }

    public void buyItem(EquipmentEntity item) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        userRepo.getUserCoins(uid, task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                Log.e("EquipmentService", "❌ Neuspešno čitanje korisnika");
                return;
            }

            DocumentSnapshot doc = task.getResult();
            int currentCoins = doc.contains("coins") ? doc.getLong("coins").intValue() : 0;

            if (currentCoins < item.price) {
                showToast("Nemaš dovoljno novčića 💰");
                return;
            }

            int newCoins = currentCoins - item.price;
            userRepo.updateUserCoins(uid, newCoins);

            item.userId = uid;
            item.isActive = false;
            item.battlesLeft = Math.max(item.battlesLeft, 0);

            AppDatabase.exec(() -> {
                EquipmentEntity existing = repo.getByNameSync(item.name);

                if (existing != null) {
                    existing.value += item.value;
                    if (existing.battlesLeft > 0 && item.battlesLeft > 0) {
                        existing.battlesLeft += item.battlesLeft;
                    }
                    repo.update(existing);
                    Log.d("EquipmentRepo", "🪄 Item već postoji → sabrani postotci i trajanje ažurirano.");
                } else {
                    repo.insert(item);
                    repo.syncToFirestore(item);
                    Log.d("EquipmentRepo", "🆕 Kupljen novi item: " + item.name +"stari je"+existing.name);
                }

                new Handler(Looper.getMainLooper()).post(() ->
                        showToast("Kupljeno: " + item.name + " ✅")
                );
            });
        });
    }


    private void showToast(String msg) {
        // Sve poruke idu kroz ovaj helper – na glavnom threadu, uvek u tačnom redosledu
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show());
    }

    public void activateItem(long id, int battles) {

        repo.activateItem(id, battles);
    }

    public void consumeBattle() {
        repo.consumeBattle();
    }
}
