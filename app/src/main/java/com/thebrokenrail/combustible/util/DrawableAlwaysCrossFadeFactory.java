package com.thebrokenrail.combustible.util;

import android.graphics.drawable.Drawable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.request.transition.TransitionFactory;

public class DrawableAlwaysCrossFadeFactory implements TransitionFactory<Drawable> {
    private final Transition<Drawable> transition = new DrawableCrossFadeFactory.Builder().build().build(DataSource.REMOTE, true);

    @Override
    public Transition<Drawable> build(DataSource dataSource, boolean isFirstResource) {
        return transition;
    }
}
