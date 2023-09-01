package com.thebrokenrail.combustible.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.post.PostFeedActivity;
import com.thebrokenrail.combustible.activity.feed.tabbed.user.UserFeedActivity;
import com.thebrokenrail.combustible.api.method.Community;
import com.thebrokenrail.combustible.api.method.Person;
import com.thebrokenrail.combustible.util.Names;

import java.time.Duration;
import java.time.Instant;

/**
 * Widget that displays the creator and/or community of a post/comment, along with its published/edited time.
 */
public class Metadata extends TableLayout {
    private final LinkWithIcon creator;
    private final LinkWithIcon community;
    private final AppCompatTextView time;

    public Metadata(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Inner Layout
        TableRow inner = new TableRow(context);
        inner.setGravity(Gravity.CENTER_VERTICAL);
        inner.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(inner);

        // Creator
        creator = new LinkWithIcon(context, null);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        creator.setLayoutParams(layoutParams);
        inner.addView(creator);

        // Spacer
        AppCompatTextView spacer = new AppCompatTextView(context);
        spacer.setText(R.string.post_creator_community_spacer);
        spacer.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.link_with_icon_font_size));
        spacer.setMaxLines(1);
        layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        int margin = context.getResources().getDimensionPixelSize(R.dimen.feed_item_margin);
        layoutParams.setMarginEnd(margin);
        layoutParams.setMarginStart(margin);
        spacer.setLayoutParams(layoutParams);
        inner.addView(spacer);

        // Community
        community = new LinkWithIcon(context, null);
        layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        community.setLayoutParams(layoutParams);
        inner.addView(community);

        // Second Spacer
        AppCompatTextView spacer2 = new AppCompatTextView(context);
        spacer2.setText(R.string.post_community_time_spacer);
        spacer2.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.link_with_icon_font_size));
        spacer2.setMaxLines(1);
        layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        layoutParams.setMarginEnd(margin);
        layoutParams.setMarginStart(margin);
        spacer2.setLayoutParams(layoutParams);
        inner.addView(spacer2);

        // Time
        time = new AppCompatTextView(context);
        time.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.link_with_icon_font_size));
        time.setMaxLines(1);
        layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        time.setLayoutParams(layoutParams);
        inner.addView(time);

        // Shrink Columns
        setColumnShrinkable(0, true);
        setColumnShrinkable(2, true);
    }

    /**
     * Setup the widget.
     * @param creatorView The creator to be displayed (or null to hide creator information)
     * @param communityView The community to be displayed (or null to hide community information)
     * @param timeView The time to be displayed
     * @param isEdited If the time should be marked as edited
     * @param blurNsfw If NSFW content should be blurred
     * @param showAvatars Show avatars
     */
    public void setup(Person creatorView, Community communityView, String timeView, boolean isEdited, boolean blurNsfw, boolean showAvatars) {
        // Creator
        boolean creatorVisible = creatorView != null;
        setColumnCollapsed(0, !creatorVisible);
        if (creatorVisible) {
            creator.setup(showAvatars ? creatorView.avatar : null, false, Names.getPersonTitle(creatorView), () -> {
                Context context = getContext();
                Intent intent = new Intent(context, UserFeedActivity.class);
                intent.putExtra(UserFeedActivity.USER_ID_EXTRA, creatorView.id);
                context.startActivity(intent);
            });
        } else {
            creator.setup(null, false, "", null);
        }

        // Community
        boolean communityVisible = communityView != null;
        setColumnCollapsed(2, !communityVisible);
        if (communityVisible) {
            community.setup(showAvatars ? communityView.icon : null, blurNsfw && communityView.nsfw, Names.getCommunityTitle(communityView), () -> {
                Context context = getContext();
                Intent intent = new Intent(context, PostFeedActivity.class);
                intent.putExtra(PostFeedActivity.COMMUNITY_ID_EXTRA, communityView.id);
                context.startActivity(intent);
            });
        } else {
            community.setup(null, false, "", null);
        }

        // Spacer
        setColumnCollapsed(1, !creatorVisible || !communityVisible);

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
