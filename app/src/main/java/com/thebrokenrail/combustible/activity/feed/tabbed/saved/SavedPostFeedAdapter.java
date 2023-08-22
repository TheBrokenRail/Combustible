package com.thebrokenrail.combustible.activity.feed.tabbed.saved;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.post.BasePostFeedAdapter;
import com.thebrokenrail.combustible.activity.feed.post.PostContext;
import com.thebrokenrail.combustible.activity.feed.util.dataset.FeedDataset;
import com.thebrokenrail.combustible.activity.feed.util.dataset.SimpleFeedDataset;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.GetPosts;
import com.thebrokenrail.combustible.api.method.ListingType;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.api.method.SortType;
import com.thebrokenrail.combustible.util.Util;

import java.util.List;
import java.util.function.Consumer;

class SavedPostFeedAdapter extends BasePostFeedAdapter {
    SavedPostFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider) {
        super(parent, connection, viewModelProvider);
        sorting.set(SortType.New);
    }

    @Override
    protected boolean isSortingTypeVisible(Class<? extends Enum<?>> type) {
        return type == SortType.class;
    }

    @Override
    protected void loadPage(int page, Consumer<List<PostView>> successCallback, Runnable errorCallback) {
        GetPosts method = new GetPosts();
        method.page = page;
        method.limit = Util.ELEMENTS_PER_PAGE;
        method.sort = sorting.get(SortType.class);
        method.type_ = ListingType.All;
        method.saved_only = true;
        connection.send(method, getPostsResponse -> successCallback.accept(getPostsResponse.posts), errorCallback);
    }

    @Override
    protected FeedDataset<PostView> createDataset() {
        return new SimpleFeedDataset<PostView>() {
            @Override
            protected boolean isBlocked(PostView element) {
                return !element.saved || super.isBlocked(element);
            }
        };
    }

    @Override
    protected PostContext.PinMode getPinMode() {
        return PostContext.PinMode.NONE;
    }

    @Override
    protected boolean showBanner() {
        return false;
    }

    @Override
    protected boolean showCreator() {
        return true;
    }

    @Override
    protected boolean showCommunity() {
        return true;
    }
}
