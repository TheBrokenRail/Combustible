package com.thebrokenrail.combustible.activity.feed.post;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.api.method.SavePost;
import com.thebrokenrail.combustible.util.BaseOverflow;
import com.thebrokenrail.combustible.util.Sharing;
import com.thebrokenrail.combustible.util.Util;

import java.util.function.Consumer;

class PostOverflow extends BaseOverflow<PostView> {
    public PostOverflow(View view, Connection connection, PostView obj, Consumer<PostView> updateFunction) {
        super(view, connection, obj, updateFunction);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.post_share) {
            // Share
            Sharing.sharePost(context, obj.post.id);
            return true;
        } else if (item.getItemId() == R.id.post_save) {
            save(true);
            return true;
        } else if (item.getItemId() == R.id.post_unsave) {
            save(false);
            return true;
        } else {
            return false;
        }
    }

    private void save(boolean shouldSave) {
        SavePost method = new SavePost();
        method.save = shouldSave;
        method.post_id = obj.post.id;
        connection.send(method, postResponse -> updateFunction.accept(postResponse.post_view), () -> Util.unknownError(context));
    }

    @Override
    protected int getMenuResource() {
        return R.menu.post_overflow;
    }

    @Override
    protected void onCreateMenu(Menu menu) {
        menu.findItem(R.id.post_save).setVisible(connection.hasToken() && !obj.saved);
        menu.findItem(R.id.post_unsave).setVisible(connection.hasToken() && obj.saved);
        menu.findItem(R.id.post_share).setVisible(showShare());
    }

    protected boolean showShare() {
        return true;
    }
}
