package com.thebrokenrail.combustible.activity.feed.comment;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.create.CommentCreateActivity;
import com.thebrokenrail.combustible.activity.feed.util.overflow.PostOrCommentOverflow;
import com.thebrokenrail.combustible.activity.feed.util.report.CommentReportDialogFragment;
import com.thebrokenrail.combustible.activity.feed.util.report.ReportDialogFragment;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.DeleteComment;
import com.thebrokenrail.combustible.api.method.DistinguishComment;
import com.thebrokenrail.combustible.api.method.RemoveComment;
import com.thebrokenrail.combustible.api.method.SaveComment;
import com.thebrokenrail.combustible.util.RequestCodes;
import com.thebrokenrail.combustible.util.Sharing;
import com.thebrokenrail.combustible.util.Util;

abstract class CommentOverflow extends PostOrCommentOverflow<CommentView> {
    public CommentOverflow(View view, Connection connection, CommentView obj) {
        super(view, connection, obj);
    }

    @Override
    protected void onCreateMenu(Menu menu) {
        super.onCreateMenu(menu);
        // Distinguish
        menu.findItem(R.id.post_distinguish).setVisible(getPermissions().canDistinguish(obj));
        if (obj.comment.distinguished) {
            menu.findItem(R.id.post_distinguish).setIcon(R.drawable.baseline_mic_off_24);
            menu.findItem(R.id.post_distinguish).setTitle(R.string.undistinguish);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.post_distinguish) {
            // Distinguish Comment
            DistinguishComment method = new DistinguishComment();
            method.comment_id = obj.comment.id;
            method.distinguished = !obj.comment.distinguished;
            connection.send(method, commentResponse -> update(commentResponse.comment_view), () -> Util.unknownError(context));
            return true;
        } else {
            return super.onMenuItemClick(item);
        }
    }

    @Override
    protected void save(boolean shouldSave) {
        SaveComment method = new SaveComment();
        method.save = shouldSave;
        method.comment_id = obj.comment.id;
        connection.send(method, commentResponse -> update(commentResponse.comment_view), () -> Util.unknownError(context));
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

    @Override
    protected boolean canEdit() {
        return obj.creator.id.equals(getCurrentUser());
    }

    @Override
    protected void edit() {
        AppCompatActivity activity = Util.getActivityFromContext(context);
        Intent intent = new Intent(activity, CommentCreateActivity.class);
        intent.putExtra(CommentCreateActivity.EDIT_ID_EXTRA, obj.comment.id);
        //noinspection deprecation
        activity.startActivityForResult(intent, RequestCodes.CREATE_COMMENT);
    }

    @Override
    protected boolean canDelete() {
        return getPermissions().canDelete(obj);
    }

    @Override
    protected boolean canRemove() {
        return getPermissions().canRemove(obj);
    }

    @Override
    protected boolean isDeleted() {
        return obj.comment.deleted;
    }

    @Override
    protected boolean isRemoved() {
        return obj.comment.removed;
    }



    @Override
    protected void delete(boolean restore) {
        DeleteComment method = new DeleteComment();
        method.comment_id = obj.comment.id;
        method.deleted = !restore;
        connection.send(method, commentResponse -> update(commentResponse.comment_view), () -> Util.unknownError(context));
    }

    @Override
    protected void remove(boolean restore) {
        RemoveComment method = new RemoveComment();
        method.comment_id = obj.comment.id;
        method.removed = !restore;
        connection.send(method, commentResponse -> update(commentResponse.comment_view), () -> Util.unknownError(context));
    }

    @Override
    protected CharSequence getText() {
        return obj.comment.content;
    }
}
