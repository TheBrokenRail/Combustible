package com.thebrokenrail.combustible.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.api.method.GetCommunityResponse;
import com.thebrokenrail.combustible.api.method.GetPersonDetailsResponse;
import com.thebrokenrail.combustible.api.method.GetSiteResponse;
import com.thebrokenrail.combustible.util.glide.GlideApp;
import com.thebrokenrail.combustible.util.glide.GlideUtil;

import java.util.Objects;

/**
 * Utility class for displaying avatars in the top app bar.
 */
public class AppBarAvatar extends CustomTarget<Drawable> {
    private final Context context;
    private final ActionBar actionBar;

    private final boolean isRtl;
    private final int margin;

    private final Class<? extends FeedPrerequisite<?>> mode;

    private boolean shouldLoad = false;
    private String url = null;
    private boolean isNsfw = false;
    private boolean shouldBlurNsfw = false;

    private AppBarAvatar(AppCompatActivity activity, Class<? extends FeedPrerequisite<?>> mode, int size) {
        super(size, size);
        this.context = activity;
        this.actionBar = Objects.requireNonNull(activity.getSupportActionBar());
        this.mode = mode;
        margin = activity.getResources().getDimensionPixelSize(R.dimen.app_bar_avatar_margin);
        isRtl = activity.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }
    public AppBarAvatar(AppCompatActivity activity, Class<? extends FeedPrerequisite<?>> mode) {
        this(activity, mode, activity.getResources().getDimensionPixelSize(R.dimen.app_bar_avatar_size));
    }

    /**
     * Hook utility class into {@link FeedPrerequisites}.
     * @param prerequisites Prerequisite tracker object
     */
    public void handlePrerequisites(FeedPrerequisites prerequisites) {
        prerequisites.require(FeedPrerequisite.Site.class);
        prerequisites.require(mode);
        prerequisites.listen((prerequisite, isRefreshing) -> {
            // Check Permissions
            if (prerequisite instanceof FeedPrerequisite.Site) {
                GetSiteResponse getSiteResponse = ((FeedPrerequisite.Site) prerequisite).get();
                if (getSiteResponse.my_user != null) {
                    shouldLoad = getSiteResponse.my_user.local_user_view.local_user.show_avatars;
                } else {
                    shouldLoad = true;
                }
                shouldBlurNsfw = Images.shouldBlurNsfw(getSiteResponse);
                // Trigger Load If Possible
                updateAvatar();
            }
            if (prerequisite.getClass() == mode) {
                // Retrieve URL
                boolean updateNeeded = false;
                if (prerequisite instanceof FeedPrerequisite.Community) {
                    // Community Avatar
                    GetCommunityResponse getCommunityResponse = ((FeedPrerequisite.Community) prerequisite).get();
                    url = getCommunityResponse.community_view.community.icon;
                    isNsfw = getCommunityResponse.community_view.community.nsfw;
                    updateNeeded = true;
                } else if (prerequisite instanceof FeedPrerequisite.User) {
                    // User Avatar
                    GetPersonDetailsResponse getPersonDetailsResponse = ((FeedPrerequisite.User) prerequisite).get();
                    url = getPersonDetailsResponse.person_view.person.avatar;
                    updateNeeded = true;
                } else if (prerequisite instanceof FeedPrerequisite.Site) {
                    // Site Avatar
                    GetSiteResponse getSiteResponse = ((FeedPrerequisite.Site) prerequisite).get();
                    url = getSiteResponse.site_view.site.icon;
                    updateNeeded = true;
                }
                // Trigger Load If Possible
                if (updateNeeded) {
                    updateAvatar();
                }
            }
        });
    }

    private void updateAvatar() {
        // Check If Avatar Can Be Loaded
        boolean canLoad = shouldLoad && url != null;
        RequestManager requestManager = GlideApp.with(context);
        if (canLoad) {
            // Load
            boolean blur = isNsfw && shouldBlurNsfw;
            GlideUtil.load(context, requestManager, url, new CircleCrop(), 0, blur, true, null, this);
        } else {
            requestManager.clear(this);
        }
    }

    private void setEnabled(boolean enabled) {
        actionBar.setDisplayUseLogoEnabled(enabled);
        actionBar.setDisplayShowHomeEnabled(enabled);
    }

    @Override
    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
        // Add Margin
        int left = 0;
        int right = 0;
        if (isRtl) {
            left = margin;
        } else {
            right = margin;
        }
        resource = new InsetDrawable(resource, left, 0, right, 0);

        // Set Resource
        setEnabled(true);
        actionBar.setLogo(resource);
    }

    @Override
    public void onLoadCleared(@Nullable Drawable placeholder) {
        setEnabled(false);
        actionBar.setLogo(null);
    }
}
