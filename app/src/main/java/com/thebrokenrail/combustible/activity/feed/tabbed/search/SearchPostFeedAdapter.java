package com.thebrokenrail.combustible.activity.feed.tabbed.search;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.post.BasePostFeedAdapter;
import com.thebrokenrail.combustible.activity.feed.post.PostContext;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.ListingType;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.api.method.Search;
import com.thebrokenrail.combustible.api.method.SearchType;
import com.thebrokenrail.combustible.api.method.SortType;
import com.thebrokenrail.combustible.util.Util;

import java.util.List;
import java.util.function.Consumer;

class SearchPostFeedAdapter extends BasePostFeedAdapter {
    private final String query;

    SearchPostFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider, String query) {
        super(parent, connection, viewModelProvider);
        this.query = query;
    }

    @Override
    protected void loadPage(int page, Consumer<List<PostView>> successCallback, Runnable errorCallback) {
        Search method = new Search();
        method.page = page;
        method.limit = Util.ELEMENTS_PER_PAGE;
        method.sort = sorting.get(SortType.class);
        method.listing_type = sorting.get(ListingType.class);
        method.q = query;
        method.type_ = SearchType.Posts;
        connection.send(method, getPostsResponse -> successCallback.accept(getPostsResponse.posts), errorCallback);
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

    @Override
    protected boolean isSortingTypeVisible(Class<? extends Enum<?>> type) {
        return type == SortType.class || type == ListingType.class;
    }

    @Override
    protected boolean useDefaultSort() {
        return true;
    }
}
