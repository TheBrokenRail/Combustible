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

import java.util.Objects;

public class TotpTokenDialogFragment extends AppCompatDialogFragment {
    public TotpTokenDialogFragment() {
        super();
    }

    TotpTokenDialogFragment(String username, String password) {
        super();
        Bundle arguments = new Bundle();
        arguments.putString("username", username);
        arguments.putString("password", password);
        setArguments(arguments);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create View
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View root = inflater.inflate(R.layout.dialog_totp_token, null);
        TextInputLayout tokenField = root.findViewById(R.id.dialog_totp_token_field);
        tokenField.requestFocus();

        // Create Dialog
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.login_totp_token)
                .setView(root)
                .setPositiveButton(R.string.ok, (dialog1, which) -> {
                    // Login
                    Bundle arguments = getArguments();
                    assert arguments != null;
                    String username = arguments.getString("username");
                    String password = arguments.getString("password");
                    String token = Objects.requireNonNull(tokenField.getEditText()).getText().toString();
                    LoginActivity activity = (LoginActivity) requireActivity();
                    activity.tryLogin(username, password, token);
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }
}
