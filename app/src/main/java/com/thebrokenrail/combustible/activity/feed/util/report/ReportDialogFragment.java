package com.thebrokenrail.combustible.activity.feed.util.report;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.LemmyActivity;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.util.Util;

public abstract class ReportDialogFragment extends AppCompatDialogFragment {
    public void setId(int id) {
        Bundle arguments = new Bundle();
        arguments.putInt("id", id);
        setArguments(arguments);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create View
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View root = inflater.inflate(R.layout.dialog_report, null);

        // Create Dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.report)
                .setView(root)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        // Bind
        TextInputEditText reason = root.findViewById(R.id.dialog_report_reason_edit_text);
        reason.requestFocus();

        // On CLick
        dialog.setOnShowListener(dialog1 -> {
            LemmyActivity activity = (LemmyActivity) requireActivity();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v12 -> {
                // Get Info
                Bundle arguments = getArguments();
                assert arguments != null;
                int id = arguments.getInt("id");
                String reasonStr = String.valueOf(reason.getText());

                // Create Method
                Connection.Method<?> method = createReport(id, reasonStr);

                // Dismiss
                dialog.dismiss();

                // Do It
                activity.getConnection().send(method, response -> {
                    // Success
                    Toast.makeText(activity.getApplicationContext(), R.string.report_success, Toast.LENGTH_SHORT).show();
                }, () -> {
                    // Failure
                    Util.showTextDialog(activity, R.string.report, R.string.report_error);
                });
            });
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v1 -> dialog.dismiss());
        });

        // Return
        return dialog;
    }

    protected abstract Connection.Method<?> createReport(int id, String reason);
}
