package com.thebrokenrail.combustible.activity.feed.util.report;

import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CreatePrivateMessageReport;

public class PrivateMessageReportDialogFragment extends ReportDialogFragment {
    @Override
    protected Connection.Method<?> createReport(int id, String reason) {
        CreatePrivateMessageReport method = new CreatePrivateMessageReport();
        method.private_message_id = id;
        method.reason = reason;
        return method;
    }
}
