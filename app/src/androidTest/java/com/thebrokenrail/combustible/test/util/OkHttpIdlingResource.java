package com.thebrokenrail.combustible.test.util;

import androidx.test.espresso.IdlingResource;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

public class OkHttpIdlingResource implements IdlingResource {
    private final Dispatcher dispatcher;
    private volatile ResourceCallback callback;

    public OkHttpIdlingResource(OkHttpClient client) {
        dispatcher = client.dispatcher();
        dispatcher.setIdleCallback(() -> {
            if (callback != null) {
                callback.onTransitionToIdle();
            }
        });
    }

    @Override
    public String getName() {
        return "okhttp";
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.callback = callback;
    }

    @Override
    public boolean isIdleNow() {
        return dispatcher.runningCallsCount() == 0;
    }
}
