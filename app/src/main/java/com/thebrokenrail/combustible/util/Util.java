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
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.load.Transformation;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.api.method.Community;
import com.thebrokenrail.combustible.api.method.Person;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import jp.wasabeef.glide.transformations.BlurTransformation;
import okhttp3.HttpUrl;

public class Util {
    public static final int ELEMENTS_PER_PAGE = 40;
    public static final int MAX_DEPTH = 8;
    private static final int NSFW_BLUR = 36;

    public static void unknownError(Context context) {
        Toast.makeText(context.getApplicationContext(), R.string.unknown_error, Toast.LENGTH_SHORT).show();
    }

    public static String getCommunityTitle(Community community) {
        String name = community.title;
        if (!community.local) {
            HttpUrl url = HttpUrl.parse(community.actor_id);
            if (url != null) {
                name += '@' + url.host();
            }
        }
        return name;
    }

    public static String getCommunityName(Community community) {
        String name = community.name;
        if (!community.local) {
            HttpUrl url = HttpUrl.parse(community.actor_id);
            if (url != null) {
                name += '@' + url.host();
            }
        }
        return name;
    }

    public static String getPersonTitle(Person person) {
        if (person.display_name != null) {
            return person.display_name;
        } else {
            return '@' + getPersonName(person);
        }
    }

    public static String getPersonName(Person person) {
        String name = person.name;
        if (!person.local) {
            HttpUrl url = HttpUrl.parse(person.actor_id);
            if (url != null) {
                name += '@' + url.host();
            }
        }
        return name;
    }

    public static void showTextDialog(Context context, @StringRes int title, @StringRes int text) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(text)
                .setNeutralButton(R.string.ok, null)
                .show();
    }

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
}
