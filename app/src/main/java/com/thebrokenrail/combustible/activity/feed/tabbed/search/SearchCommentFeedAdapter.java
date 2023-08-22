package com.thebrokenrail.combustible.activity.feed.tabbed.search;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.comment.BaseCommentFeedAdapter;
import com.thebrokenrail.combustible.activity.feed.util.dataset.FeedDataset;
import com.thebrokenrail.combustible.activity.feed.util.dataset.SimpleFeedDataset;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.ListingType;
import com.thebrokenrail.combustible.api.method.Search;
import com.thebrokenrail.combustible.api.method.SortType;
import com.thebrokenrail.combustible.util.Util;

import java.util.List;
import java.util.function.Consumer;

class SearchCommentFeedAdapter extends BaseCommentFeedAdapter {
    private final String query;

    SearchCommentFeedAdapter(View recyclerView, Connection connection, ViewModelProvider viewModelProvider, String query) {
        super(recyclerView, connection, viewModelProvider);
        this.query = query;
    }

    @Override
    protected void loadPage(int page, Consumer<List<CommentView>> successCallback, Runnable errorCallback) {
        Search method = new Search();
        method.page = page;
        method.limit = Util.ELEMENTS_PER_PAGE;
        method.sort = sorting.get(SortType.class);
        method.listing_type = sorting.get(ListingType.class);
        method.q = query;
        connection.send(method, getCommentsResponse -> successCallback.accept(getCommentsResponse.comments), errorCallback);
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
}
