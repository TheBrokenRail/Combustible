package com.thebrokenrail.combustible.activity.feed.util.report;

import com.thebrokenrail.combustible.api.method.CreatePostReport;
import com.thebrokenrail.combustible.api.util.Method;

public class PostReportDialogFragment extends ReportDialogFragment {
    @Override
    protected Method<?> createReport(int id, String reason) {
        CreatePostReport method = new CreatePostReport();
        method.post_id = id;
        method.reason = reason;
        return method;
    }
}
