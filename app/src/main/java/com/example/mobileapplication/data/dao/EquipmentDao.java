package com.example.mobileapplication.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.mobileapplication.data.models.EquipmentEntity;

import java.util.List;

@Dao
public interface EquipmentDao {
    @Insert
    long insert(EquipmentEntity e);

    @Query("SELECT * FROM equipment WHERE userId = :uid")
    LiveData<List<EquipmentEntity>> getAll(String uid);

    @Query("SELECT * FROM equipment WHERE userId = :uid AND isActive = 1")
    LiveData<List<EquipmentEntity>> getActive(String uid);

    @Query("UPDATE equipment SET isActive = 1, battlesLeft = :battles WHERE id = :id AND userId = :uid")
    void activate(long id, String uid, int battles);

    @Query("UPDATE equipment SET battlesLeft = battlesLeft - 1 WHERE userId = :uid AND isActive = 1 AND battlesLeft > 0")
    void consumeBattle(String uid);

    @Query("DELETE FROM equipment WHERE userId = :uid AND battlesLeft <= 0 AND isActive = 1")
    void cleanupExpired(String uid);

    @Query("SELECT * FROM equipment WHERE id = :id AND userId = :uid LIMIT 1")
    EquipmentEntity getByIdSync(long id, String uid);
    @Query("SELECT * FROM equipment WHERE userId = :uid")
    LiveData<List<EquipmentEntity>> getByUser(String uid);



}
