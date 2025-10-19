package com.example.mobileapplication.domain.serviceImpl;

import android.util.Log;
import com.example.mobileapplication.data.models.User;

public class LevelManager {

    private static int calculateNextLevelXp(int currentLevelXp) {
        double next = currentLevelXp * 2 + currentLevelXp / 2.0;
        return ((int) Math.ceil(next / 100.0)) * 100;
    }

    private static int calculateNextPP(int previousPP) {
        if (previousPP == 0) return 40;
        return (int) Math.round(previousPP + (3.0 / 4.0) * previousPP);
    }

    private static String getTitleForLevel(int level) {
        switch (level) {
            case 1: return "Novajlija";
            case 2: return "Istraživač";
            case 3: return "Heroj";
            case 4: return "Legenda";
            default: return "Vitez nivoa " + level;
        }
    }

    public static void checkLevelUp(User user) {
        while (user.getXp() >= user.getNextLevelXp()) {
            int oldXp = user.getNextLevelXp();
            user.setLevel(user.getLevel() + 1);
            user.setXp(user.getXp() - oldXp);
            user.setNextLevelXp(calculateNextLevelXp(oldXp));
            user.setPp(calculateNextPP(user.getPp()));
            user.setTitle(getTitleForLevel(user.getLevel()));

            Log.d("LevelManager", "🎉 Novi nivo: " + user.getLevel()
                    + " | XP: " + user.getXp()
                    + " / " + user.getNextLevelXp()
                    + " | PP: " + user.getPp()
                    + " | Titula: " + user.getTitle());
        }
    }
}
