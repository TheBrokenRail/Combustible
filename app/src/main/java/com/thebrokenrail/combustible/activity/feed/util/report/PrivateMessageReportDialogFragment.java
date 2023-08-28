package com.thebrokenrail.combustible.activity.feed.util.report;

import com.thebrokenrail.combustible.api.method.CreatePrivateMessageReport;
import com.thebrokenrail.combustible.api.util.Method;

public class PrivateMessageReportDialogFragment extends ReportDialogFragment {
    @Override
    protected Method<?> createReport(int id, String reason) {
        CreatePrivateMessageReport method = new CreatePrivateMessageReport();
        method.private_message_id = id;
        method.reason = reason;
        return method;
    }
}
