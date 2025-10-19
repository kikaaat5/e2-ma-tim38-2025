package com.example.mobileapplication.ui.boss;

public final class BossMath {
    private BossMath() {}
    public static long hpFor(int bossIndex) {
        if (bossIndex <= 1) return 200;
        double hp = 200;
        for (int i = 2; i <= bossIndex; i++) hp *= 2.5;
        return Math.round(hp);
    }
    public static long coinsFor(int bossIndex) {
        if (bossIndex <= 1) return 200;
        double c = 200;
        for (int i = 2; i <= bossIndex; i++) c *= 1.2;
        return Math.round(c);
    }
}
