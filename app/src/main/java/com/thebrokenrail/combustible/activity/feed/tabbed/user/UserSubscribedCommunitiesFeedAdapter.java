package com.thebrokenrail.combustible.activity.feed.tabbed.user;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.util.simple.BaseCommunityFeedAdapter;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.Community;
import com.thebrokenrail.combustible.api.method.CommunityView;
import com.thebrokenrail.combustible.api.method.ListCommunities;
import com.thebrokenrail.combustible.api.method.ListingType;
import com.thebrokenrail.combustible.api.method.SortType;
import com.thebrokenrail.combustible.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UserSubscribedCommunitiesFeedAdapter extends BaseCommunityFeedAdapter {
    private final int user;

    public UserSubscribedCommunitiesFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider, int user) {
        super(parent, connection, viewModelProvider);
        this.user = user;
    }

    @Override
    protected void loadPage(int page, Consumer<List<Community>> successCallback, Runnable errorCallback) {
        if (site.my_user != null && site.my_user.local_user_view.person.id == user) {
            ListCommunities method = new ListCommunities();
            method.page = page;
            method.limit = Util.ELEMENTS_PER_PAGE;
            method.sort = sorting.get(SortType.class);
            method.type_ = ListingType.Subscribed;
            connection.send(method, listCommunitiesResponse -> {
                List<Community> communities = new ArrayList<>();
                for (CommunityView communityView : listCommunitiesResponse.communities) {
                    communities.add(communityView.community);
                }
                successCallback.accept(communities);
            }, errorCallback);
        } else {
            errorCallback.run();
        }
    }

    @Override
    protected boolean isSortingTypeVisible(Class<? extends Enum<?>> type) {
        return type == SortType.class;
    }
}
