package com.thebrokenrail.combustible.activity;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import com.thebrokenrail.combustible.activity.settings.app.AppSettings;

public class SubApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        setDarkMode(this);
    }

    public static void setDarkMode(Context context) {
        String mode = AppSettings.DARK_MODE.getString(context);
        switch (mode) {
            case "system": {
                if (Build.VERSION.CODENAME.equals("P") || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                }
                break;
            }
            case "on": {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            }
            case "off": {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            }
        }
    }
}
