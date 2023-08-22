package com.thebrokenrail.combustible.activity.fullscreen.welcome;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.thebrokenrail.combustible.R;

import java.util.Objects;

import okhttp3.HttpUrl;

public class CustomInstanceDialogFragment extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create View
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View root = inflater.inflate(R.layout.dialog_custom_instance, null);

        // Create Dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.welcome_instance_custom)
                .setView(root)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create();
        TextInputLayout urlField = root.findViewById(R.id.dialog_custom_instance_field);
        urlField.requestFocus();
        dialog.setOnShowListener(dialog1 -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v12 -> {
                String urlStr = Objects.requireNonNull(urlField.getEditText()).getText().toString();

                // Parse
                HttpUrl url = HttpUrl.parse(urlStr);
                if (url == null) {
                    // Error
                    urlField.setError(getString(R.string.welcome_invalid_url));
                    return;
                }

                // Dismiss
                dialog.dismiss();

                // Check
                ((WelcomeActivity) requireActivity()).checkAndSetInstance(url);
            });
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v1 -> dialog.dismiss());
        });

        // Return
        return dialog;
    }
}
