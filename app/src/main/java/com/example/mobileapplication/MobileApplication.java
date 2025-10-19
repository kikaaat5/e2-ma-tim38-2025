package com.example.mobileapplication;

import android.app.Application;
import android.content.Context;

import dagger.hilt.android.HiltAndroidApp;

/**
 * Glavna Application klasa – ulazna tačka za Hilt.
 * Ova klasa se kreira pre svih aktivnosti i omogućava Hilt Dependency Injection.
 */
@HiltAndroidApp
public class MobileApplication extends Application {

    private static MobileApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Context getAppContext() {
        return instance.getApplicationContext();
    }
}
