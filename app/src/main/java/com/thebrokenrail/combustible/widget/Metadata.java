package com.thebrokenrail.combustible.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.post.PostFeedActivity;
import com.thebrokenrail.combustible.activity.feed.tabbed.user.UserFeedActivity;
import com.thebrokenrail.combustible.api.method.Community;
import com.thebrokenrail.combustible.api.method.Person;
import com.thebrokenrail.combustible.util.Util;

import java.time.Duration;
import java.time.Instant;

/**
 * Widget that displays the creator and/or community of a post/comment, along with its published/edited time.
 */
public class Metadata extends FrameLayout {
    private final LinkWithIcon creator;
    private final AppCompatTextView spacer;
    private final LinkWithIcon community;
    private final AppCompatTextView time;

    public Metadata(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Inner Layout
        LinearLayout inner = new LinearLayout(context);
        inner.setGravity(Gravity.CENTER_VERTICAL);
        inner.setOrientation(LinearLayout.HORIZONTAL);
        inner.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addView(inner);

        // Creator
        creator = new LinkWithIcon(context, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        creator.setLayoutParams(layoutParams);
        inner.addView(creator);

        // Spacer
        spacer = new AppCompatTextView(context);
        spacer.setText(R.string.post_creator_community_spacer);
        spacer.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.link_with_icon_font_size));
        spacer.setMaxLines(1);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = context.getResources().getDimensionPixelSize(R.dimen.post_padding);
        layoutParams.setMarginEnd(margin);
        layoutParams.setMarginStart(margin);
        spacer.setLayoutParams(layoutParams);
        inner.addView(spacer);

        // Community
        community = new LinkWithIcon(context, null);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        community.setLayoutParams(layoutParams);
        inner.addView(community);

        // Second Spacer
        AppCompatTextView spacer2 = new AppCompatTextView(context);
        spacer2.setText(R.string.post_community_time_spacer);
        spacer2.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.link_with_icon_font_size));
        spacer2.setMaxLines(1);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMarginEnd(margin);
        layoutParams.setMarginStart(margin);
        spacer2.setLayoutParams(layoutParams);
        inner.addView(spacer2);

        // Time
        time = new AppCompatTextView(context);
        time.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.link_with_icon_font_size));
        time.setMaxLines(1);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        time.setLayoutParams(layoutParams);
        inner.addView(time);
    }

    /**
     * Setup the widget.
     * @param creatorView The creator to be displayed (or null to hide creator information)
     * @param communityView The community to be displayed (or null to hide community information)
     * @param timeView The time to be displayed
     * @param isEdited If the time should be marked as edited
     * @param blurNsfw If NSFW content should be blurred
     */
    public void setup(Person creatorView, Community communityView, String timeView, boolean isEdited, boolean blurNsfw) {
        // Creator
        if (creatorView != null) {
            creator.setVisibility(VISIBLE);
            creator.setup(creatorView.avatar, false, Util.getPersonTitle(creatorView), () -> {
                Context context = getContext();
                Intent intent = new Intent(context, UserFeedActivity.class);
                intent.putExtra(UserFeedActivity.USER_ID_EXTRA, creatorView.id);
                context.startActivity(intent);
            });
        } else {
            creator.setVisibility(GONE);
        }

        // Spacer
        spacer.setVisibility((creatorView != null && communityView != null) ? VISIBLE : GONE);

        // Community
        if (communityView != null) {
            community.setVisibility(VISIBLE);
            community.setup(communityView.icon, blurNsfw && communityView.nsfw, Util.getCommunityTitle(communityView), () -> {
                Context context = getContext();
                Intent intent = new Intent(context, PostFeedActivity.class);
                intent.putExtra(PostFeedActivity.COMMUNITY_ID_EXTRA, communityView.id);
                context.startActivity(intent);
            });
        } else {
            community.setVisibility(GONE);
        }

        // Time
        String timeText;
        Instant timeInstant = Instant.parse(timeView + 'Z');
        Instant nowInstant = Instant.now();
        Duration duration = Duration.between(timeInstant, nowInstant);
        long days = duration.toDays();
        if (days >= 365) {
            // More Than A Year Old
            long years = days / 365;
            timeText = years + "y";
        } else if (days > 0) {
            // More Than A Day Old
            timeText = days + "d";
        } else {
            long seconds = duration.getSeconds();
            long minutes = seconds / 60;
            long hours = minutes / 60;
            if (hours > 0) {
                // More Than An Hour Old
                timeText = hours + "h";
            } else if (minutes > 0) {
                // More Than A Minute Old
                timeText = minutes + "m";
            } else {
                // Less Than A Minute Old
                timeText = seconds + "s";
            }
        }
        if (isEdited) {
            timeText += '*';
        }
        time.setText(timeText);
    }
}
