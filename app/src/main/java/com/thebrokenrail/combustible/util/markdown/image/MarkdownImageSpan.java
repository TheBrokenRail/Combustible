package com.thebrokenrail.combustible.util.markdown.image;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

import io.noties.markwon.image.DrawableUtils;
import io.noties.markwon.image.ImageSize;
import io.noties.markwon.image.ImageSizeResolverDef;

class MarkdownImageSpan extends ImageSpan {
    private static class CustomImageSizeResolver extends ImageSizeResolverDef {
        private Rect resolveImageSize(Drawable drawable, TextView textView, ImageSize size) {
            int width = textView.getWidth() - textView.getPaddingLeft() - textView.getPaddingRight();
            float textSize = textView.getPaint().getTextSize();
            return resolveImageSize(size, drawable.getBounds(), width, textSize);
        }
    }

    private WeakReference<TextView> textView;
    private final ImageSize size;
    final String url;

    /**
     * This object needs to be strongly referenced somewhere because {@link Drawable} only have a weak reference.
     * @noinspection FieldCanBeLocal
     */
    @Keep
    private MarkdownDrawableCallback callback = null;

    private MarkdownImageTarget target = null;

    MarkdownImageSpan(@NonNull Drawable drawable, TextView textView, ImageSize size, String url) {
        super(resizeDrawable(drawable, textView, size));
        this.textView = new WeakReference<>(textView);
        this.size = size;
        this.url = url;
        // Attach Callback
        attachCallback();
    }

    MarkdownImageSpan(MarkdownImageSpan oldSpan, Drawable drawable) {
        this(drawable, oldSpan.textView.get(), oldSpan.size, oldSpan.url);
    }

    private static Drawable resizeDrawable(Drawable drawable, TextView textView, ImageSize size) {
        // Intrinsic Bounds
        DrawableUtils.applyIntrinsicBoundsIfEmpty(drawable);
        // Resolve Size
        if (textView != null) {
            drawable.setBounds(new CustomImageSizeResolver().resolveImageSize(drawable, textView, size));
        }
        // Return
        return drawable;
    }

    void attach(TextView textView) {
        // Store TextView Reference
        this.textView = new WeakReference<>(textView);
        // Attach Callback
        attachCallback();
        // Create Target
        target = new MarkdownImageTarget(textView, this);
    }

    private void attachCallback() {
        // Create
        callback = textView.get() != null ? new MarkdownDrawableCallback(textView.get()) : null;
        // Attach
        getDrawable().setCallback(callback);
    }

    MarkdownImageTarget getTarget() {
        return target;
    }
}
