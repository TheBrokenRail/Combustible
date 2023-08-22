package com.thebrokenrail.combustible.activity.feed.tabbed.saved;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.post.PostFeedAdapter;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.GetPosts;
import com.thebrokenrail.combustible.api.method.ListingType;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.api.method.SortType;
import com.thebrokenrail.combustible.util.Util;

import java.util.List;
import java.util.function.Consumer;

class SavedPostFeedAdapter extends PostFeedAdapter {
    SavedPostFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider, String viewModelKey) {
        super(parent, connection, viewModelProvider, viewModelKey, false, -1);
        sortBy = SortType.New;
    }

    @Override
    protected boolean hasListingTypeSort() {
        return false;
    }

    @Override
    protected void loadPage(int page, Consumer<List<PostView>> successCallback, Runnable errorCallback) {
        GetPosts method = new GetPosts();
        method.page = page;
        method.limit = Util.ELEMENTS_PER_PAGE;
        method.sort = sortBy;
        method.type_ = ListingType.All;
        method.saved_only = true;
        connection.send(method, getPostsResponse -> successCallback.accept(getPostsResponse.posts), errorCallback);
    }

    @Override
    protected boolean isBlocked(PostView element) {
        return !element.saved;
    }

    @Override
    protected PinMode getPinMode() {
        return PinMode.NONE;
    }

    @Override
    protected boolean showBanner() {
        return false;
    }

    @Override
    protected boolean useDefaultSort() {
        return false;
    }
}
