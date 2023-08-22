package com.thebrokenrail.combustible.activity.feed.post;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import androidx.appcompat.widget.PopupMenu;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.util.Config;

import okhttp3.HttpUrl;

class PostOverflow implements PopupMenu.OnMenuItemClickListener {
    private final Context context;
    private final PostView post;

    public PostOverflow(Context context, PostView post) {
        this.context = context;
        this.post = post;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.post_share) {
            // Share

            // Build URL
            Config config = new Config(context);
            HttpUrl url = config.getInstance();
            url = url.newBuilder().addPathSegments("post/" + post.post.id).build();
            String urlStr = url.toString();

            // Launch
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, urlStr);
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            context.startActivity(shareIntent);
            return true;
        } else {
            return false;
        }
    }
}
