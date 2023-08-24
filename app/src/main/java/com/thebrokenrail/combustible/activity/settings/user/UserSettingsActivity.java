package com.thebrokenrail.combustible.activity.settings.user;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.settings.SettingsActivity;
import com.thebrokenrail.combustible.activity.settings.SettingsFragment;
import com.thebrokenrail.combustible.util.Uploader;

import java.util.Objects;

public class UserSettingsActivity extends SettingsActivity {
    static final int UPLOAD_AVATAR = 200;
    static final int UPLOAD_BANNER = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check
        if (!connection.hasToken()) {
            finish();
        }

        // Title
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.feed_menu_user_settings);
    }

    @Override
    protected SettingsFragment createFragment() {
        return new UserSettingsFragment();
    }

    private UserSettingsDataStore getDataStore() {
        // Get Data Store
        UserSettingsFragment fragment = (UserSettingsFragment) getSupportFragmentManager().findFragmentById(R.id.settings);
        assert fragment != null;
        UserSettingsDataStore dataStore = fragment.dataStore;
        assert dataStore != null;
        return dataStore;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Uploading
        if (requestCode == UPLOAD_AVATAR) {
            // Avatar
            Uploader.onActivityResult(this, connection, resultCode, data, s -> getDataStore().putString("avatar", s));
        } else if (requestCode == UPLOAD_BANNER) {
            // Banner
            Uploader.onActivityResult(this, connection, resultCode, data, s -> getDataStore().putString("banner", s));
        }
    }
}
