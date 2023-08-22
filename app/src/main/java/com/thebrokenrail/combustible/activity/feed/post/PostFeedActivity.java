package com.thebrokenrail.combustible.activity.feed.post;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.RecyclerView;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.FeedActivity;
import com.thebrokenrail.combustible.activity.feed.FeedAdapter;
import com.thebrokenrail.combustible.api.method.GetCommunity;

import java.util.Objects;

public class PostFeedActivity extends FeedActivity {
    public static final String COMMUNITY_ID_EXTRA = "com.thebrokenrail.combustible.COMMUNITY_ID_EXTRA";

    private boolean hasCommunityID = false;
    private int communityID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Title
        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        if (!hasCommunityID) {
            actionBar.setTitle(R.string.posts_home);
        } else {
            actionBar.setDisplayHomeAsUpEnabled(true);

            // Load Community
            GetCommunity method = new GetCommunity();
            method.id = communityID;
            connection.send(method, getCommunityResponse -> {
                // Title
                actionBar.setTitle(getCommunityResponse.community_view.community.title);

                // Banner
                ((PostFeedAdapter) getAdapter()).setBannerUrl(getCommunityResponse.community_view.community.banner);
            }, () -> {});
        }
    }

    @Override
    protected void onCreateBeforeAdapter() {
        super.onCreateBeforeAdapter();

        // Get Community ID
        Intent intent = getIntent();
        hasCommunityID = intent.hasExtra(COMMUNITY_ID_EXTRA);
        if (hasCommunityID) {
            communityID = intent.getIntExtra(COMMUNITY_ID_EXTRA, 0);
        }
    }

    @Override
    protected FeedAdapter<?> createAdapter(RecyclerView feed) {
        return new PostFeedAdapter(feed, connection, hasCommunityID, communityID);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
