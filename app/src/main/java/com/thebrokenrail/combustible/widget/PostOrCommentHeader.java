package com.thebrokenrail.combustible.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

/**
 * Widget that displays a post or comment's header.
 * This consists of a {@link RelativeLayout} placed inside of a (seemingly redundant) {@link LinearLayoutCompat} to workaround an <a href="https://stackoverflow.com/a/33997626/16198887">issue</a>.
 */
public class PostOrCommentHeader extends LinearLayoutCompat {
    public final Metadata metadata;
    public final CommonIcons icons;

    public PostOrCommentHeader(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Inner Layout
        RelativeLayout inner = new RelativeLayout(context);
        LayoutParams innerLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        inner.setLayoutParams(innerLayoutParams);
        addView(inner);

        // Metadata
        int iconsId = View.generateViewId();
        metadata = new Metadata(context, null);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        layoutParams.addRule(RelativeLayout.START_OF, iconsId);
        metadata.setLayoutParams(layoutParams);
        inner.addView(metadata);

        // Icons
        icons = new CommonIcons(context, null);
        icons.setId(iconsId);
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        icons.setLayoutParams(layoutParams);
        inner.addView(icons);
    }
}
