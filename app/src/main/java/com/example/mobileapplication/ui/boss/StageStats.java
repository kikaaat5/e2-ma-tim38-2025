package com.example.mobileapplication.ui.boss;

import com.example.mobileapplication.data.dao.TaskDao;

public final class StageStats {
    private StageStats() {}
    public static int successPercent(TaskDao dao, long start, long end){
        int eligible = dao.eligibleCount(start, end);
        if (eligible <= 0) return 0;
        int done = dao.doneCount(start, end);
        double p = (done * 100.0) / (double) eligible;
        int pct = (int) Math.round(Math.max(0, Math.min(100, p)));
        return Math.max(0, Math.min(100, pct));
    }
}
