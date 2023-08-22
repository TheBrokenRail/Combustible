package com.thebrokenrail.combustible.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.thebrokenrail.combustible.R;

public class LinkWithIcon extends LinearLayout {
    private final ImageView icon;
    private final TextView text;
    private Runnable onClick = null;

    public LinkWithIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        // Icon
        this.icon = new AppCompatImageView(context);
        int iconSize = context.getResources().getDimensionPixelSize(R.dimen.link_with_icon_size);
        LayoutParams layoutParams = new LayoutParams(iconSize, iconSize);
        layoutParams.setMarginEnd(context.getResources().getDimensionPixelSize(R.dimen.post_padding));
        icon.setLayoutParams(layoutParams);
        addView(icon);

        // Text
        this.text = new AppCompatTextView(context);
        layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        text.setLayoutParams(layoutParams);
        text.setTypeface(null, Typeface.BOLD);
        text.setMaxLines(1);
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.link_with_icon_font_size));
        text.setEllipsize(TextUtils.TruncateAt.END);
        addView(text);

        // On Click
        setClickable(true);
        setFocusable(true);
        setOnClickListener(v -> {
            if (onClick != null) {
                onClick.run();
            }
        });

        // Ripple Effect
        int[] rippleAttrs = new int[]{android.R.attr.selectableItemBackground};
        TypedArray typedArray = context.obtainStyledAttributes(rippleAttrs);
        setBackgroundResource(typedArray.getResourceId(0, 0));
        typedArray.recycle();
    }

    public void setup(String iconUrl, String newText, Runnable onClick) {
        // Icon
        if (iconUrl != null) {
            icon.setVisibility(VISIBLE);

            // Load Image
            Glide.with(getContext())
                    .load(iconUrl)
                    .circleCrop()
                    .placeholder(R.drawable.baseline_image_24)
                    .into(icon);
        } else {
            icon.setVisibility(GONE);
            Glide.with(getContext()).clear(icon);
        }

        // Text
        text.setText(newText);

        // Set Callback
        this.onClick = onClick;
    }
}
