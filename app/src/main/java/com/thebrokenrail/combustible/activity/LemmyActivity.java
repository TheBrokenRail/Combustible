package com.thebrokenrail.combustible.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.thebrokenrail.combustible.activity.create.BaseCreateActivity;
import com.thebrokenrail.combustible.activity.feed.comment.CommentFeedActivity;
import com.thebrokenrail.combustible.activity.fullscreen.welcome.WelcomeActivity;
import com.thebrokenrail.combustible.activity.settings.app.AppSettings;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.util.RequestCodes;
import com.thebrokenrail.combustible.util.config.Config;

import java.io.IOException;
import java.util.Objects;

import okhttp3.HttpUrl;

/**
 * Class that provides a correctly configured {@link Connection} for sub-classes.
 */
public class LemmyActivity extends AppCompatActivity {
    /**
     * The connection to Lemmy.
     */
    protected Connection connection = null;

    private long lastConfigVersion = 0;
    private long lastSettingsVersion = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load Configuration
        Config config = Config.create(this);
        acknowledgeConfigChange();
        // Settings Version
        lastSettingsVersion = AppSettings.SETTINGS_VERSION.getLong(this);

        // Check If Setup Has Been Completed
        if (!config.isSetup() && !(this instanceof WelcomeActivity)) {
            // Replace Activity
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivity(intent);
            finish();
        }

        // Create Connection
        connect(config.getInstance(), config.getToken());
    }

    /**
     * Connect to a different instance
     * @param url The new instance
     * @param token The new token
     */
    public void connect(HttpUrl url, String token) {
        if (connection != null) {
            connection.close();
        }
        connection = new Connection(url, token);
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
        boolean doDullRecreate = false;
        Config config = Config.create(this);
        if (config.getVersion() != lastConfigVersion) {
            // Restart
            doDullRecreate = true;
        }

        // Check Settings Version
        boolean doRecreate = AppSettings.SETTINGS_VERSION.getLong(this) != lastSettingsVersion;

        // Recreate
        if (doDullRecreate) {
            fullRecreate();
        } else if (doRecreate) {
            recreate();
        }
    }

    /**
     * Recreate activity and clear all {@link androidx.lifecycle.ViewModel}s.
     */
    public void fullRecreate() {
        getViewModelStore().clear();
        recreate();
    }

    /**
     * Protect activity from {@link Config}-related recreation.
     */
    protected void acknowledgeConfigChange() {
        Config config = Config.create(this);
        lastConfigVersion = config.getVersion();
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            try {
                if (requestCode == RequestCodes.CREATE_POST) {
                    // Post Created/Edited
                    boolean wasEdit = data.getBooleanExtra(BaseCreateActivity.WAS_EDIT_KEY, false);
                    Moshi moshi = new Moshi.Builder().build();
                    JsonAdapter<PostView> jsonAdapter = moshi.adapter(PostView.class);
                    PostView post = jsonAdapter.fromJson(Objects.requireNonNull(data.getStringExtra(BaseCreateActivity.OBJ_KEY)));
                    assert post != null;
                    if (!wasEdit) {
                        Intent intent = new Intent(this, CommentFeedActivity.class);
                        intent.putExtra(CommentFeedActivity.POST_ID_EXTRA, post.post.id);
                        startActivity(intent);
                    } else {
                        handleEdit(post);
                    }
                } else if (requestCode == RequestCodes.CREATE_COMMENT) {
                    // Comment Created/Edited
                    Moshi moshi = new Moshi.Builder().build();
                    JsonAdapter<CommentView> jsonAdapter = moshi.adapter(CommentView.class);
                    CommentView comment = jsonAdapter.fromJson(Objects.requireNonNull(data.getStringExtra(BaseCreateActivity.OBJ_KEY)));
                    assert comment != null;
                    handleEdit(comment);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Override this to handle when an element is edited.
     * @param element The edited element
     */
    protected void handleEdit(Object element) {
        throw new RuntimeException();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // https://stackoverflow.com/a/42253596/16198887
        ((MenuBuilder) menu).setOptionalIconsVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Trigger recreation of all other {@link LemmyActivity}s. (This does not use {@link #fullRecreate()}.)
     */
    public void recreateOtherActivities() {
        long newVersion = System.currentTimeMillis();
        AppSettings.SETTINGS_VERSION.setLong(this, newVersion);
        lastSettingsVersion = newVersion;
    }
}
