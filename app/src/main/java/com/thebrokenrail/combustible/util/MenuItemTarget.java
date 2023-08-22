package com.thebrokenrail.combustible.util;

import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.Objects;

/**
 * Based off of {@link com.bumptech.glide.request.target.ImageViewTarget}.
 */
public class MenuItemTarget extends CustomTarget<Drawable> {
    private final MenuItem item;
    public final Drawable.ConstantState placeholder;
    private final PorterDuff.Mode placeholderTint;

    @Nullable
    private Animatable animatable;

    public MenuItemTarget(MenuItem item) {
        this.item = item;
        this.placeholder = Objects.requireNonNull(item.getIcon()).getConstantState();
        placeholderTint = MenuItemCompat.getIconTintMode(item);
    }

    @Override
    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
        setResourceInternal(resource);
    }

    @Override
    public void onLoadCleared(@Nullable Drawable placeholder) {
        if (animatable != null) {
            animatable.stop();
        }
        setResourceInternal(null);
        setDrawable(placeholder, true);
    }

    @Override
    public void onLoadStarted(@Nullable Drawable placeholder) {
        setResourceInternal(null);
        setDrawable(placeholder, true);
    }

    @Override
    public void onLoadFailed(@Nullable Drawable errorDrawable) {
        setResourceInternal(null);
        setDrawable(errorDrawable, true);
    }

    @Override
    public void onStart() {
        if (animatable != null) {
            animatable.start();
        }
    }

    @Override
    public void onStop() {
        if (animatable != null) {
            animatable.stop();
        }
    }

    private void setResourceInternal(@Nullable Drawable resource) {
        // Order matters here. Set the resource first to make sure that the Drawable has a valid and
        // non-null Callback before starting it.
        setDrawable(resource, false);
        maybeUpdateAnimatable(resource);
    }

    private void maybeUpdateAnimatable(@Nullable Drawable resource) {
        if (resource instanceof Animatable) {
            animatable = (Animatable) resource;
            animatable.start();
        } else {
            animatable = null;
        }
    }

    private void setDrawable(Drawable drawable, boolean isPlaceholder) {
        item.setIcon(drawable);
        MenuItemCompat.setIconTintMode(item, isPlaceholder ? placeholderTint : PorterDuff.Mode.DST);
    }
}
