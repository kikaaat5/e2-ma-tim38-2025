package com.example.mobileapplication.data.repository;

import android.util.Log;

import com.example.mobileapplication.data.models.StatisticsModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class StatisticsRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public StatisticsRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // Ovo bi čitalo više kolekcija iz Firestore-a u pravoj implementaciji.
    // Za sada vraćamo simulirane podatke kao Task<StatisticModel>.
    public Task<StatisticsModel> loadStatistics() {
        StatisticsModel stats = new StatisticsModel();

        stats.setActiveDays(34);
        stats.setLongestStreak(12);
        stats.setCreatedTasks(57);
        stats.setCompletedTasks(42);
        stats.setUncompletedTasks(10);
        stats.setCanceledTasks(5);

        Map<String, Integer> categories = new HashMap<>();
        categories.put("Zdravlje", 10);
        categories.put("Učenje", 14);
        categories.put("Hobiji", 5);
        categories.put("Fizička aktivnost", 7);
        stats.setTasksByCategory(categories);

        Map<String, Integer> xpByDay = new HashMap<>();
        xpByDay.put("Ponedeljak", 50);
        xpByDay.put("Utorak", 90);
        xpByDay.put("Sreda", 130);
        xpByDay.put("Četvrtak", 160);
        xpByDay.put("Petak", 200);
        xpByDay.put("Subota", 250);
        xpByDay.put("Nedelja", 270);
        stats.setXpByDay(xpByDay);

        stats.setStartedMissions(2);
        stats.setFinishedMissions(1);

        Map<String, Integer> difficulty = new HashMap<>();
        difficulty.put("Lako", 80);
        difficulty.put("Srednje", 120);
        difficulty.put("Teško", 250);
        stats.setAvgDifficultyXp(difficulty);

        return Tasks.forResult(stats);
    }
}
