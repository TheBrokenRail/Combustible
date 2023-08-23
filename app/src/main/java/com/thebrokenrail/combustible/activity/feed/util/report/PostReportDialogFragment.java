package com.thebrokenrail.combustible.activity.feed.util.report;

import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CreatePostReport;

public class PostReportDialogFragment extends ReportDialogFragment {
    @Override
    protected Connection.Method<?> createReport(int id, String reason) {
        CreatePostReport method = new CreatePostReport();
        method.post_id = id;
        method.reason = reason;
        return method;
    }
}
