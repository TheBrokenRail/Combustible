package com.thebrokenrail.combustible.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.thebrokenrail.combustible.R;

import okhttp3.HttpUrl;

/**
 * Utility class to manage the configuration.
 */
public class Config {
    private final Context context;
    private final SharedPreferences preferences;
    public Config(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    private static final String INSTANCE_KEY = "lemmy_instance";

    /**
     * Sets a new URL as the current Lemmy instance.
     * @param instance The new instance's URL
     */
    public void setInstance(HttpUrl instance) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(INSTANCE_KEY, instance.toString());
        editor.remove(TOKEN_KEY);
        updateVersion(editor);
        editor.apply();
    }

    /**
     * Get the current Lemmy instance's URL.
     * @return The current URL
     */
    public HttpUrl getInstance() {
        String defaultStr = context.getString(R.string.recommended_instance);
        HttpUrl defaultUrl = HttpUrl.parse(defaultStr);
        if (defaultUrl == null) {
            throw new RuntimeException();
        }
        String url = preferences.getString(INSTANCE_KEY, defaultStr);
        HttpUrl parsedUrl = HttpUrl.parse(url);
        if (parsedUrl != null) {
            return parsedUrl;
        } else {
            return defaultUrl;
        }
    }

    private static final String SETUP_KEY = "setup_completed";

    /**
     * Check if setup has been completed.
     * @return True if setup has been completed, false otherwise
     */
    public boolean isSetup() {
        return preferences.getBoolean(SETUP_KEY, false);
    }

    /**
     * Mark setup as completed.
     */
    public void finishSetup() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SETUP_KEY, true);
        updateVersion(editor);
        editor.apply();
    }

    private static final String TOKEN_KEY = "auth_token";

    /**
     * Get the current authentication token.
     * @return An authentication token
     */
    public String getToken() {
        return preferences.getString(TOKEN_KEY, null);
    }

    /**
     * Sets a new authentication token.
     * @param token The new authentication token
     */
    public void setToken(String token) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN_KEY, token);
        updateVersion(editor);
        editor.apply();
    }

    private static final String VERSION_KEY = "version";
    private void updateVersion(SharedPreferences.Editor editor) {
        editor.putLong(VERSION_KEY, System.currentTimeMillis());
    }

    /**
     * Get the current configuration's version.
     * @return The current version
     */
    public long getVersion() {
        return preferences.getLong(VERSION_KEY, 0);
    }

    /**
     * Trigger refresh of all {@link com.thebrokenrail.combustible.activity.LemmyActivity}s.
     */
    public void triggerRefresh() {
        SharedPreferences.Editor editor = preferences.edit();
        updateVersion(editor);
        editor.apply();
    }
}
