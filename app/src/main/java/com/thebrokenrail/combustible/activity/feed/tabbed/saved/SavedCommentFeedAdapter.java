package com.thebrokenrail.combustible.activity.feed.tabbed.saved;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.comment.FlatCommentFeedAdapter;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommentSortType;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.GetComments;
import com.thebrokenrail.combustible.api.method.ListingType;
import com.thebrokenrail.combustible.util.Util;

import java.util.List;
import java.util.function.Consumer;

class SavedCommentFeedAdapter extends FlatCommentFeedAdapter {
    SavedCommentFeedAdapter(View recyclerView, Connection connection, ViewModelProvider viewModelProvider, String viewModelKey) {
        super(recyclerView, connection, viewModelProvider, viewModelKey, null, -1);
        sortBy = CommentSortType.New;
    }

    @Override
    protected void loadPage(int page, Consumer<List<CommentView>> successCallback, Runnable errorCallback) {
        GetComments method = new GetComments();
        method.page = page;
        method.limit = Util.ELEMENTS_PER_PAGE;
        method.sort = (CommentSortType) sortBy;
        method.saved_only = true;
        method.type_ = ListingType.All;
        connection.send(method, getCommentsResponse -> successCallback.accept(getCommentsResponse.comments), errorCallback);
    }

    @Override
    protected boolean isBlocked(CommentView element) {
        return !element.saved;
    }

    @Override
    protected boolean showCommunity() {
        return true;
    }
}
