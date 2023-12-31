package com.thebrokenrail.combustible.activity.feed.comment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.FeedActivity;
import com.thebrokenrail.combustible.activity.feed.comment.dataset.CommentTreeDataset;
import com.thebrokenrail.combustible.activity.feed.util.adapter.base.FeedAdapter;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.api.method.BlockCommunity;
import com.thebrokenrail.combustible.api.method.GetPostResponse;
import com.thebrokenrail.combustible.api.method.MarkPostAsRead;
import com.thebrokenrail.combustible.util.Sharing;
import com.thebrokenrail.combustible.util.Util;

import java.util.Objects;

public class CommentFeedActivity extends FeedActivity {
    public static final String POST_ID_EXTRA = "com.thebrokenrail.combustible.POST_ID_EXTRA";
    public static final String COMMENT_ID_EXTRA = "com.thebrokenrail.combustible.COMMENT_ID_EXTRA";

    private CommentTreeDataset.ParentType parentType = null;
    private int parent;

    // Blocking
    private Integer communityId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Title
        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setTitle(getIntent().hasExtra(POST_ID_EXTRA) ? R.string.comments_view_post : R.string.comments_view_comment);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Mark As Read
        markAsRead();

        // Community Blocking
        canBlockCommunity = true;
        updateNavigation();
    }

    private void markAsRead() {
        if (connection.hasToken()) {
            // Mark Post As Read
            if (parentType == CommentTreeDataset.ParentType.POST) {
                MarkPostAsRead method = new MarkPostAsRead();
                method.post_id = parent;
                method.read = true;
                connection.send(method, postResponse -> {}, () -> Util.unknownError(CommentFeedActivity.this));
            }
        }
    }

    @Override
    protected void onCreateBeforeAdapter() {
        super.onCreateBeforeAdapter();

        // Get Community ID
        Intent intent = getIntent();
        if (intent.hasExtra(POST_ID_EXTRA)) {
            parentType = CommentTreeDataset.ParentType.POST;
            parent = intent.getIntExtra(POST_ID_EXTRA, 0);
        } else if (intent.hasExtra(COMMENT_ID_EXTRA)) {
            parentType = CommentTreeDataset.ParentType.COMMENT;
            parent = intent.getIntExtra(COMMENT_ID_EXTRA, 0);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    protected FeedAdapter<?> createAdapter(RecyclerView feed) {
        return new CommentFeedAdapter(feed, connection, new ViewModelProvider(this), parentType, parent);
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
        return true;
    }

    @Override
    protected void share() {
        if (parentType == CommentTreeDataset.ParentType.POST) {
            Sharing.sharePost(this, parent);
        } else {
            Sharing.shareComment(this, parent);
        }
    }

    @Override
    protected void addPrerequisites(FeedPrerequisites prerequisites) {
        super.addPrerequisites(prerequisites);

        // Load Post
        boolean isPost = parentType == CommentTreeDataset.ParentType.POST;
        if (isPost) {
            prerequisites.add(new FeedPrerequisite.Post(parent, null));
        } else {
            prerequisites.add(new FeedPrerequisite.Post(null, parent));
            prerequisites.add(new FeedPrerequisite.Comment(parent));
        }

        // Info Dialog
        prerequisites.listen((prerequisite, isRefreshing) -> {
            if (prerequisite instanceof FeedPrerequisite.Post) {
                GetPostResponse getPostResponse = ((FeedPrerequisite.Post) prerequisite).get();
                infoCommunity.set(CommentFeedActivity.this, getPostResponse.community_view);
                // Community Blocking
                isCommunityBlocked = getPostResponse.community_view.blocked;
                communityId = getPostResponse.community_view.community.id;
                updateNavigation();
            }
        });
    }

    @Override
    protected BlockCommunity blockCommunity(boolean shouldBlock) {
        BlockCommunity method = new BlockCommunity();
        method.block = shouldBlock;
        method.community_id = communityId;
        return method;
    }
}
