package com.thebrokenrail.combustible.activity.fullscreen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.Login;
import com.thebrokenrail.combustible.api.method.PasswordReset;
import com.thebrokenrail.combustible.util.Config;
import com.thebrokenrail.combustible.util.Util;

import java.util.Objects;

public class LoginActivity extends FullscreenActivity {
    private TextInputLayout username;
    private TextInputLayout password;
    private Button login;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Field
        username = findViewById(R.id.login_username);
        username.requestFocus();
        password = findViewById(R.id.login_password);

        // Cancel
        Button cancel = findViewById(R.id.login_cancel);
        cancel.setOnClickListener(v -> onBackPressed());

        // Login
        login = findViewById(R.id.login_submit);
        login.setOnClickListener(v -> {
            login.setEnabled(false);
            tryLogin();
        });

        // Password Recovery
        Button forgotPassword = findViewById(R.id.login_forgot_password);
        forgotPassword.setOnClickListener(v -> passwordRecovery());
    }

    private void tryLogin() {
        // Connect
        Config config = new Config(LoginActivity.this);
        Connection connection = new Connection(config.getInstance());
        connection.setCallbackHelper(this::runOnUiThread);

        // Try
        Login method = new Login();
        method.username_or_email = Objects.requireNonNull(username.getEditText()).getText().toString();
        method.password = Objects.requireNonNull(password.getEditText()).getText().toString();
        // Failure
        connection.send(method, loginResponse -> {
            // Success
            if (loginResponse.jwt != null) {
                config.setToken(loginResponse.jwt);
                finish();
            } else {
                loginError();
            }
        }, this::loginError);
    }

    private void loginError() {
        login.setEnabled(true);

        // Error Dialog
        Util.showTextDialog(this, R.string.error, R.string.login_error);
    }

    private void passwordRecovery() {
        // Create Dialog View
        LayoutInflater inflater = LayoutInflater.from(this);
        View root = inflater.inflate(R.layout.dialog_password_recovery, null);
        TextInputLayout emailField = root.findViewById(R.id.dialog_password_recovery_email_field);

        // Create Dialog
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.login_password_recovery)
                .setView(root)
                .setPositiveButton(R.string.ok, (dialog1, which) -> {
                    // Connect
                    Config config = new Config(LoginActivity.this);
                    Connection connection = new Connection(config.getInstance());
                    connection.setCallbackHelper(this::runOnUiThread);

                    // Send
                    PasswordReset method = new PasswordReset();
                    method.email = Objects.requireNonNull(emailField.getEditText()).getText().toString();
                    connection.send(method, o -> Util.showTextDialog(LoginActivity.this, R.string.login_password_recovery, R.string.login_password_recovery_success), () -> Util.showTextDialog(LoginActivity.this, R.string.error, R.string.login_password_recovery_failure));
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        emailField.requestFocus();
    }
}
