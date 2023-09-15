package com.thebrokenrail.combustible.util.markdown.image;

import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.noties.markwon.image.DrawableUtils;
import io.noties.markwon.image.ImageSize;

class MarkdownImageSpan extends ImageSpan {
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

    MarkdownImageSpan(@NonNull Drawable drawable, TextView textView, @Nullable ImageSize size, @NonNull String url) {
        super(resizeDrawable(drawable));
        this.size = size;
        this.url = url;
        // Attach Callback
        attachCallback(textView);
    }

    MarkdownImageSpan(MarkdownImageSpan oldSpan, TextView textView, Drawable drawable) {
        this(drawable, textView, oldSpan.size, oldSpan.url);
    }

    private static Drawable resizeDrawable(Drawable drawable) {
        // Intrinsic Bounds
        DrawableUtils.applyIntrinsicBoundsIfEmpty(drawable);

        // Return
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
