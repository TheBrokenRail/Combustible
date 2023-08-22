package com.thebrokenrail.combustible.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatImageView;

import com.thebrokenrail.combustible.R;

/**
 * Widget containing common icons for posts and comments.
 */
public class CommonIcons extends LinearLayout {
    private final AppCompatImageView deleted;
    private final AppCompatImageView nsfw;
    private final AppCompatImageView locked;
    private final AppCompatImageView pinned;
    public final AppCompatImageView overflow;

    public CommonIcons(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Setup
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        // Icons
        deleted = addIcon(R.drawable.baseline_delete_18, R.string.icon_deleted);
        nsfw = addIcon(R.drawable.baseline_warning_24, R.string.icon_nsfw);
        locked = addIcon(R.drawable.baseline_lock_18, R.string.icon_locked);
        pinned = addIcon(R.drawable.baseline_push_pin_18, R.string.icon_pinned);

        // Overflow
        overflow = addIcon(R.drawable.baseline_more_vert_24, R.string.overflow);
        overflow.setClickable(true);
        overflow.setFocusable(true);
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true);
        overflow.setBackgroundResource(typedValue.resourceId);
    }

    private AppCompatImageView addIcon(@DrawableRes int icon, @StringRes int contentDescription) {
        // Construct
        AppCompatImageView view = new AppCompatImageView(getContext());

        // Layout
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        int margin = getContext().getResources().getDimensionPixelSize(R.dimen.post_padding);
        layoutParams.setMarginStart(margin);
        view.setLayoutParams(layoutParams);

        // Setup
        view.setImageResource(icon);
        view.setContentDescription(getContext().getString(contentDescription));

        // Add
        addView(view);
        return view;
    }

    private static void setVisible(AppCompatImageView image, boolean visible) {
        image.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Setup the widget.
     * @param isDeleted If the post/comment is removed or deleted
     * @param isNsfw If the post is NSFW
     * @param isLocked If the post is locked
     * @param isPinned If the post/comment is pinned or distinguished
     */
    public void setup(boolean isDeleted, boolean isNsfw, boolean isLocked, boolean isPinned) {
        setVisible(deleted, isDeleted);
        setVisible(nsfw, isNsfw);
        setVisible(locked, isLocked);
        setVisible(pinned, isPinned);
    }
}
