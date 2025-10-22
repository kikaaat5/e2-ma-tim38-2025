package com.example.mobileapplication.ui.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.mobileapplication.data.AppDatabase;
import com.example.mobileapplication.data.models.EquipmentEntity;
import com.example.mobileapplication.data.repository.EquipmentRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class EquipmentInventoryViewModel extends AndroidViewModel {

    private final EquipmentRepository repo;
    private final String uid;

    public EquipmentInventoryViewModel(@NonNull Application application) {
        super(application);
        repo = new EquipmentRepository(AppDatabase.get(application));
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public LiveData<List<EquipmentEntity>> getUserEquipment() {
        return repo.getUserEquipment(uid);
    }

    public void activate(EquipmentEntity item) {
        int battles = 0;
        if ("POTION".equalsIgnoreCase(item.type)) battles = 1;
        else if ("ARMOR".equalsIgnoreCase(item.type)) battles = 2;
        else if ("WEAPON".equalsIgnoreCase(item.type)) battles = 9999;
        repo.activateItem(item.id, battles);
    }
}
