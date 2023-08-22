package com.thebrokenrail.combustible.activity.feed.tabbed.search;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.tabbed.TabbedFeedActivity;
import com.thebrokenrail.combustible.util.SearchSuggestionProvider;

import java.util.Objects;

public class SearchFeedActivity extends TabbedFeedActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Title
        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setTitle(R.string.search);

        // Search Suggestions
        String query = getQuery();
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
        suggestions.saveRecentQuery(query, null);
    }

    private String getQuery() {
        Intent intent = getIntent();
        if (!intent.hasExtra(SearchManager.QUERY)) {
            throw new RuntimeException();
        }
        return intent.getStringExtra(SearchManager.QUERY);
    }

    @Override
    protected void createTabs() {
        String query = getQuery();
        addTab(R.string.posts, new SearchPostFeedAdapter(viewPager, connection, new ViewModelProvider(this), query));
        addTab(R.string.comments, new SearchCommentFeedAdapter(viewPager, connection, new ViewModelProvider(this), query));
        addTab(R.string.communities, new SearchCommunityFeedAdapter(viewPager, connection, new ViewModelProvider(this), query));
        addTab(R.string.users, new SearchUserFeedAdapter(viewPager, connection, new ViewModelProvider(this), query));
    }
}
