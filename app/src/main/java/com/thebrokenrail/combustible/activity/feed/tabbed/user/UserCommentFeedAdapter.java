package com.thebrokenrail.combustible.activity.feed.tabbed.user;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.comment.BaseCommentFeedAdapter;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.GetPersonDetails;
import com.thebrokenrail.combustible.api.method.SortType;
import com.thebrokenrail.combustible.util.Util;

import java.util.List;
import java.util.function.Consumer;

class UserCommentFeedAdapter extends BaseCommentFeedAdapter {
    private final int user;

    UserCommentFeedAdapter(View recyclerView, Connection connection, ViewModelProvider viewModelProvider, int user) {
        super(recyclerView, connection, viewModelProvider);
        this.user = user;
        sorting.set(SortType.New);
    }

    @Override
    protected void loadPage(int page, Consumer<List<CommentView>> successCallback, Runnable errorCallback) {
        GetPersonDetails method = new GetPersonDetails();
        method.page = page;
        method.limit = Util.ELEMENTS_PER_PAGE;
        method.sort = sorting.get(SortType.class);
        method.person_id = user;
        connection.send(method, getCommentsResponse -> successCallback.accept(getCommentsResponse.comments), errorCallback);
    }

    @Override
    protected boolean showCreator() {
        return false;
    }

    @Override
    protected boolean showCommunity() {
        return true;
    }

    @Override
    protected boolean isSortingTypeVisible(Class<? extends Enum<?>> type) {
        return type == SortType.class;
    }
}
