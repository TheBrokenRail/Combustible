package com.thebrokenrail.combustible.activity.settings.app;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.AnyRes;
import androidx.preference.PreferenceManager;

import com.thebrokenrail.combustible.R;

/**
 * Collection of app settings that can be configured with {@link android.content.SharedPreferences}.
 */
public enum AppSettings {
    // General
    DARK_MODE("dark_mode", R.string.settings_dark_mode_default),
    // Media
    DISABLE_IMAGES("disable_images", R.bool.app_settings_disable_images_default),
    DISABLE_LARGE_THUMBNAIL("disable_large_thumbnail", R.bool.app_settings_disable_large_thumbnail_default),
    DISABLE_MARKDOWN_IMAGES("disable_markdown_images", R.bool.app_settings_disable_markdown_images_default),
    // Links
    USE_CUSTOM_TABS("use_custom_tabs", R.bool.app_settings_use_custom_tabs_default),
    USE_INCOGNITO_MODE("use_incognito_mode", R.bool.app_settings_use_incognito_mode_default);

    public final String key;
    @AnyRes
    private final int defaultValue;

    AppSettings(String key, @AnyRes int defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    private int getType(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getResources().getValue(defaultValue, typedValue, true);
        return typedValue.type;
    }

    public boolean getBool(Context context) {
        assert getType(context) == TypedValue.TYPE_INT_BOOLEAN;
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, context.getResources().getBoolean(defaultValue));
    }

    public String getString(Context context) {
        assert getType(context) == TypedValue.TYPE_STRING;
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, context.getResources().getString(defaultValue));
    }
}
