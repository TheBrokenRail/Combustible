package com.thebrokenrail.combustible.activity.feed.tabbed.saved;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.comment.BaseCommentFeedAdapter;
import com.thebrokenrail.combustible.activity.feed.util.dataset.FeedDataset;
import com.thebrokenrail.combustible.activity.feed.util.dataset.SimpleFeedDataset;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommentSortType;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.GetComments;
import com.thebrokenrail.combustible.api.method.ListingType;
import com.thebrokenrail.combustible.util.Util;

import java.util.List;
import java.util.function.Consumer;

class SavedCommentFeedAdapter extends BaseCommentFeedAdapter {
    SavedCommentFeedAdapter(View recyclerView, Connection connection, ViewModelProvider viewModelProvider) {
        super(recyclerView, connection, viewModelProvider);
        sorting.set(CommentSortType.New);
    }

    @Override
    protected boolean showCreator() {
        return true;
    }

    @Override
    protected boolean isSortingTypeVisible(Class<? extends Enum<?>> type) {
        return type == CommentSortType.class;
    }

    @Override
    protected void loadPage(int page, Consumer<List<CommentView>> successCallback, Runnable errorCallback) {
        GetComments method = new GetComments();
        method.page = page;
        method.limit = Util.ELEMENTS_PER_PAGE;
        method.sort = sorting.get(CommentSortType.class);
        method.saved_only = true;
        method.type_ = ListingType.All;
        connection.send(method, getCommentsResponse -> successCallback.accept(getCommentsResponse.comments), errorCallback);
    }

    @Override
    protected FeedDataset<CommentView> createDataset() {
        return new SimpleFeedDataset<CommentView>() {
            @Override
            protected boolean isBlocked(CommentView element) {
                return !element.saved || super.isBlocked(element);
            }
        };
    }

    @Override
    protected boolean showCommunity() {
        return true;
    }
}
