package com.thebrokenrail.combustible.activity.feed.util.overflow;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.util.report.ReportDialogFragment;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.util.Util;

import java.util.function.Consumer;

public abstract class PostOrCommentOverflow<T> extends BaseOverflow<T> {
    public PostOrCommentOverflow(View view, Connection connection, T obj, Consumer<T> updateFunction) {
        super(view, connection, obj, updateFunction);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.post_share) {
            // Share
            share();
            return true;
        } else if (item.getItemId() == R.id.post_save) {
            // Save
            save(true);
            return true;
        } else if (item.getItemId() == R.id.post_unsave) {
            // Unsave
            save(false);
            return true;
        } else if (item.getItemId() == R.id.post_report) {
            // Get Fragment Manager
            AppCompatActivity activity = Util.getActivityFromContext(context);
            FragmentManager fragmentManager = activity.getSupportFragmentManager();

            // Create Report Dialog
            ReportDialogFragment dialog = createReportDialog();
            dialog.show(fragmentManager, "report_" + dialog.getClass().getName());
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
        menu.findItem(R.id.post_save).setVisible(connection.hasToken() && !isSaved());
        menu.findItem(R.id.post_unsave).setVisible(connection.hasToken() && isSaved());
        menu.findItem(R.id.post_share).setVisible(showShare());
    }

    protected abstract void save(boolean shouldSave);

    protected abstract boolean isSaved();

    protected boolean showShare() {
        return true;
    }

    protected abstract void share();

    protected abstract ReportDialogFragment createReportDialog();
}
