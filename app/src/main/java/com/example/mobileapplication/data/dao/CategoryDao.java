package com.example.mobileapplication.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.mobileapplication.data.models.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert long insert(CategoryEntity c);

    @Query("SELECT * FROM categories ORDER BY name")
    LiveData<List<CategoryEntity>> all();

    @Query("SELECT COUNT(*) FROM categories WHERE id=:id")
    int exists(long id);

    @Query("SELECT COUNT(*) FROM categories")
    int count();

    @Query("SELECT * FROM categories")
    List<CategoryEntity> getAllCategoriesSync();

}
