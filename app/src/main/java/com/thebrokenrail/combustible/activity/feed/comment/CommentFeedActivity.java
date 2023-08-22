package com.thebrokenrail.combustible.activity.feed.comment;

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
import com.thebrokenrail.combustible.activity.feed.FeedAdapter;
import com.thebrokenrail.combustible.activity.feed.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.api.method.MarkPostAsRead;
import com.thebrokenrail.combustible.util.Sharing;
import com.thebrokenrail.combustible.util.Util;

import java.util.Objects;

public class CommentFeedActivity extends FeedActivity {
    public static final String POST_ID_EXTRA = "com.thebrokenrail.combustible.POST_ID_EXTRA";
    public static final String COMMENT_ID_EXTRA = "com.thebrokenrail.combustible.COMMENT_ID_EXTRA";

    private FlatCommentFeedAdapter.ParentType parentType = null;
    private int parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Title
        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setTitle(getIntent().hasExtra(POST_ID_EXTRA) ? R.string.comments_view_post : R.string.comments_view_comment);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Mark Post As Read
        if (connection.hasToken() && parentType == FlatCommentFeedAdapter.ParentType.POST) {
            MarkPostAsRead method = new MarkPostAsRead();
            method.post_id = parent;
            method.read = true;
            connection.send(method, postResponse -> {}, () -> Util.unknownError(CommentFeedActivity.this));
        }
    }

    @Override
    protected void onCreateBeforeAdapter() {
        super.onCreateBeforeAdapter();

        // Get Community ID
        Intent intent = getIntent();
        if (intent.hasExtra(POST_ID_EXTRA)) {
            parentType = FlatCommentFeedAdapter.ParentType.POST;
            parent = intent.getIntExtra(POST_ID_EXTRA, 0);
        } else if (intent.hasExtra(COMMENT_ID_EXTRA)) {
            parentType = FlatCommentFeedAdapter.ParentType.COMMENT;
            parent = intent.getIntExtra(COMMENT_ID_EXTRA, 0);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    protected FeedAdapter<?> createAdapter(RecyclerView feed) {
        return new CommentFeedAdapter(feed, connection, new ViewModelProvider(this), "comments", parentType, parent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean ret = super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.feed_show_info).setVisible(false);
        return ret;
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

    @Override
    protected boolean canShare() {
        return true;
    }

    @Override
    protected void share() {
        if (parentType == FlatCommentFeedAdapter.ParentType.POST) {
            Sharing.sharePost(this, parent);
        } else {
            Sharing.shareComment(this, parent);
        }
    }

    @Override
    protected void addPrerequisites(FeedPrerequisites prerequisites) {
        super.addPrerequisites(prerequisites);

        // Load Post
        if (parentType == FlatCommentFeedAdapter.ParentType.POST) {
            prerequisites.add(new FeedPrerequisite.Post(parent));
        }
    }
}
