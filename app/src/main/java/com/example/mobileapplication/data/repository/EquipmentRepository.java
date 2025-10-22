package com.example.mobileapplication.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.mobileapplication.data.AppDatabase;
import com.example.mobileapplication.data.dao.EquipmentDao;
import com.example.mobileapplication.data.models.EquipmentEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipmentRepository {

    private final EquipmentDao equipmentDao;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public EquipmentRepository(AppDatabase db) {
        this.equipmentDao = db.equipmentDao();
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public LiveData<List<EquipmentEntity>> getUserEquipment() {
        String uid = getUid();
        return equipmentDao.getAll(uid);
    }

    // 🔹 Dohvatanje aktivne opreme (pre borbe, prikaz u UI)
    public LiveData<List<EquipmentEntity>> getActiveEquipment() {
        String uid = getUid();
        return equipmentDao.getActive(uid);
    }

    public EquipmentEntity getByNameSync(String name) {
        String uid = getUid();
        return equipmentDao.getByNameSync(name, uid);
    }

    // 🔹 Kupovina itema iz prodavnice
    public void buyItem(EquipmentEntity item) {
        String uid = getUid();
        item.userId = uid;

        AppDatabase.exec(() -> {
            long id = equipmentDao.insert(item);
            item.id = id;

            // sinhronizacija sa Firebase
            syncToFirestore(item);
            Log.d("EquipmentDebug", "✅ Kupljen predmet: " + item.name);
        });
    }

    // 🔹 Aktivacija kupljene opreme
    public void activateItem(long id, int battles) {
        String uid = getUid();
        AppDatabase.exec(() -> {
            equipmentDao.activate(id, uid);

            EquipmentEntity item = equipmentDao.getByIdSync(id, uid);
            if (item != null) {
                item.isActive = true;
                Log.d("EquipmentDebug", "🟢 Aktiviran predmet AKAKAKAK ID=" + id +item.type + " za " + item.battlesLeft + " borbi");
                syncToFirestore(item);

            }
            Log.d("EquipmentDebug", "🟢 Aktiviran predmet ID=" + id + " za " + item.battlesLeft + " borbi");
        });

    }

    // 🔹 Smanji trajanje aktivne opreme nakon borbe
    public void consumeBattle() {
        String uid = getUid();
        AppDatabase.exec(() -> {
            equipmentDao.consumeBattle(uid);
            equipmentDao.cleanupExpired(uid);
        });

        AppDatabase.exec(() -> {
            List<EquipmentEntity> all = equipmentDao.getAllForDebug(uid);
            for (EquipmentEntity e : all) {
                Log.d("EquipmentDebug", e.name + " → active=" + e.isActive + ", left=" + e.battlesLeft);
            }
        });
    }


    // 🔹 Pomoćna metoda za sinhronizaciju sa Firebase
    public void syncToFirestore(EquipmentEntity e) {
        if (auth.getCurrentUser() == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("id", e.id);
        data.put("userId", e.userId);
        data.put("name", e.name);
        data.put("type", e.type);
        data.put("effect", e.effect);
        data.put("value", e.value);
        data.put("price", e.price);
        data.put("duration", e.duration);
        data.put("isActive", e.isActive);
        data.put("battlesLeft", e.battlesLeft);

        firestore.collection("equipment")
                .document(e.userId + "_" + e.id)
                .set(data)
                .addOnSuccessListener(a -> Log.d("FirestoreSync", "✅ Sync: " + e.name))
                .addOnFailureListener(err -> Log.e("FirestoreSync", "❌ " + err.getMessage()));
    }

    // 🔹 Dohvati UID bez null pointera
    private String getUid() {
        if (auth.getCurrentUser() == null)
            throw new IllegalStateException("Korisnik nije prijavljen!");
        return auth.getCurrentUser().getUid();
    }

    public LiveData<List<EquipmentEntity>> getUserEquipment(String userId) {
        return equipmentDao.getByUser(userId);
    }

    public long insert(EquipmentEntity e) {
        return equipmentDao.insert(e);
    }

    public void update(EquipmentEntity e){
        equipmentDao.update(e);
    }


}
