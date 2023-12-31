package com.thebrokenrail.combustible.activity.feed.post;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.FeedActivity;
import com.thebrokenrail.combustible.activity.feed.util.adapter.base.FeedAdapter;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.api.method.BlockCommunity;
import com.thebrokenrail.combustible.api.method.CommunityView;
import com.thebrokenrail.combustible.api.method.GetCommunityResponse;
import com.thebrokenrail.combustible.api.method.GetSiteResponse;
import com.thebrokenrail.combustible.util.AppBarAvatar;
import com.thebrokenrail.combustible.util.Names;
import com.thebrokenrail.combustible.util.Sharing;

import java.util.Objects;

public class PostFeedActivity extends FeedActivity {
    public static final String COMMUNITY_ID_EXTRA = "com.thebrokenrail.combustible.COMMUNITY_ID_EXTRA";

    private Integer communityId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Title
        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        if (communityId == null) {
            actionBar.setTitle(R.string.posts_home);
        } else {
            actionBar.setDisplayHomeAsUpEnabled(true);

            // Can Be Blocked
            canBlockCommunity = true;
            updateNavigation();
        }
    }

    @Override
    protected void onCreateBeforeAdapter() {
        super.onCreateBeforeAdapter();

        // Get Community ID
        Intent intent = getIntent();
        if (intent.hasExtra(COMMUNITY_ID_EXTRA)) {
            communityId = intent.getIntExtra(COMMUNITY_ID_EXTRA, 0);
        } else {
            communityId = null;
        }
    }

    @Override
    protected FeedAdapter<?> createAdapter(RecyclerView feed) {
        return new PostFeedAdapter(feed, connection, new ViewModelProvider(this), communityId);
    }

    @Override
    protected void addPrerequisites(FeedPrerequisites prerequisites) {
        super.addPrerequisites(prerequisites);

        // Load Community
        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        if (communityId != null) {
            prerequisites.add(new FeedPrerequisite.Community(communityId));
        }
        prerequisites.listen((prerequisite, isRefreshing) -> {
            if (communityId != null && prerequisite instanceof FeedPrerequisite.Community) {
                GetCommunityResponse getCommunityResponse = ((FeedPrerequisite.Community) prerequisite).get();

                // Title
                actionBar.setTitle(getCommunityResponse.community_view.community.title);
                // Subtitle
                actionBar.setSubtitle('!' + Names.getCommunityName(getCommunityResponse.community_view.community));
                // Toolbar Toasts
                setupToolbarToasts();
                // Info
                infoCommunity.set(PostFeedActivity.this, getCommunityResponse.community_view);
                // Share/Blocking
                community = getCommunityResponse.community_view;
                isCommunityBlocked = community.blocked;

                // Update Navigation
                updateNavigation();
                invalidateOptionsMenu();
            } else if (communityId == null && prerequisite instanceof FeedPrerequisite.Site) {
                GetSiteResponse getSiteResponse = ((FeedPrerequisite.Site) prerequisite).get();

                // Info
                infoCommunity.set(PostFeedActivity.this, getSiteResponse);
                updateNavigation();
            }
        });

        // Avatar
        AppBarAvatar appBarAvatar = new AppBarAvatar(this, communityId != null ? FeedPrerequisite.Community.class : FeedPrerequisite.Site.class);
        appBarAvatar.handlePrerequisites(prerequisites);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean ret = super.onPrepareOptionsMenu(menu);
        if (canShare()) {
            menu.findItem(R.id.feed_share).setEnabled(community != null);
        }
        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected boolean canShare() {
        return communityId != null;
    }

    private CommunityView community = null;

    @Override
    protected void share() {
        Sharing.shareCommunity(this, community.community);
    }

    @Override
    protected BlockCommunity blockCommunity(boolean shouldBlock) {
        BlockCommunity method = new BlockCommunity();
        method.block = shouldBlock;
        method.community_id = communityId;
        return method;
    }

    @Override
    protected boolean canSearch() {
        return communityId == null;
    }
}
