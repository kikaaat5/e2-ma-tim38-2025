package com.example.mobileapplication.data.repository;

import android.content.Context;

import com.example.mobileapplication.ui.boss.BossMath;

public final class BossRepository {
    private static final String PREF = "boss_progress";
    private static final String K_BOSS_IDX = "bossIndex";
    private static final String K_HP_LEFT  = "hpLeft";
    private static final String K_COINS    = "coins";

    public static int getBossIndex(Context c) {
        return c.getSharedPreferences(PREF, 0).getInt(K_BOSS_IDX, 1);
    }
    public static void setBossIndex(Context c, int idx) {
        c.getSharedPreferences(PREF, 0).edit().putInt(K_BOSS_IDX, idx).apply();
    }

    public static long getHpLeft(Context c) {
        long def = BossMath.hpFor(getBossIndex(c));
        return c.getSharedPreferences(PREF, 0).getLong(K_HP_LEFT, def);
    }
    public static void setHpLeft(Context c, long hp) {
        c.getSharedPreferences(PREF, 0).edit().putLong(K_HP_LEFT, hp).apply();
    }

    public static long getCoins(Context c) {
        return c.getSharedPreferences(PREF, 0).getLong(K_COINS, 0);
    }
    public static void addCoins(Context c, long delta) {
        long now = getCoins(c) + Math.max(0, delta);
        c.getSharedPreferences(PREF, 0).edit().putLong(K_COINS, now).apply();
    }

    public static void advanceBoss(Context c) {
        int next = getBossIndex(c) + 1;
        setBossIndex(c, next);
        setHpLeft(c, BossMath.hpFor(next));
    }

    public static void forceReset(Context c) {
        setBossIndex(c, 1);
        setHpLeft(c, BossMath.hpFor(1));
    }

}
