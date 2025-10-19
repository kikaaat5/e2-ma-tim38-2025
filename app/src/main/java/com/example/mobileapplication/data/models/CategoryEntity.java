package com.example.mobileapplication.data.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String name;
    public String colorHex;
    @Ignore
    public int taskCount;
}
