package com.thebrokenrail.combustible.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.thebrokenrail.combustible.activity.fullscreen.welcome.WelcomeActivity;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.util.Config;

import okhttp3.HttpUrl;

/**
 * Class that provides a correctly configured {@link Connection{ for sub-classes.
 */
public class LemmyActivity extends AppCompatActivity {
    /**
     * The connection to Lemmy.
     */
    protected Connection connection = null;

    private long lastConfigVersion = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load Configuration
        Config config = new Config(this);
        acknowledgeConfigChange();

        // Check If Setup Has Been Completed
        if (!config.isSetup() && !(this instanceof WelcomeActivity)) {
            // Replace Activity
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivity(intent);
            finish();
        }

        // Create Connection
        connect(config.getInstance());
        connection.setToken(config.getToken());
    }

    /**
     * Connect to a different instance
     * @param url The new instance
     */
    public void connect(HttpUrl url) {
        if (connection != null) {
            connection.close();
        }
        connection = new Connection(url);
        // Setup Callbacks
        connection.setCallbackHelper(this::runOnUiThread);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Disable Callbacks
        connection.close();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check Configuration Version
        Config config = new Config(this);
        if (config.getVersion() != lastConfigVersion) {
            // Restart
            fullRecreate();
        }
    }

    /**
     * Recreate activity and clear all {@link androidx.lifecycle.ViewModel}s.
     */
    public void fullRecreate() {
        getViewModelStore().clear();
        recreate();
    }

    protected void acknowledgeConfigChange() {
        Config config = new Config(this);
        lastConfigVersion = config.getVersion();
    }
}
