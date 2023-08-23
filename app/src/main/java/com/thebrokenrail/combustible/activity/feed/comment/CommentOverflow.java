package com.thebrokenrail.combustible.activity.feed.comment;

import android.view.View;

import com.thebrokenrail.combustible.activity.feed.util.overflow.PostOrCommentOverflow;
import com.thebrokenrail.combustible.activity.feed.util.report.CommentReportDialogFragment;
import com.thebrokenrail.combustible.activity.feed.util.report.ReportDialogFragment;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.SaveComment;
import com.thebrokenrail.combustible.util.Sharing;
import com.thebrokenrail.combustible.util.Util;

import java.util.function.Consumer;

class CommentOverflow extends PostOrCommentOverflow<CommentView> {
    public CommentOverflow(View view, Connection connection, CommentView obj, Consumer<CommentView> updateFunction) {
        super(view, connection, obj, updateFunction);
    }

    @Override
    protected void save(boolean shouldSave) {
        SaveComment method = new SaveComment();
        method.save = shouldSave;
        method.comment_id = obj.comment.id;
        connection.send(method, commentResponse -> updateFunction.accept(commentResponse.comment_view), () -> Util.unknownError(context));
    }

    @Override
    protected boolean isSaved() {
        return obj.saved;
    }

    @Override
    protected void share() {
        Sharing.shareComment(context, obj.comment.id);
    }

    @Override
    protected ReportDialogFragment createReportDialog() {
        CommentReportDialogFragment dialog = new CommentReportDialogFragment();
        dialog.setId(obj.comment.id);
        return dialog;
    }
}
