package com.thebrokenrail.combustible.util.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.thebrokenrail.combustible.R;

import okhttp3.HttpUrl;

class NormalConfig implements Config {
    private final Context context;
    private final SharedPreferences preferences;

    NormalConfig(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    private static final String INSTANCE_KEY = "lemmy_instance";

    @Override
    public void setInstance(HttpUrl instance) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(INSTANCE_KEY, instance.toString());
        editor.remove(TOKEN_KEY);
        updateVersion(editor);
        editor.apply();
    }

    @Override
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

    @Override
    public boolean isSetup() {
        return preferences.getBoolean(SETUP_KEY, false);
    }

    @Override
    public void finishSetup() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SETUP_KEY, true);
        updateVersion(editor);
        editor.apply();
    }

    private static final String TOKEN_KEY = "auth_token";

    @Override
    public String getToken() {
        return preferences.getString(TOKEN_KEY, null);
    }

    @Override
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

    @Override
    public long getVersion() {
        return preferences.getLong(VERSION_KEY, 0);
    }

    @Override
    public void triggerRefresh() {
        SharedPreferences.Editor editor = preferences.edit();
        updateVersion(editor);
        editor.apply();
    }
}
