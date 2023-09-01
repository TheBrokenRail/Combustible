package com.thebrokenrail.combustible.activity.feed.tabbed.user;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.util.simple.BaseCommunityFeedAdapter;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.Community;
import com.thebrokenrail.combustible.api.method.CommunityFollowerView;
import com.thebrokenrail.combustible.api.method.GetSite;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UserSubscribedCommunitiesFeedAdapter extends BaseCommunityFeedAdapter {
    public UserSubscribedCommunitiesFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider) {
        super(parent, connection, viewModelProvider);
    }

    @Override
    protected void loadPage(int page, Consumer<List<Community>> successCallback, Runnable errorCallback) {
        if (connection.hasToken()) {
            GetSite method = new GetSite();
            connection.send(method, getSiteResponse -> {
                if (getSiteResponse.my_user != null) {
                    List<Community> communities = new ArrayList<>();
                    for (CommunityFollowerView communityFollowerView : getSiteResponse.my_user.follows) {
                        communities.add(communityFollowerView.community);
                    }
                    successCallback.accept(communities);
                } else {
                    errorCallback.run();
                }
            }, errorCallback);
        } else {
            errorCallback.run();
        }
    }

    @Override
    protected boolean isSortingTypeVisible(Class<? extends Enum<?>> type) {
        return false;
    }

    @Override
    protected boolean hasHeader() {
        return false;
    }

    @Override
    protected boolean isSinglePage() {
        return true;
    }
}
