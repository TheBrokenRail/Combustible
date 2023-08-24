package com.thebrokenrail.combustible.activity.feed.post;

import android.content.Intent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.thebrokenrail.combustible.activity.create.PostCreateActivity;
import com.thebrokenrail.combustible.activity.feed.util.overflow.PostOrCommentOverflow;
import com.thebrokenrail.combustible.activity.feed.util.report.PostReportDialogFragment;
import com.thebrokenrail.combustible.activity.feed.util.report.ReportDialogFragment;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.api.method.SavePost;
import com.thebrokenrail.combustible.util.RequestCodes;
import com.thebrokenrail.combustible.util.Sharing;
import com.thebrokenrail.combustible.util.Util;

abstract class PostOverflow extends PostOrCommentOverflow<PostView> {
    public PostOverflow(View view, Connection connection, PostView obj) {
        super(view, connection, obj);
    }

    @Override
    protected void save(boolean shouldSave) {
        SavePost method = new SavePost();
        method.save = shouldSave;
        method.post_id = obj.post.id;
        connection.send(method, postResponse -> update(postResponse.post_view), () -> Util.unknownError(context));
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

    @Override
    protected boolean canEdit() {
        return obj.creator.id.equals(getCurrentUser());
    }

    @Override
    protected void edit() {
        AppCompatActivity activity = Util.getActivityFromContext(context);
        Intent intent = new Intent(activity, PostCreateActivity.class);
        intent.putExtra(PostCreateActivity.EDIT_ID_EXTRA, obj.post.id);
        //noinspection deprecation
        activity.startActivityForResult(intent, RequestCodes.CREATE_POST_REQUEST_CODE);
    }
}
