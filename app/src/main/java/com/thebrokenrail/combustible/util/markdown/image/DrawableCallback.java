package com.thebrokenrail.combustible.util.markdown.image;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

class DrawableCallback implements Drawable.Callback {
    private final WeakReference<View> view;

    public DrawableCallback(View view) {
        this.view = new WeakReference<>(view);
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
        if (view.get() != null) {
            view.get().invalidate();
        }
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        if (view.get() != null) {
            view.get().postDelayed(what, when);
        }
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        if (view.get() != null) {
            view.get().removeCallbacks(what);
        }
    }
}
