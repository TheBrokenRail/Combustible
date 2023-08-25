package com.thebrokenrail.combustible.activity.fullscreen.login;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.fullscreen.FullscreenActivity;
import com.thebrokenrail.combustible.api.method.Login;
import com.thebrokenrail.combustible.util.Config;
import com.thebrokenrail.combustible.util.Util;

import java.util.Objects;

public class LoginActivity extends FullscreenActivity {
    private TextInputLayout username;
    private TextInputLayout password;

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
        cancel.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Login
        Button login = findViewById(R.id.login_submit);
        login.setOnClickListener(v -> {
            String usernameStr = Objects.requireNonNull(username.getEditText()).getText().toString();
            String passwordStr = Objects.requireNonNull(password.getEditText()).getText().toString();
            tryLogin(usernameStr, passwordStr, null);
        });

        // Password Recovery
        Button forgotPassword = findViewById(R.id.login_forgot_password);
        forgotPassword.setOnClickListener(v -> new PasswordRecoveryDialogFragment().show(getSupportFragmentManager(), "password_recovery"));
    }

    void tryLogin(String username, String password, String totpToken) {
        // Try
        Login method = new Login();
        method.username_or_email = username;
        method.password = password;
        method.totp_2fa_token = totpToken;
        // Send
        connection.send(method, loginResponse -> {
            // 2FA
            if (loginResponse == null) {
                new TotpTokenDialogFragment(username, password).show(getSupportFragmentManager(), "totp_token");
                return;
            }

            // Success
            if (loginResponse.jwt != null) {
                Config config = new Config(LoginActivity.this);
                config.setToken(loginResponse.jwt);
                finish();
            } else {
                loginError();
            }
        }, this::loginError);
    }

    private void loginError() {
        // Error Dialog
        Util.showTextDialog(this, R.string.error, R.string.login_error);
    }
}
