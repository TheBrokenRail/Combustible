package com.thebrokenrail.combustible.activity.feed.tabbed.inbox;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.tabbed.TabbedFeedActivity;
import com.thebrokenrail.combustible.activity.feed.tabbed.inbox.privatemessage.PrivateMessageFeedAdapter;
import com.thebrokenrail.combustible.api.method.MarkAllAsRead;
import com.thebrokenrail.combustible.util.Util;

import java.util.Objects;

public class InboxFeedActivity extends TabbedFeedActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Title
        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setTitle(R.string.feed_menu_inbox);
    }

    @Override
    protected void createTabs() {
        addTab(R.string.inbox_replies, new RepliesFeedAdapter(viewPager, connection, new ViewModelProvider(this)));
        addTab(R.string.inbox_mentions, new MentionsFeedAdapter(viewPager, connection, new ViewModelProvider(this)));
        addTab(R.string.inbox_private_messages, new PrivateMessageFeedAdapter(viewPager, connection, new ViewModelProvider(this)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean ret = super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.feed_mark_all_as_read).setVisible(true);
        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.feed_mark_all_as_read) {
            MarkAllAsRead method = new MarkAllAsRead();
            connection.send(method, getRepliesResponse -> {
                // Refresh
                refresh();
            }, () -> {
                // Error
                Util.unknownError(InboxFeedActivity.this);
            });
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
