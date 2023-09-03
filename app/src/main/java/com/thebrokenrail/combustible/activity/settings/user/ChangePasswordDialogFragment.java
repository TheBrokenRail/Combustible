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
import com.google.android.material.textfield.TextInputLayout;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.api.method.ChangePassword;
import com.thebrokenrail.combustible.util.config.Config;
import com.thebrokenrail.combustible.util.Util;

import java.util.Objects;

public class ChangePasswordDialogFragment extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create View
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View root = inflater.inflate(R.layout.dialog_change_password, null);

        // Create Dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.user_settings_change_password)
                .setView(root)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        // Bind
        TextInputEditText oldPassword = root.findViewById(R.id.dialog_change_password_old_edit_text);
        oldPassword.requestFocus();
        TextInputEditText newPassword = root.findViewById(R.id.dialog_change_password_new_edit_text);
        TextInputEditText newPasswordVerify = root.findViewById(R.id.dialog_change_password_new_verify_edit_text);
        TextInputLayout newPasswordVerifyField = root.findViewById(R.id.dialog_change_password_new_verify_field);

        // On CLick
        dialog.setOnShowListener(dialog1 -> {
            UserSettingsActivity activity = (UserSettingsActivity) requireActivity();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v12 -> {
                // Create Method
                ChangePassword method = new ChangePassword();
                method.old_password = String.valueOf(oldPassword.getText());
                method.new_password = String.valueOf(newPassword.getText());
                method.new_password_verify = String.valueOf(newPasswordVerify.getText());

                // Check
                if (!Objects.equals(method.new_password, method.new_password_verify)) {
                    // Error
                    newPasswordVerifyField.setError(getString(R.string.change_password_match_error));
                    return;
                }

                // Dismiss
                dialog.dismiss();

                // Do It
                activity.getConnection().send(method, loginResponse -> {
                    // Change Token
                    Config config = Config.create(activity);
                    config.setToken(loginResponse.jwt);

                    // Reload Activity
                    activity.fullRecreate();
                }, () -> {
                    // Failure
                    Util.showTextDialog(activity, R.string.user_settings_change_password, R.string.change_password_error);
                });
            });
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v1 -> dialog.dismiss());
        });

        // Return
        return dialog;
    }
}
