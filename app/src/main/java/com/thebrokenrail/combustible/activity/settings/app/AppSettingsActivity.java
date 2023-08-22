package com.thebrokenrail.combustible.activity.settings.app;

import androidx.preference.PreferenceFragmentCompat;

import com.thebrokenrail.combustible.activity.settings.SettingsActivity;

public class AppSettingsActivity extends SettingsActivity {
    @Override
    protected PreferenceFragmentCompat createFragment() {
        return new AppSettingsFragment();
    }
}
