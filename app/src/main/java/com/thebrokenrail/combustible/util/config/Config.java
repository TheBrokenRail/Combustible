package com.thebrokenrail.combustible.util.config;

import android.content.Context;

import okhttp3.HttpUrl;

/**
 * Utility class to manage the configuration.
 */
public interface Config {
    /**
     * Create a configuration management object.
     * @param context The Android context
     * @return The new object
     */
    static Config create(Context context) {
        try {
            Class.forName("androidx.test.espresso.Espresso");
            // Test Mode
            return new TestConfig(context);
        } catch (ClassNotFoundException ignored) {
            // Normal Mode
            return new NormalConfig(context);
        }
    }

    /**
     * Sets a new URL as the current Lemmy instance.
     * @param instance The new instance's URL
     */
    void setInstance(HttpUrl instance);

    /**
     * Get the current Lemmy instance's URL.
     * @return The current URL
     */
    HttpUrl getInstance();

    /**
     * Check if setup has been completed.
     * @return True if setup has been completed, false otherwise
     */
    boolean isSetup();

    /**
     * Mark setup as completed.
     */
    void finishSetup();

    /**
     * Get the current authentication token.
     * @return An authentication token
     */
    String getToken();

    /**
     * Sets a new authentication token.
     * @param token The new authentication token
     */
    void setToken(String token);

    /**
     * Get the current configuration's version.
     * @return The current version
     */
    long getVersion();

    /**
     * Trigger refresh of all {@link com.thebrokenrail.combustible.activity.LemmyActivity}s.
     */
    void triggerRefresh();
}
