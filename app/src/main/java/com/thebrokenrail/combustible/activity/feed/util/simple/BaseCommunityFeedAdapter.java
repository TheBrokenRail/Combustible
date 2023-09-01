package com.thebrokenrail.combustible.activity.feed.util.simple;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.post.PostFeedActivity;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.Community;
import com.thebrokenrail.combustible.util.Names;
import com.thebrokenrail.combustible.widget.CommonIcons;

public abstract class BaseCommunityFeedAdapter extends SimpleFeedAdapter<Community> {
    public BaseCommunityFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider) {
        super(parent, connection, viewModelProvider);
    }

    @Override
    protected String getName(Community obj) {
        return Names.getCommunityTitle(obj);
    }

    @Override
    protected String getIcon(Community obj) {
        return obj.icon;
    }

    @Override
    protected void click(Context context, Community obj) {
        Intent intent = new Intent(context, PostFeedActivity.class);
        intent.putExtra(PostFeedActivity.COMMUNITY_ID_EXTRA, obj.id);
        context.startActivity(intent);
    }

    @Override
    protected void setupIcons(CommonIcons icons, Community obj) {
        icons.setup(false, obj.nsfw, false, false, false, false);
        icons.overflow.setOnClickListener(v -> new CommunityOverflow(v, connection, obj) {
            @Override
            protected void update(Community newObj) {
                viewModel.dataset.replace(notifier, obj, newObj);
            }
        });
    }
}
