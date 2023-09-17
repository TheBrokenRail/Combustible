package com.thebrokenrail.combustible.util.markdown.image;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.lang.ref.WeakReference;

class MarkdownImageTarget extends CustomTarget<Drawable> {
    @NonNull
    private final WeakReference<TextView> textView;
    @NonNull
    private final MarkdownImageSpan span;

    @Nullable
    private Animatable animatable = null;

    MarkdownImageTarget(TextView textView, @NonNull MarkdownImageSpan span) {
        this.textView = new WeakReference<>(textView);
        this.span = span;
    }

    private void setDrawable(Drawable drawable) {
        // Disable Old Drawable
        span.getDrawable().setCallback(null);

        // Update Span
        span.setDrawable(drawable);
        if (textView.get() != null) {
            textView.get().setText(textView.get().getText());
        }

        // Update Animation
        maybeUpdateAnimatable(drawable);
    }

    @Override
    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
        setDrawable(resource);
    }

    @Override
    public void onLoadCleared(@Nullable Drawable placeholder) {
        // Stop Animation
        if (animatable != null) {
            animatable.stop();
        }
        // Set Placeholder
        if (textView.get() != null) {
            setDrawable(CustomImagePlugin.getPlaceholder(textView.get().getContext()));
        }
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

    private void maybeUpdateAnimatable(Drawable resource) {
        if (resource instanceof Animatable) {
            animatable = (Animatable) resource;
            animatable.start();
        } else {
            animatable = null;
        }
    }
}
