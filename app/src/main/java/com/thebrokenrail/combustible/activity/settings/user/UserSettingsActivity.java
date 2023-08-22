package com.thebrokenrail.combustible.activity.settings.user;

import android.os.Bundle;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.settings.SettingsActivity;
import com.thebrokenrail.combustible.activity.settings.SettingsFragment;
import com.thebrokenrail.combustible.api.Connection;

import java.util.Objects;

public class UserSettingsActivity extends SettingsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Title
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.feed_menu_user_settings);
    }

    @Override
    protected SettingsFragment createFragment() {
        return new UserSettingsFragment();
    }

    Connection getConnection() {
        return connection;
    }
}
