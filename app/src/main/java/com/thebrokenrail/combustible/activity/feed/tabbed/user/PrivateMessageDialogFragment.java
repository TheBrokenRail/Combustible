package com.thebrokenrail.combustible.activity.feed.tabbed.user;

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
import com.thebrokenrail.combustible.api.method.CreatePrivateMessage;
import com.thebrokenrail.combustible.util.Util;

public class PrivateMessageDialogFragment extends AppCompatDialogFragment {
    PrivateMessageDialogFragment(int user) {
        super();
        Bundle arguments = new Bundle();
        arguments.putInt("user", user);
        setArguments(arguments);
    }

    public PrivateMessageDialogFragment() {
        super();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create View
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View root = inflater.inflate(R.layout.dialog_private_message, null);

        // Create Dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.private_message)
                .setView(root)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        // Bind
        TextInputEditText message = root.findViewById(R.id.dialog_private_message_edit_text);
        message.requestFocus();

        // On CLick
        dialog.setOnShowListener(dialog1 -> {
            LemmyActivity activity = (LemmyActivity) requireActivity();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v12 -> {
                // Get Info
                Bundle arguments = getArguments();
                assert arguments != null;
                int user = arguments.getInt("user");
                String messageStr = String.valueOf(message.getText());

                // Create Method
                CreatePrivateMessage method = new CreatePrivateMessage();
                method.content = messageStr;
                method.recipient_id = user;

                // Dismiss
                dialog.dismiss();

                // Do It
                activity.getConnection().send(method, response -> {
                    // Success
                    Toast.makeText(activity.getApplicationContext(), R.string.private_message_success, Toast.LENGTH_SHORT).show();
                }, () -> {
                    // Failure
                    Util.showTextDialog(activity, R.string.private_message, R.string.private_message_error);
                });
            });
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v1 -> dialog.dismiss());
        });

        // Return
        return dialog;
    }
}
