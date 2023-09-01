package com.thebrokenrail.combustible.activity.feed.tabbed.user;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.util.simple.BaseCommunityFeedAdapter;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.Community;
import com.thebrokenrail.combustible.api.method.CommunityModeratorView;
import com.thebrokenrail.combustible.api.method.GetPersonDetails;
import com.thebrokenrail.combustible.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UserModeratesCommunitiesFeedAdapter extends BaseCommunityFeedAdapter {
    private final int user;

    public UserModeratesCommunitiesFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider, int user) {
        super(parent, connection, viewModelProvider);
        this.user = user;
    }

    @Override
    protected void loadPage(int page, Consumer<List<Community>> successCallback, Runnable errorCallback) {
        GetPersonDetails method = new GetPersonDetails();
        method.limit = Util.MIN_LIMIT;
        method.person_id = user;
        connection.send(method, getPersonDetailsResponse -> {
            List<Community> communities = new ArrayList<>();
            for (CommunityModeratorView communityModeratorView : getPersonDetailsResponse.moderates) {
                communities.add(communityModeratorView.community);
            }
            successCallback.accept(communities);
        }, errorCallback);
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
