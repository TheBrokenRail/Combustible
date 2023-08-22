package com.thebrokenrail.combustible.activity.feed.comment;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.SaveComment;
import com.thebrokenrail.combustible.util.BaseOverflow;
import com.thebrokenrail.combustible.util.Sharing;
import com.thebrokenrail.combustible.util.Util;

import java.util.function.Consumer;

class CommentOverflow extends BaseOverflow<CommentView> {
    public CommentOverflow(View view, Connection connection, CommentView obj, Consumer<CommentView> updateFunction) {
        super(view, connection, obj, updateFunction);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.post_share) {
            // Share
            Sharing.shareComment(context, obj.comment.id);
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
        SaveComment method = new SaveComment();
        method.save = shouldSave;
        method.comment_id = obj.comment.id;
        connection.send(method, commentResponse -> updateFunction.accept(commentResponse.comment_view), () -> Util.unknownError(context));
    }

    @Override
    protected int getMenuResource() {
        return R.menu.post_overflow;
    }

    @Override
    protected void onCreateMenu(Menu menu) {
        menu.findItem(R.id.post_save).setVisible(connection.hasToken() && !obj.saved);
        menu.findItem(R.id.post_unsave).setVisible(connection.hasToken() && obj.saved);
    }
}
