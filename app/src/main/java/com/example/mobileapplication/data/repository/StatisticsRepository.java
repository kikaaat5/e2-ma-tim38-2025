package com.example.mobileapplication.data.repository;

import android.util.Log;

import com.example.mobileapplication.data.dao.TaskDao;
import com.example.mobileapplication.data.models.CategoryEntity;
import com.example.mobileapplication.data.models.StatisticsModel;
import com.example.mobileapplication.data.models.TaskEntity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsRepository {

    private final TaskRepository taskRepository;
    private final TaskDao taskDao;

    public StatisticsRepository(TaskRepository taskRepository, TaskDao taskDao) {
        this.taskRepository = taskRepository;
        this.taskDao = taskDao;
    }

    public Task<StatisticsModel> loadStatistics() {
        TaskCompletionSource<StatisticsModel> tcs = new TaskCompletionSource<>();

        new Thread(() -> {
            try {
                StatisticsModel stats = new StatisticsModel();

                List<TaskEntity> allTasks = taskDao.getAllTasksSync();
                if (allTasks == null || allTasks.isEmpty()) {
                    TaskEntity t1 = new TaskEntity();
                    t1.title = "Zadatak 1";
                    t1.totalXp = 120;
                    t1.status = "DONE";
                    t1.createdAt = System.currentTimeMillis();

                    TaskEntity t2 = new TaskEntity();
                    t2.title = "Zadatak 2";
                    t2.totalXp = 80;
                    t2.status = "CANCELLED";
                    t2.createdAt = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000);

                    TaskEntity t3 = new TaskEntity();
                    t3.title = "Zadatak 3";
                    t3.totalXp = 150;
                    t3.status = "ACTIVE";
                    t3.createdAt = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000);

                    taskDao.insert(t1);
                    taskDao.insert(t2);
                    taskDao.insert(t3);

                    allTasks = taskDao.getAllTasksSync();
                }
                int completed = 0, active = 0, cancelled = 0;
                for (TaskEntity t : allTasks) {
                    switch (t.status) {
                        case "DONE": completed++; break;
                        case "CANCELLED": cancelled++; break;
                        default: active++; break;
                    }
                }

                stats.setCompletedTasks(completed);
                stats.setUncompletedTasks(active);
                stats.setCanceledTasks(cancelled);
                stats.setCreatedTasks(allTasks.size());

                Map<String, Integer> byCategory = new HashMap<>();
                List<CategoryEntity> categories = taskRepository.getCategoriesWithTaskCount();
                for (CategoryEntity c : categories) {
                    byCategory.put(c.name, c.taskCount);
                }
                stats.setTasksByCategory(byCategory);

                int easyXp = 0, mediumXp = 0, hardXp = 0;
                for (TaskEntity t : allTasks) {
                    int xp = t.totalXp;
                    if (xp < 100) easyXp += xp;
                    else if (xp < 200) mediumXp += xp;
                    else hardXp += xp;
                }
                Map<String, Integer> xpByDiff = new HashMap<>();
                xpByDiff.put("Laki", easyXp);
                xpByDiff.put("Srednji", mediumXp);
                xpByDiff.put("Teški", hardXp);
                stats.setAvgDifficultyXp(xpByDiff);

                Map<String, Integer> xpByDay = new HashMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.", Locale.getDefault());
                long now = System.currentTimeMillis();
                long sevenDaysAgo = now - (7L * 24 * 60 * 60 * 1000);

                for (TaskEntity t : allTasks) {
                    if (t.createdAt >= sevenDaysAgo) {
                        String date = sdf.format(new Date(t.createdAt));
                        int oldXp = xpByDay.getOrDefault(date, 0);
                        xpByDay.put(date, oldXp + t.totalXp);
                    }
                }
                stats.setXpByDay(xpByDay);

                stats.setActiveDays(taskDao.getTotalTasks() > 0 ? 7 : 0);
                stats.setLongestStreak(completed > 0 ? 5 : 0);
                stats.setStartedMissions(active);
                stats.setFinishedMissions(completed);

                tcs.setResult(stats);
            } catch (Exception e) {
                Log.e("StatisticsRepository", " Greška pri izračunavanju statistike", e);
                tcs.setException(e);
            }
        }).start();

        return tcs.getTask();
    }
}
