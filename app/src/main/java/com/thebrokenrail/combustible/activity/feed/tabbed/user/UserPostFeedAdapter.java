package com.thebrokenrail.combustible.activity.feed.tabbed.user;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.post.PostFeedAdapter;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.GetPersonDetails;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.api.method.SortType;
import com.thebrokenrail.combustible.util.Util;

import java.util.List;
import java.util.function.Consumer;

class UserPostFeedAdapter extends PostFeedAdapter {
    private final int user;

    UserPostFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider, String viewModelKey, int user) {
        super(parent, connection, viewModelProvider, viewModelKey, false, -1);
        this.user = user;
        sortBy = SortType.New;
    }

    @Override
    protected boolean hasListingTypeSort() {
        return false;
    }

    @Override
    protected void loadPage(int page, Consumer<List<PostView>> successCallback, Runnable errorCallback) {
        GetPersonDetails method = new GetPersonDetails();
        method.page = page;
        method.limit = Util.ELEMENTS_PER_PAGE;
        method.sort = sortBy;
        method.person_id = user;
        connection.send(method, getPostsResponse -> successCallback.accept(getPostsResponse.posts), errorCallback);
    }

    @Override
    protected boolean showCreator() {
        return false;
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
