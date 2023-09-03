package com.thebrokenrail.combustible.util.config;

import android.content.Context;

import com.thebrokenrail.combustible.R;

import okhttp3.HttpUrl;

class TestConfig implements Config {
    private final Context context;

    TestConfig(Context context) {
        this.context = context;
    }

    @Override
    public void setInstance(HttpUrl instance) {
        throw new RuntimeException();
    }

    @Override
    public HttpUrl getInstance() {
        return HttpUrl.parse(context.getString(R.string.recommended_instance));
    }

    @Override
    public boolean isSetup() {
        return true;
    }

    @Override
    public void finishSetup() {
    }

    @Override
    public String getToken() {
        return null;
    }

    @Override
    public void setToken(String token) {
        throw new RuntimeException();
    }

    @Override
    public long getVersion() {
        return 0;
    }

    @Override
    public void triggerRefresh() {
    }
}
