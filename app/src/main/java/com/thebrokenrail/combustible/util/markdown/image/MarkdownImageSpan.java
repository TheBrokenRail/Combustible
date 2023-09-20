package com.thebrokenrail.combustible.util.markdown.image;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.text.style.DynamicDrawableSpan;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thebrokenrail.combustible.util.Images;

import java.lang.reflect.Field;

import io.noties.markwon.image.DrawableUtils;
import io.noties.markwon.image.ImageSize;

/**
 * {@link DynamicDrawableSpan} that displays Markdown images.
 */
public class MarkdownImageSpan extends DynamicDrawableSpan {
    private Drawable drawable;

    // Markdown Information
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

    // Last Recorded Position Of Span
    private float lastX = -1;
    private float lastY = -1;

    MarkdownImageSpan(@NonNull Drawable drawable, @Nullable ImageSize size, @NonNull String url) {
        setDrawable(drawable, null, false);
        this.size = size;
        this.url = url;
    }

    // Set Drawable
    void setDrawable(Drawable drawable, TextView textView, boolean ripple) {
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
        if (getDrawable() != null) {
            removeCallback();
        }

        // Ripple
        if (ripple && textView != null) {
            drawable = Images.createRipple(textView.getContext(), drawable);
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

    // Attach to TextView (including Drawable.Callback)
    void attach(TextView textView) {
        // Attach Callback
        attachCallback(textView);
        // Create Target
        target = new SpanTarget(textView, this);
    }

    // Create and attach Drawable.Callback
    private void attachCallback(TextView textView) {
        // Create
        callback = textView != null ? new DrawableCallback(textView) : null;
        // Attach
        setCallback(callback);
    }

    // Set Drawable.Callback
    private void setCallback(Drawable.Callback callback) {
        // Attach To Main Drawable
        getDrawable().setCallback(callback);
        // Attach To Ripple Content
        if (getDrawable() instanceof RippleDrawable) {
            Drawable content = ((RippleDrawable) getDrawable()).getDrawable(0);
            if (content != null) {
                content.setCallback(callback);
            }
        }
    }

    // Remove Drawable.Callback
    void removeCallback() {
        setCallback(null);
        callback = null;
    }

    @Nullable
    SpanTarget getTarget() {
        return target;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        // Store Position
        lastX = x;
        assert getVerticalAlignment() == ALIGN_BOTTOM;
        lastY = bottom - getDrawable().getBounds().bottom; // Taken From DynamicDrawableSpan.draw;

        // Draw
        super.draw(canvas, text, start, end, x, top, y, bottom, paint);
    }

    /**
     * Set hotspot of drawable.
     * @param x The new hotspot's X position
     * @param y The new hotspot's Y position
     */
    public void setHotspot(float x, float y) {
        getDrawable().setHotspot(x - lastX, y - lastY);
    }
}
