package com.thebrokenrail.combustible.activity.feed.util.report;

import com.thebrokenrail.combustible.api.method.CreateCommentReport;
import com.thebrokenrail.combustible.api.util.Method;

public class CommentReportDialogFragment extends ReportDialogFragment {
    @Override
    protected Method<?> createReport(int id, String reason) {
        CreateCommentReport method = new CreateCommentReport();
        method.comment_id = id;
        method.reason = reason;
        return method;
    }
}
