package com.thebrokenrail.combustible.activity.feed.util.overflow;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.util.report.ReportDialogFragment;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.util.Permissions;
import com.thebrokenrail.combustible.util.Util;

public abstract class PostOrCommentOverflow<T> extends BaseOverflow<T> {
    public PostOrCommentOverflow(View view, Connection connection, T obj) {
        super(view, connection, obj);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.post_share) {
            // Share
            share();
            return true;
        } else if (item.getItemId() == R.id.post_save) {
            // Save/Unsave
            save(!isSaved());
            return true;
        } else if (item.getItemId() == R.id.post_report) {
            // Get Fragment Manager
            AppCompatActivity activity = Util.getActivityFromContext(context);
            FragmentManager fragmentManager = activity.getSupportFragmentManager();

            // Create Report Dialog
            ReportDialogFragment dialog = createReportDialog();
            dialog.show(fragmentManager, "report_" + dialog.getClass().getName());
            return true;
        } else if (item.getItemId() == R.id.post_edit) {
            edit();
            return true;
        } else if (item.getItemId() == R.id.post_delete) {
            delete(isDeleted());
            return true;
        } else if (item.getItemId() == R.id.post_remove) {
            remove(isRemoved());
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected int getMenuResource() {
        return R.menu.post_overflow;
    }

    @Override
    protected void onCreateMenu(Menu menu) {
        // Save/Unsave
        menu.findItem(R.id.post_save).setVisible(connection.hasToken());
        if (isSaved()) {
            menu.findItem(R.id.post_save).setTitle(R.string.unsave);
            menu.findItem(R.id.post_save).setIcon(R.drawable.baseline_bookmark_24);
        }
        // Share
        menu.findItem(R.id.post_share).setVisible(showShare());
        // Edit
        menu.findItem(R.id.post_edit).setVisible(canEdit());
        // Delete
        menu.findItem(R.id.post_delete).setVisible(canDelete());
        if (isDeleted()) {
            menu.findItem(R.id.post_delete).setTitle(R.string.restore);
            menu.findItem(R.id.post_delete).setIcon(R.drawable.baseline_restore_from_trash_24);
        }
        // Remove
        menu.findItem(R.id.post_remove).setVisible(canRemove());
        if (isRemoved()) {
            menu.findItem(R.id.post_remove).setTitle(R.string.restore);
            menu.findItem(R.id.post_remove).setIcon(R.drawable.baseline_restore_from_trash_24);
        }
    }

    protected abstract void save(boolean shouldSave);

    protected abstract boolean isSaved();

    protected boolean showShare() {
        return true;
    }

    protected abstract void share();

    protected abstract ReportDialogFragment createReportDialog();

    protected abstract boolean canEdit();

    protected abstract void edit();

    protected abstract Integer getCurrentUser();

    protected abstract Permissions getPermissions();

    protected abstract boolean canDelete();
    protected abstract boolean canRemove();

    protected abstract boolean isDeleted();
    protected abstract boolean isRemoved();

    protected abstract void delete(boolean restore);
    protected abstract void remove(boolean restore);
}
