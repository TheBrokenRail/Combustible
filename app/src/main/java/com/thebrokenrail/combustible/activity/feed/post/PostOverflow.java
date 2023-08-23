package com.thebrokenrail.combustible.activity.feed.post;

import android.view.View;

import com.thebrokenrail.combustible.activity.feed.util.overflow.PostOrCommentOverflow;
import com.thebrokenrail.combustible.activity.feed.util.report.PostReportDialogFragment;
import com.thebrokenrail.combustible.activity.feed.util.report.ReportDialogFragment;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.api.method.SavePost;
import com.thebrokenrail.combustible.util.Sharing;
import com.thebrokenrail.combustible.util.Util;

import java.util.function.Consumer;

class PostOverflow extends PostOrCommentOverflow<PostView> {
    public PostOverflow(View view, Connection connection, PostView obj, Consumer<PostView> updateFunction) {
        super(view, connection, obj, updateFunction);
    }

    @Override
    protected void save(boolean shouldSave) {
        SavePost method = new SavePost();
        method.save = shouldSave;
        method.post_id = obj.post.id;
        connection.send(method, postResponse -> updateFunction.accept(postResponse.post_view), () -> Util.unknownError(context));
    }

    @Override
    protected boolean isSaved() {
        return obj.saved;
    }

    @Override
    protected void share() {
        Sharing.sharePost(context, obj.post.id);
    }

    @Override
    protected ReportDialogFragment createReportDialog() {
        PostReportDialogFragment dialog = new PostReportDialogFragment();
        dialog.setId(obj.post.id);
        return dialog;
    }
}
