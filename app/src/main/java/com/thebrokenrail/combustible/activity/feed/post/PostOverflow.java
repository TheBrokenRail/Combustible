package com.thebrokenrail.combustible.activity.feed.post;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.create.PostCreateActivity;
import com.thebrokenrail.combustible.activity.feed.util.overflow.PostOrCommentOverflow;
import com.thebrokenrail.combustible.activity.feed.util.report.PostReportDialogFragment;
import com.thebrokenrail.combustible.activity.feed.util.report.ReportDialogFragment;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.DeletePost;
import com.thebrokenrail.combustible.api.method.FeaturePost;
import com.thebrokenrail.combustible.api.method.LockPost;
import com.thebrokenrail.combustible.api.method.PostFeatureType;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.api.method.RemovePost;
import com.thebrokenrail.combustible.api.method.SavePost;
import com.thebrokenrail.combustible.util.RequestCodes;
import com.thebrokenrail.combustible.util.Sharing;
import com.thebrokenrail.combustible.util.Util;

abstract class PostOverflow extends PostOrCommentOverflow<PostView> {
    public PostOverflow(View view, Connection connection, PostView obj) {
        super(view, connection, obj);
    }

    @Override
    protected void onCreateMenu(Menu menu) {
        super.onCreateMenu(menu);
        // Lock
        menu.findItem(R.id.post_lock).setVisible(getPermissions().canLock(obj.post));
        if (obj.post.locked) {
            menu.findItem(R.id.post_lock).setTitle(R.string.unlock);
        }
        // Pin (Community)
        menu.findItem(R.id.post_pin_community).setVisible(getPermissions().canPinCommunity(obj.post));
        if (obj.post.featured_community) {
            menu.findItem(R.id.post_pin_community).setTitle(R.string.unpin_community);
        }
        // Pin (Instance)
        menu.findItem(R.id.post_pin_instance).setVisible(getPermissions().canPinInstance());
        if (obj.post.featured_local) {
            menu.findItem(R.id.post_pin_instance).setTitle(R.string.unpin_instance);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.post_lock) {
            // Lock Post
            LockPost method = new LockPost();
            method.post_id = obj.post.id;
            method.locked = !obj.post.locked;
            connection.send(method, postResponse -> update(postResponse.post_view), () -> Util.unknownError(context));
            return true;
        } else if (item.getItemId() == R.id.post_pin_community || item.getItemId() == R.id.post_pin_instance) {
            // Pin
            FeaturePost method = new FeaturePost();
            method.post_id = obj.post.id;
            if (item.getItemId() == R.id.post_pin_community) {
                // Community
                method.feature_type = PostFeatureType.Community;
                method.featured = !obj.post.featured_community;
            } else {
                // Instance
                method.feature_type = PostFeatureType.Local;
                method.featured = !obj.post.featured_local;
            }
            connection.send(method, postResponse -> update(postResponse.post_view), () -> Util.unknownError(context));
            return true;
        } else {
            return super.onMenuItemClick(item);
        }
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
        activity.startActivityForResult(intent, RequestCodes.CREATE_POST);
    }

    @Override
    protected boolean canDelete() {
        return getPermissions().canDelete(obj.post);
    }

    @Override
    protected boolean canRemove() {
        return getPermissions().canRemove(obj.post);
    }

    @Override
    protected boolean isDeleted() {
        return obj.post.deleted;
    }

    @Override
    protected boolean isRemoved() {
        return obj.post.removed;
    }

    @Override
    protected void delete(boolean restore) {
        DeletePost method = new DeletePost();
        method.post_id = obj.post.id;
        method.deleted = !restore;
        connection.send(method, postResponse -> update(postResponse.post_view), () -> Util.unknownError(context));
    }

    @Override
    protected void remove(boolean restore) {
        RemovePost method = new RemovePost();
        method.post_id = obj.post.id;
        method.removed = !restore;
        connection.send(method, postResponse -> update(postResponse.post_view), () -> Util.unknownError(context));
    }
}
