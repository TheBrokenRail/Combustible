package com.thebrokenrail.combustible.activity.fullscreen.login;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.api.method.PasswordReset;
import com.thebrokenrail.combustible.util.Util;

import java.util.Objects;

public class PasswordRecoveryDialogFragment extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create View
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View root = inflater.inflate(R.layout.dialog_password_recovery, null);
        TextInputLayout emailField = root.findViewById(R.id.dialog_password_recovery_email_field);
        emailField.requestFocus();

        // Create Dialog
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.login_password_recovery)
                .setView(root)
                .setPositiveButton(R.string.ok, (dialog1, which) -> {
                    // Send
                    PasswordReset method = new PasswordReset();
                    method.email = Objects.requireNonNull(emailField.getEditText()).getText().toString();
                    LoginActivity activity = (LoginActivity) requireActivity();
                    activity.getConnection().send(method, o -> Util.showTextDialog(activity, R.string.login_password_recovery, R.string.login_password_recovery_success), () -> Util.showTextDialog(activity, R.string.error, R.string.login_password_recovery_failure));
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }
}
