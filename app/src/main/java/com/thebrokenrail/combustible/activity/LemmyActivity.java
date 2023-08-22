package com.thebrokenrail.combustible.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.thebrokenrail.combustible.activity.fullscreen.WelcomeActivity;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.util.Config;

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
        lastConfigVersion = config.getVersion();

        // Check If Setup Has Been Completed
        if (!config.isSetup()) {
            // Replace Activity
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivity(intent);
            finish();
        }

        // Create Connection
        connection = new Connection(config.getInstance());
        connection.setToken(config.getToken());

        // Setup Callbacks
        connection.setCallbackHelper(this::runOnUiThread);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Disable Callbacks
        connection.setCallbackHelper(runnable -> {});
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check Configuration Version
        Config config = new Config(this);
        if (config.getVersion() != lastConfigVersion) {
            // Restart
            recreate();
        }
    }
}
