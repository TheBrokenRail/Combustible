package com.thebrokenrail.combustible.activity.feed.tabbed.search;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.util.adapter.simple.BaseCommunityFeedAdapter;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.Community;
import com.thebrokenrail.combustible.api.method.CommunityView;
import com.thebrokenrail.combustible.api.method.ListingType;
import com.thebrokenrail.combustible.api.method.Search;
import com.thebrokenrail.combustible.api.method.SortType;
import com.thebrokenrail.combustible.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SearchCommunityFeedAdapter extends BaseCommunityFeedAdapter {
    private final String query;

    public SearchCommunityFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider, String query) {
        super(parent, connection, viewModelProvider);
        this.query = query;
    }

    @Override
    protected void loadPage(int page, Consumer<List<Community>> successCallback, Runnable errorCallback) {
        Search method = new Search();
        method.page = page;
        method.limit = Util.ELEMENTS_PER_PAGE;
        method.sort = sorting.get(SortType.class);
        method.listing_type = sorting.get(ListingType.class);
        method.q = query;
        connection.send(method, searchResponse -> {
            List<Community> communities = new ArrayList<>();
            for (CommunityView communityView : searchResponse.communities) {
                communities.add(communityView.community);
            }
            successCallback.accept(communities);
        }, errorCallback);
    }

    @Override
    protected boolean isSortingTypeVisible(Class<? extends Enum<?>> type) {
        return type == SortType.class || type == ListingType.class;
    }
}
