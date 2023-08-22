package com.thebrokenrail.combustible.activity.feed.tabbed.blocked;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.tabbed.TabbedFeedActivity;

import java.util.Objects;

public class BlockedFeedActivity extends TabbedFeedActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Title
        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setTitle(R.string.feed_menu_blocked);
    }

    @Override
    protected void createTabs() {
        addTab(R.string.communities, new BlockedCommunityFeedAdapter(viewPager, connection, new ViewModelProvider(this)));
        addTab(R.string.users, new BlockedUserFeedAdapter(viewPager, connection, new ViewModelProvider(this)));
    }
}
