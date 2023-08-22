package com.thebrokenrail.combustible.activity.feed.comment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.RecyclerView;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.FeedActivity;
import com.thebrokenrail.combustible.activity.feed.FeedAdapter;

import java.util.Objects;

public class CommentFeedActivity extends FeedActivity {
    public static final String POST_ID_EXTRA = "com.thebrokenrail.combustible.POST_ID_EXTRA";
    public static final String COMMENT_ID_EXTRA = "com.thebrokenrail.combustible.COMMENT_ID_EXTRA";

    private CommentFeedAdapter.ParentType parentType = null;
    private int parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Title
        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setTitle(R.string.posts_home);
    }

    @Override
    protected void onCreateBeforeAdapter() {
        super.onCreateBeforeAdapter();

        // Get Community ID
        Intent intent = getIntent();
        if (intent.hasExtra(POST_ID_EXTRA)) {
            parentType = CommentFeedAdapter.ParentType.POST;
            parent = intent.getIntExtra(POST_ID_EXTRA, 0);
        } else if (intent.hasExtra(COMMENT_ID_EXTRA)) {
            parentType = CommentFeedAdapter.ParentType.COMMENT;
            parent = intent.getIntExtra(COMMENT_ID_EXTRA, 0);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    protected FeedAdapter<?> createAdapter(RecyclerView feed) {
        return new CommentFeedAdapter(feed, connection, parentType, parent);
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
