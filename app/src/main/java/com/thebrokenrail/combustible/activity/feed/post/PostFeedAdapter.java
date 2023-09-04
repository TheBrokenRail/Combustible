package com.thebrokenrail.combustible.activity.feed.post;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.create.PostCreateActivity;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.Community;
import com.thebrokenrail.combustible.api.method.GetCommunityResponse;
import com.thebrokenrail.combustible.api.method.GetPosts;
import com.thebrokenrail.combustible.api.method.ListingType;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.api.method.SortType;
import com.thebrokenrail.combustible.util.RequestCodes;
import com.thebrokenrail.combustible.util.Util;

import java.util.List;
import java.util.function.Consumer;

class PostFeedAdapter extends BasePostFeedAdapter {
    private final Integer communityId;

    private Community community;

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
        prerequisites.listen((prerequisite, isRefreshing) -> {
            boolean reloadHeader = false;
            if (prerequisite instanceof FeedPrerequisite.Site) {
                // Site Loaded
                assert site != null;
                reloadHeader = true;

                // Banner
                if (showBanner() && communityId == null) {
                    setBannerUrl(site.site_view.site.banner, false);
                }
            } else if (communityId != null && prerequisite instanceof FeedPrerequisite.Community) {
                // Community Loaded
                reloadHeader = true;

                // Banner
                GetCommunityResponse getCommunityResponse = ((FeedPrerequisite.Community) prerequisite).get();
                if (showBanner()) {
                    setBannerUrl(getCommunityResponse.community_view.community.banner, getCommunityResponse.community_view.community.nsfw);
                }

                // Store For Permissions
                community = getCommunityResponse.community_view.community;
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
            return PostContext.PinMode.INSTANCE;
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

    @Override
    protected void bindHeader(View root) {
        super.bindHeader(root);

        // Create Post
        boolean canPost = false;
        if (community != null) {
            canPost = permissions.canPost(community);
        }
        Button createPost = root.findViewById(R.id.posts_create);
        if (canPost) {
            createPost.setVisibility(View.VISIBLE);
            createPost.setOnClickListener(v -> {
                AppCompatActivity activity = Util.getActivityFromContext(v.getContext());
                Intent intent = new Intent(activity, PostCreateActivity.class);
                intent.putExtra(PostFeedActivity.COMMUNITY_ID_EXTRA, communityId);
                //noinspection deprecation
                activity.startActivityForResult(intent, RequestCodes.CREATE_POST);
            });
        } else {
            createPost.setVisibility(View.GONE);
        }
    }
}
