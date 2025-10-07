package com.example.mobileapplication;

import android.app.Application;
import dagger.hilt.android.HiltAndroidApp;

/**
 * Glavna Application klasa – ulazna tačka za Hilt.
 * Ova klasa se kreira pre svih aktivnosti i omogućava Hilt Dependency Injection.
 */
@HiltAndroidApp
public class MobileApplication extends Application {

}