package com.thebrokenrail.combustible.util.markdown.image;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;

import io.noties.markwon.image.DrawableUtils;
import io.noties.markwon.image.ImageSize;

class DynamicImageSpan extends DynamicDrawableSpan {
    private Drawable drawable;

    @Nullable
    final ImageSize size;
    @NonNull
    final String url;

    /**
     * This object needs to be strongly referenced somewhere because {@link Drawable} only have a weak reference.
     * @noinspection FieldCanBeLocal
     */
    @Keep
    private DrawableCallback callback = null;

    @Nullable
    private SpanTarget target = null;

    DynamicImageSpan(@NonNull Drawable drawable, @Nullable ImageSize size, @NonNull String url) {
        setDrawable(drawable, null);
        this.size = size;
        this.url = url;
    }

    void setDrawable(Drawable drawable, TextView textView) {
        // Clear Cached Drawable
        try {
            @SuppressLint("DiscouragedPrivateApi") @SuppressWarnings("JavaReflectionMemberAccess") Field field = DynamicDrawableSpan.class.getDeclaredField("mDrawableRef");
            field.setAccessible(true);
            field.set(this, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Unable To Clear
            throw new RuntimeException(e);
        }

        // Disable Old Drawable
        if (this.drawable != null) {
            this.drawable.setCallback(null);
        }

        // Intrinsic Bounds
        DrawableUtils.applyIntrinsicBoundsIfEmpty(drawable);
        // Set Drawable
        this.drawable = drawable;

        // Attach Callback
        attachCallback(textView);
    }

    @Override
    public Drawable getDrawable() {
        return drawable;
    }

    void attach(TextView textView) {
        // Attach Callback
        attachCallback(textView);
        // Create Target
        target = new SpanTarget(textView, this);
    }

    private void attachCallback(TextView textView) {
        // Create
        callback = textView != null ? new DrawableCallback(textView) : null;
        // Attach
        getDrawable().setCallback(callback);
    }

    @Nullable
    SpanTarget getTarget() {
        return target;
    }
}
