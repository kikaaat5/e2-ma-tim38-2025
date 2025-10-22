package com.example.mobileapplication.ui.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.mobileapplication.data.models.EquipmentEntity;
import com.example.mobileapplication.domain.serviceImpl.EquipmentServiceImpl;

import java.util.List;

public class EquipmentViewModel extends AndroidViewModel {

    private final EquipmentServiceImpl service;

    public EquipmentViewModel(@NonNull Application app) {
        super(app);
        this.service = new EquipmentServiceImpl(app);
    }

    public LiveData<List<EquipmentEntity>> getEquipment() {
        return service.getUserEquipment();
    }

    public LiveData<List<EquipmentEntity>> getActiveEquipment() {
        return service.getActiveEquipment();
    }

    public void buyItem(EquipmentEntity item) {
        service.buyItem(item);
    }

    public void activateItem(long id, int battles) {

        service.activateItem(id, battles);
    }

    public void consumeBattle() {
        service.consumeBattle();
    }
}
