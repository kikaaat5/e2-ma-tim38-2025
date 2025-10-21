package com.example.mobileapplication.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks",
        indices = {@Index(value = {"categoryId"}), @Index(value = {"kind"})})
public class TaskEntity {
    @PrimaryKey(autoGenerate = true) public long id;

    public String title;
    public String description;
    public long categoryId;

    public String kind;       // "ONE_TIME" | "RECURRING"
    public Long scheduledAt;  // ONE_TIME (millis)


    public Integer repeatEvery;   // null za ONE_TIME
    public String repeatUnit;     // "DAY" | "WEEK" (null za ONE_TIME)
    public Long repeatStartAt;    // inclusive
    public Long repeatEndAt;      // inclusive


    public int weightXp;
    public int importanceXp;
    public int totalXp;

    public long createdAt;
    @NonNull
    public String userId;

    @NonNull
    public String status = "ACTIVE";
}
