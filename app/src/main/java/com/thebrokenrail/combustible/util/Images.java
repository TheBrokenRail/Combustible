package com.thebrokenrail.combustible.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.util.TypedValue;
import android.view.Gravity;

import androidx.annotation.ColorInt;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.load.Transformation;
import com.thebrokenrail.combustible.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class Images {
    private static final int NSFW_BLUR = 36;
    // https://github.com/LemmyNet/lemmy-ui/blob/9c489680de48247cf85b1a4b33f3d0d1a8f88673/src/shared/utils/media/is-image.ts#L1
    private static final Pattern imagePattern = Pattern.compile("(http)?s?:?(//[^\"']*\\.(?:jpg|jpeg|gif|png|svg|webp))");

    public static boolean isImage(String url) {
        if (url == null) {
            return false;
        }
        return imagePattern.matcher(url).find();
    }

    @SafeVarargs
    public static Transformation<Bitmap>[] addBlurTransformation(boolean blur, Transformation<Bitmap>... transformations) {
        List<Transformation<Bitmap>> result = new ArrayList<>(Arrays.asList(transformations));
        if (blur) {
            result.add(0, new BlurTransformation(NSFW_BLUR));
        }
        //noinspection unchecked
        return result.toArray(new Transformation[0]);
    }

    public static int getCornerRadius(Context context) {
        // Get Style ID
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.shapeAppearanceCornerLarge, typedValue, true);

        // Get Style
        Resources.Theme theme = context.getResources().newTheme();
        theme.applyStyle(typedValue.resourceId, true);

        // Get Corner Radius
        theme.resolveAttribute(com.google.android.material.R.attr.cornerSize, typedValue, true);
        return (int) typedValue.getDimension(context.getResources().getDisplayMetrics());
    }

    public static Drawable createThumbnailBackground(Context context) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.BLACK);
        background.setCornerRadius(getCornerRadius(context));
        return background;
    }

    public static Drawable createThumbnailForeground(Context context) {
        // Get Color
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorControlHighlight, typedValue, true);
        @ColorInt int color = ContextCompat.getColor(context, typedValue.resourceId);

        // Get Mask
        Drawable mask = createThumbnailBackground(context);

        // Create
        return new RippleDrawable(ColorStateList.valueOf(color), null, mask);
    }

    public static Drawable createPlaceholder(Context context) {
        Drawable drawable = AppCompatResources.getDrawable(context, R.drawable.baseline_image_24);
        ScaleDrawable placeholder = new ScaleDrawable(drawable, Gravity.CENTER, 0.5f, 0.5f);
        // https://stackoverflow.com/a/5531592
        placeholder.setLevel(1);
        return placeholder;
    }

    public static Drawable createThumbnailHintBackground(Context context) {
        // Get Color
        @ColorInt int color = ContextCompat.getColor(context, R.color.thumbnail_hint_background);

        // Create Drawable
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        int cornerRadius = getCornerRadius(context);
        float[] corners = {0, 0, 0, 0, cornerRadius, cornerRadius, cornerRadius, cornerRadius};
        background.setCornerRadii(corners);
        return background;
    }
}
