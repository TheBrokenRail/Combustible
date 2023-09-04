package com.thebrokenrail.combustible.activity.feed.tabbed.inbox.privatemessage;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.util.overflow.BaseOverflow;
import com.thebrokenrail.combustible.activity.feed.util.report.PrivateMessageReportDialogFragment;
import com.thebrokenrail.combustible.activity.feed.util.report.ReportDialogFragment;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.PrivateMessageView;
import com.thebrokenrail.combustible.util.Util;

public class PrivateMessageOverflow extends BaseOverflow<PrivateMessageView> {
    public PrivateMessageOverflow(View view, Connection connection, PrivateMessageView obj) {
        super(view, connection, obj);
    }

    @Override
    protected int getMenuResource() {
        return R.menu.private_message_overflow;
    }

    @Override
    protected void onCreateMenu(Menu menu) {
    }

    @Override
    protected void update(PrivateMessageView newObj) {
        throw new RuntimeException();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.post_report) {
            // Get Fragment Manager
            AppCompatActivity activity = Util.getActivityFromContext(context);
            FragmentManager fragmentManager = activity.getSupportFragmentManager();

            // Create Report Dialog
            ReportDialogFragment dialog = new PrivateMessageReportDialogFragment();
            dialog.setId(obj.private_message.id);
            dialog.show(fragmentManager, "report_" + dialog.getClass().getName());
            return true;
        } else if (item.getItemId() == R.id.post_copy_text) {
            // Copy Text
            Util.copyText(context, obj.private_message.content);
            return true;
        } else {
            return false;
        }
    }
}
