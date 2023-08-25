package com.thebrokenrail.combustible.activity.feed.util.simple;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.post.PostFeedActivity;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommunityView;
import com.thebrokenrail.combustible.util.Names;
import com.thebrokenrail.combustible.widget.CommonIcons;

public abstract class BaseCommunityFeedAdapter extends SimpleFeedAdapter<CommunityView> {
    public BaseCommunityFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider) {
        super(parent, connection, viewModelProvider);
    }

    @Override
    protected String getName(CommunityView obj) {
        return Names.getCommunityTitle(obj.community);
    }

    @Override
    protected String getIcon(CommunityView obj) {
        return obj.community.icon;
    }

    @Override
    protected void click(Context context, CommunityView obj) {
        Intent intent = new Intent(context, PostFeedActivity.class);
        intent.putExtra(PostFeedActivity.COMMUNITY_ID_EXTRA, obj.community.id);
        context.startActivity(intent);
    }

    @Override
    protected void setupIcons(CommonIcons icons, CommunityView obj) {
        icons.setup(false, obj.community.nsfw, false, false, false, false);
        icons.overflow.setOnClickListener(v -> new CommunityOverflow(v, connection, obj) {
            @Override
            protected void update(CommunityView newObj) {
                viewModel.dataset.replace(notifier, obj, newObj);
            }
        });
    }
}
