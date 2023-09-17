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

class MarkdownImageSpan extends DynamicDrawableSpan {
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
    private MarkdownDrawableCallback callback = null;

    @Nullable
    private MarkdownImageTarget target = null;

    MarkdownImageSpan(@NonNull Drawable drawable, @Nullable ImageSize size, @NonNull String url) {
        setDrawable(drawable);
        this.size = size;
        this.url = url;
        // Attach Callback
        attachCallback(null);
    }

    void setDrawable(Drawable drawable) {
        // Clear Cached Drawable
        try {
            @SuppressLint("DiscouragedPrivateApi") @SuppressWarnings("JavaReflectionMemberAccess") Field field = DynamicDrawableSpan.class.getDeclaredField("mDrawableRef");
            field.setAccessible(true);
            field.set(this, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Unable To Clear
            throw new RuntimeException(e);
        }

        // Intrinsic Bounds
        DrawableUtils.applyIntrinsicBoundsIfEmpty(drawable);
        // Set Drawable
        this.drawable = drawable;
    }

    @Override
    public Drawable getDrawable() {
        return drawable;
    }

    void attach(TextView textView) {
        // Attach Callback
        attachCallback(textView);
        // Create Target
        target = new MarkdownImageTarget(textView, this);
    }

    private void attachCallback(TextView textView) {
        // Create
        callback = textView != null ? new MarkdownDrawableCallback(textView) : null;
        // Attach
        getDrawable().setCallback(callback);
    }

    @Nullable
    MarkdownImageTarget getTarget() {
        return target;
    }
}
