package com.thebrokenrail.combustible.activity.feed.tabbed.user;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.comment.FlatCommentFeedAdapter;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.GetPersonDetails;
import com.thebrokenrail.combustible.api.method.SortType;
import com.thebrokenrail.combustible.util.Util;

import java.util.List;
import java.util.function.Consumer;

class UserCommentFeedAdapter extends FlatCommentFeedAdapter {
    private final int user;

    UserCommentFeedAdapter(View recyclerView, Connection connection, ViewModelProvider viewModelProvider, String viewModelKey, int user) {
        super(recyclerView, connection, viewModelProvider, viewModelKey, null, -1);
        this.user = user;
        sortingMethod = new SortingMethod() {
            @Override
            public Enum<?> get(int position) {
                return SortType.values()[position];
            }

            @Override
            public int values() {
                return R.array.sort_types;
            }
        };
        sortBy = SortType.New;
    }

    @Override
    protected void loadPage(int page, Consumer<List<CommentView>> successCallback, Runnable errorCallback) {
        GetPersonDetails method = new GetPersonDetails();
        method.page = page;
        method.limit = Util.ELEMENTS_PER_PAGE;
        method.sort = (SortType) sortBy;
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
}
