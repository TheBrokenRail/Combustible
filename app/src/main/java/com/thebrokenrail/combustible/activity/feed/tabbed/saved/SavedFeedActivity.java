package com.thebrokenrail.combustible.activity.feed.tabbed.saved;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.tabbed.TabbedFeedActivity;

import java.util.Objects;

public class SavedFeedActivity extends TabbedFeedActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Title
        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setTitle(R.string.feed_menu_saved);
    }

    @Override
    protected void createTabs() {
        addTab(R.string.posts, new SavedPostFeedAdapter(viewPager, connection, new ViewModelProvider(this), "posts"));
        addTab(R.string.comments, new SavedCommentFeedAdapter(viewPager, connection, new ViewModelProvider(this), "comments"));
    }
}
