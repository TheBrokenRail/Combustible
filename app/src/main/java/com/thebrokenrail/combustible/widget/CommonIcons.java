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
    private final AppCompatImageView distinguished;
    private final AppCompatImageView unread;
    public final AppCompatImageView overflow;

    public CommonIcons(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Setup
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        // Icons
        deleted = addIcon(R.drawable.baseline_delete_18, R.string.icon_deleted);
        nsfw = addIcon(R.drawable.baseline_warning_18, R.string.icon_nsfw);
        locked = addIcon(R.drawable.baseline_lock_18, R.string.icon_locked);
        pinned = addIcon(R.drawable.baseline_push_pin_18, R.string.icon_pinned);
        distinguished = addIcon(R.drawable.baseline_mic_18, R.string.icon_distinguished);
        unread = addIcon(R.drawable.baseline_notifications_18, R.string.icon_unread);

        // Overflow
        overflow = addIcon(R.drawable.baseline_more_vert_24, R.string.overflow);
        overflow.setClickable(true);
        overflow.setFocusable(true);
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.controlBackground, typedValue, true);
        overflow.setBackgroundResource(typedValue.resourceId);
    }

    private AppCompatImageView addIcon(@DrawableRes int icon, @StringRes int contentDescription) {
        // Construct
        AppCompatImageView view = new AppCompatImageView(getContext());

        // Layout
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(layoutParams);

        // Padding
        int padding = getContext().getResources().getDimensionPixelSize(R.dimen.feed_item_margin);
        boolean includeEndPadding = contentDescription == R.string.overflow;
        view.setPaddingRelative(padding, padding, includeEndPadding ? padding : 0, padding);

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
     * @param isPinned If the post is pinned
     * @param isDistinguished If the comment is distinguished
     * @param isUnread If the private message is unread
     */
    public void setup(boolean isDeleted, boolean isNsfw, boolean isLocked, boolean isPinned, boolean isDistinguished, boolean isUnread) {
        setVisible(deleted, isDeleted);
        setVisible(nsfw, isNsfw);
        setVisible(locked, isLocked);
        setVisible(pinned, isPinned);
        setVisible(distinguished, isDistinguished);
        setVisible(unread, isUnread);
    }
}
