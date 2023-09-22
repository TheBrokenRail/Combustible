package com.thebrokenrail.combustible.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.util.Images;
import com.thebrokenrail.combustible.util.Util;
import com.thebrokenrail.combustible.util.glide.GlideApp;
import com.thebrokenrail.combustible.util.glide.GlideUtil;

/**
 * Widget that displays a clickable link with a corresponding icon.
 */
public class LinkWithIcon extends LinearLayout {
    private final ImageView icon;
    private final TextView text;
    private Runnable onClick = null;

    public LinkWithIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        // Padding
        int padding = context.getResources().getDimensionPixelSize(R.dimen.feed_item_margin);
        setPadding(padding, padding, padding, padding);

        // Icon
        this.icon = new AppCompatImageView(context);
        addView(icon);

        // Text
        this.text = new AppCompatTextView(context);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        text.setLayoutParams(layoutParams);
        text.setTypeface(null, Typeface.BOLD);
        text.setMaxLines(1);
        text.setEllipsize(TextUtils.TruncateAt.END);
        addView(text);

        // Default Multiplier
        setSizeMultiplier(1);

        // On Click
        setOnClickListener(v -> {
            if (onClick != null) {
                onClick.run();
            }
        });
    }

    /**
     * Set size multiplier.
     * @param multiplier The new size multiplier
     */
    public void setSizeMultiplier(float multiplier) {
        // Text
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.link_with_icon_font_size) * multiplier);
        // Icon
        float iconSize = getResources().getDimensionPixelSize(R.dimen.link_with_icon_size) * multiplier;
        LayoutParams layoutParams = new LayoutParams((int) iconSize, (int) iconSize);
        layoutParams.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.feed_item_margin));
        icon.setLayoutParams(layoutParams);
    }

    /**
     * Setup the widget.
     * @param iconUrl The icon's URL
     * @param blur If the icon should be blurred
     * @param newText The link's text
     * @param onClick Callback that is executed on click
     */
    public void setup(String iconUrl, boolean blur, String newText, Runnable onClick) {
        // Icon
        RequestManager requestManager = GlideApp.with(getContext());
        if (iconUrl != null) {
            icon.setVisibility(VISIBLE);

            // Load Image
            String thumbnailUrl = Util.getThumbnailUrl(iconUrl);
            GlideUtil.load(getContext(), requestManager, thumbnailUrl, new CircleCrop(), 0, blur, true, Images.createThemedPlaceholder(getContext()), icon);
        } else {
            icon.setVisibility(GONE);
            requestManager.clear(icon);
        }

        // Text
        text.setText(newText);

        // Set Callback
        this.onClick = onClick;
        setClickable(onClick != null);
        setFocusable(onClick != null);

        // Ripple Effect
        if (onClick != null) {
            TypedValue typedValue = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
            setBackgroundResource(typedValue.resourceId);
        } else {
            setBackground(null);
        }
    }
}