package com.thebrokenrail.combustible.activity.feed.tabbed.user;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.post.BasePostFeedAdapter;
import com.thebrokenrail.combustible.activity.feed.post.PostContext;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.GetPersonDetails;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.api.method.SortType;
import com.thebrokenrail.combustible.util.Util;

import java.util.List;
import java.util.function.Consumer;

class UserPostFeedAdapter extends BasePostFeedAdapter {
    private final int user;

    UserPostFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider, int user) {
        super(parent, connection, viewModelProvider);
        this.user = user;
        sorting.set(SortType.New);
    }

    @Override
    protected void loadPage(int page, Consumer<List<PostView>> successCallback, Runnable errorCallback) {
        GetPersonDetails method = new GetPersonDetails();
        method.page = page;
        method.limit = Util.ELEMENTS_PER_PAGE;
        method.sort = sorting.get(SortType.class);
        method.person_id = user;
        connection.send(method, getPostsResponse -> successCallback.accept(getPostsResponse.posts), errorCallback);
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
    protected PostContext.PinMode getPinMode() {
        return PostContext.PinMode.NONE;
    }

    @Override
    protected boolean showBanner() {
        return false;
    }

    @Override
    protected boolean isSortingTypeVisible(Class<? extends Enum<?>> type) {
        return type == SortType.class;
    }
}
