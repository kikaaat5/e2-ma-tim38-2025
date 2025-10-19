// data/CategoryDao.java
package com.example.mobileapplication.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mobileapplication.data.models.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert long insert(CategoryEntity c);

    @Update int update(CategoryEntity c);

    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<CategoryEntity>> all();

    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    List<CategoryEntity> allSync();

    @Query("SELECT COUNT(*) FROM categories")
    int count();

    @Query("SELECT COUNT(*) FROM categories WHERE LOWER(colorHex)=LOWER(:hex)")
    int existsColor(String hex);

    @Query("SELECT COUNT(*) FROM categories WHERE LOWER(colorHex)=LOWER(:hex) AND id!=:excludeId")
    int existsColorExcluding(String hex, long excludeId);

    @Query("SELECT COUNT(*) FROM tasks WHERE categoryId=:catId AND status='ACTIVE'")
    int activeTasksIn(long catId);

    @Query("DELETE FROM categories WHERE id=:id")
    int deleteById(long id);

    @Query("SELECT * FROM categories WHERE id=:id LIMIT 1")
    CategoryEntity byIdSync(long id);
}
