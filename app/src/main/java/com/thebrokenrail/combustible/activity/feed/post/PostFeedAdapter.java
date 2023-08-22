package com.thebrokenrail.combustible.activity.feed.post;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.GetCommunityResponse;
import com.thebrokenrail.combustible.api.method.GetPosts;
import com.thebrokenrail.combustible.api.method.ListingType;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.api.method.SortType;
import com.thebrokenrail.combustible.util.Util;

import java.util.List;
import java.util.function.Consumer;

class PostFeedAdapter extends BasePostFeedAdapter {
    private final Integer communityId;

    public PostFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider, Integer communityId) {
        super(parent, connection, viewModelProvider);
        this.communityId = communityId;
    }

    @Override
    protected void handlePrerequisites(FeedPrerequisites prerequisites) {
        super.handlePrerequisites(prerequisites);
        if (showBanner() && communityId != null) {
            prerequisites.require(FeedPrerequisite.Community.class);
        }
        prerequisites.listen(prerequisite -> {
            boolean reloadHeader = false;
            if (prerequisite instanceof FeedPrerequisite.Site) {
                // Site Loaded
                assert site != null;

                // Banner
                if (showBanner() && communityId == null) {
                    setBannerUrl(site.site_view.site.banner, false);
                    reloadHeader = true;
                }
            } else if (showBanner() && communityId != null && prerequisite instanceof FeedPrerequisite.Community) {
                // Banner
                GetCommunityResponse getCommunityResponse = ((FeedPrerequisite.Community) prerequisite).get();
                reloadHeader = setBannerUrl(getCommunityResponse.community_view.community.banner, getCommunityResponse.community_view.community.nsfw);
            }
            if (reloadHeader) {
                // Reload Header
                notifyItemChanged(0);
            }
        });
    }

    @Override
    protected boolean showBanner() {
        return true;
    }

    @Override
    protected boolean showCreator() {
        return true;
    }

    @Override
    protected boolean showCommunity() {
        return communityId == null;
    }

    @Override
    protected PostContext.PinMode getPinMode() {
        if (communityId != null) {
            return PostContext.PinMode.COMMUNITY;
        } else {
            return PostContext.PinMode.HOME;
        }
    }

    @Override
    protected void loadPage(int page, Consumer<List<PostView>> successCallback, Runnable errorCallback) {
        GetPosts method = new GetPosts();
        method.page = page;
        method.limit = Util.ELEMENTS_PER_PAGE;
        method.sort = sorting.get(SortType.class);
        if (isSortingTypeVisible(ListingType.class)) {
            method.type_ = sorting.get(ListingType.class);
        } else {
            method.type_ = ListingType.All;
            method.community_id = communityId;
        }
        connection.send(method, getPostsResponse -> successCallback.accept(getPostsResponse.posts), errorCallback);
    }

    @Override
    protected boolean isSortingTypeVisible(Class<? extends Enum<?>> type) {
        if (type == ListingType.class) {
            return communityId == null;
        } else {
            return type == SortType.class;
        }
    }

    @Override
    protected boolean useDefaultSort() {
        return true;
    }
}
