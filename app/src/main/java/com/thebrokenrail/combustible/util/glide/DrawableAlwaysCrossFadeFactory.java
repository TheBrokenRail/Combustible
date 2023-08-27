package com.thebrokenrail.combustible.util.glide;

import android.graphics.drawable.Drawable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.request.transition.TransitionFactory;

class DrawableAlwaysCrossFadeFactory implements TransitionFactory<Drawable> {
    static final boolean IS_CROSS_FADE = true;

    private final Transition<Drawable> transition = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(IS_CROSS_FADE).build().build(DataSource.REMOTE, true);

    @Override
    public Transition<Drawable> build(DataSource dataSource, boolean isFirstResource) {
        return transition;
    }
}
