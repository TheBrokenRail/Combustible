package com.thebrokenrail.combustible.activity.settings.user;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.api.method.DeleteAccount;
import com.thebrokenrail.combustible.util.Config;
import com.thebrokenrail.combustible.util.Util;

public class DeleteAccountDialogFragment extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create View
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View root = inflater.inflate(R.layout.dialog_delete_account, null);

        // Create Dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.user_settings_delete_account)
                .setView(root)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        // Bind
        TextInputEditText password = root.findViewById(R.id.dialog_delete_account_password_edit_text);
        password.requestFocus();

        // On CLick
        dialog.setOnShowListener(dialog1 -> {
            UserSettingsActivity activity = (UserSettingsActivity) requireActivity();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v12 -> {
                // Create Method
                DeleteAccount method = new DeleteAccount();
                method.password = String.valueOf(password.getText());

                // Dismiss
                dialog.dismiss();

                // Do It
                activity.getConnection().send(method, loginResponse -> {
                    // Logout
                    Config config = new Config(activity);
                    config.setToken(null);

                    // Exit Activity
                    activity.finish();
                }, () -> {
                    // Failure
                    Util.showTextDialog(activity, R.string.user_settings_delete_account, R.string.delete_account_error);
                });
            });
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v1 -> dialog.dismiss());
        });

        // Return
        return dialog;
    }
}
