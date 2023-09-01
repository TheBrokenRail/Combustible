package com.thebrokenrail.combustible.activity.feed.tabbed.blocked;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.util.adapter.simple.BaseCommunityFeedAdapter;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.Community;
import com.thebrokenrail.combustible.api.method.CommunityBlockView;
import com.thebrokenrail.combustible.api.method.GetSite;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlockedCommunityFeedAdapter extends BaseCommunityFeedAdapter {
    public BlockedCommunityFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider) {
        super(parent, connection, viewModelProvider);
    }

    @Override
    protected void loadPage(int page, Consumer<List<Community>> successCallback, Runnable errorCallback) {
        GetSite method = new GetSite();
        connection.send(method, getSiteResponse -> {
            // Check
            if (getSiteResponse.my_user == null) {
                errorCallback.run();
                return;
            }

            // Copy Into Dataset
            List<Community> communities = new ArrayList<>();
            for (CommunityBlockView communityBlockView : getSiteResponse.my_user.community_blocks) {
                communities.add(communityBlockView.community);
            }
            successCallback.accept(communities);
        }, errorCallback);
    }

    @Override
    protected boolean isSortingTypeVisible(Class<? extends Enum<?>> type) {
        return false;
    }

    @Override
    protected boolean isSinglePage() {
        return true;
    }

    @Override
    protected boolean hasHeader() {
        return false;
    }
}
