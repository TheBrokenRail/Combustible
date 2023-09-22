package com.thebrokenrail.combustible.util.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.thebrokenrail.combustible.activity.settings.app.AppSettings;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class GlideUtil {
    private static final int NSFW_BLUR = 36;

    public static void load(Context context, RequestManager requestManager, String url, Transformation<Bitmap> scalingTransformation, int cornerRadius, boolean blur, boolean crossFade, Drawable placeholder, Target<Drawable> target) {
        // Check Settings
        boolean disableImages = AppSettings.DISABLE_IMAGES.getBool(context);
        if (disableImages) {
            target.onLoadCleared(placeholder);
            return;
        }

        // Load
        RequestBuilder<Drawable> requestBuilder = requestManager.load(url);

        // Placeholder
        if (placeholder != null) {
            requestBuilder = requestBuilder.placeholder(placeholder);
        }

        // Transformations
        List<Transformation<Bitmap>> transformations = new ArrayList<>();
        if (scalingTransformation != null) {
            // Scaling
            transformations.add(scalingTransformation);
        }
        if (cornerRadius > 0) {
            // Rounded Corners
            transformations.add(new RoundedCorners(cornerRadius));
        }
        if (blur) {
            // Blur
            transformations.add(new BlurTransformation(NSFW_BLUR));
        }
        if (transformations.size() > 1) {
            //noinspection unchecked
            Transformation<Bitmap>[] transformationsArray = (Transformation<Bitmap>[]) transformations.toArray(new Transformation[0]);
            requestBuilder = requestBuilder.transform(transformationsArray);
        } else if (transformations.size() == 1) {
            requestBuilder = requestBuilder.transform(transformations.get(0));
        }

        // Transition
        if (crossFade) {
            if (requestManager.isPaused()) {
                // Always Show Transition When Loading Cached Images If Loading Is Passed
                requestBuilder = requestBuilder.transition(DrawableTransitionOptions.with(new DrawableAlwaysCrossFadeFactory()));
            } else {
                requestBuilder = requestBuilder.transition(DrawableTransitionOptions.withCrossFade(new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(DrawableAlwaysCrossFadeFactory.IS_CROSS_FADE).build()));
            }
        }

        // Target
        requestBuilder.into(target);
    }

    public static void load(Context context, RequestManager requestManager, String url, Transformation<Bitmap> scalingTransformation, int cornerRadius, boolean blur, boolean crossFade, Drawable placeholder, ImageView target) {
        load(context, requestManager, url, scalingTransformation, cornerRadius, blur, crossFade, placeholder, new DrawableImageViewTarget(target));
    }
}
