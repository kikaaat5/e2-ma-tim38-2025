package com.example.mobileapplication.data.models;

import java.util.Map;

public class StatisticsModel {
    private int activeDays;
    private int longestStreak;
    private int createdTasks;
    private int completedTasks;
    private int uncompletedTasks;
    private int canceledTasks;
    private Map<String, Integer> tasksByCategory;
    private Map<String, Integer> xpByDay;
    private int startedMissions;
    private int finishedMissions;
    private Map<String, Integer> avgDifficultyXp;

    public StatisticsModel() {
    }

    public int getActiveDays() {
        return activeDays;
    }

    public void setActiveDays(int activeDays) {
        this.activeDays = activeDays;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public int getCreatedTasks() {
        return createdTasks;
    }

    public void setCreatedTasks(int createdTasks) {
        this.createdTasks = createdTasks;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(int completedTasks) {
        this.completedTasks = completedTasks;
    }

    public int getUncompletedTasks() {
        return uncompletedTasks;
    }

    public void setUncompletedTasks(int uncompletedTasks) {
        this.uncompletedTasks = uncompletedTasks;
    }

    public int getCanceledTasks() {
        return canceledTasks;
    }

    public void setCanceledTasks(int canceledTasks) {
        this.canceledTasks = canceledTasks;
    }

    public Map<String, Integer> getTasksByCategory() {
        return tasksByCategory;
    }

    public void setTasksByCategory(Map<String, Integer> tasksByCategory) {
        this.tasksByCategory = tasksByCategory;
    }

    public Map<String, Integer> getXpByDay() {
        return xpByDay;
    }

    public void setXpByDay(Map<String, Integer> xpByDay) {
        this.xpByDay = xpByDay;
    }

    public int getStartedMissions() {
        return startedMissions;
    }

    public void setStartedMissions(int startedMissions) {
        this.startedMissions = startedMissions;
    }

    public int getFinishedMissions() {
        return finishedMissions;
    }

    public void setFinishedMissions(int finishedMissions) {
        this.finishedMissions = finishedMissions;
    }

    public Map<String, Integer> getAvgDifficultyXp() {
        return avgDifficultyXp;
    }

    public void setAvgDifficultyXp(Map<String, Integer> avgDifficultyXp) {
        this.avgDifficultyXp = avgDifficultyXp;
    }
}
