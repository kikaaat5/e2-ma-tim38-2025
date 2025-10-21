package com.example.mobileapplication.ui.boss;

import com.example.mobileapplication.data.dao.TaskDao;
import com.google.firebase.auth.FirebaseAuth;

public final class StageStats {
    private StageStats() {}
    private static FirebaseAuth auth = FirebaseAuth.getInstance();

    private static String userId = auth.getCurrentUser().getUid();
    public static int successPercent(TaskDao dao, long start, long end){
        int eligible = dao.eligibleCount(userId, start, end);
        if (eligible <= 0) return 0;
        int done = dao.doneCount(userId, start, end);
        double p = (done * 100.0) / (double) eligible;
        int pct = (int) Math.round(Math.max(0, Math.min(100, p)));
        return Math.max(0, Math.min(100, pct));
    }
}
