package com.example.mobileapplication.ui.boss;

import android.content.Context;
import android.content.SharedPreferences;

public final class PlayerPower {
    private PlayerPower() {}

    private static final String PREF = "player_progress";
    private static final String KEY_PP = "pp";

    private static final int FIRST_LEVEL_PP = 40;

    private static SharedPreferences prefs(Context c){
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }
    public static int currentPP(Context c){
        // TODO: kasnije saberi PP iz opreme + bazni PP.
        return prefs(c).getInt(KEY_PP, 50);
    }

    public static void setPP(Context c, int value){
        prefs(c).edit().putInt(KEY_PP, Math.max(0, value)).apply();
    }

    public static int applyWinRule(Context c){
        int old = currentPP(c);
        int now;
        if (old <= 30) {
            now = 30 + FIRST_LEVEL_PP;
        } else {
            now = Math.round(old * 1.75f);
        }
        setPP(c, now);
        return now;
    }

}
