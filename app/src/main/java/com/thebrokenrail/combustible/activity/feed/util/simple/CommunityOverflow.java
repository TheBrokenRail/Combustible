package com.thebrokenrail.combustible.activity.feed.util.simple;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.util.overflow.BaseOverflow;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommunityView;
import com.thebrokenrail.combustible.util.Sharing;

abstract class CommunityOverflow extends BaseOverflow<CommunityView> {
    public CommunityOverflow(View view, Connection connection, CommunityView obj) {
        super(view, connection, obj);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.community_share) {
            // Share
            Sharing.shareCommunity(context, obj);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected int getMenuResource() {
        return R.menu.community_overflow;
    }

    @Override
    protected void onCreateMenu(Menu menu) {
    }
}